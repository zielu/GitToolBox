package zielu.junit5.intellij;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.ide.highlighter.ProjectFileType;
import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.idea.IdeaLogger;
import com.intellij.idea.IdeaTestApplication;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.impl.FileTypeManagerImpl;
import com.intellij.openapi.module.EmptyModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.VirtualFilePointerTracker;
import com.intellij.openapi.vfs.impl.jar.JarFileSystemImpl;
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFS;
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFSImpl;
import com.intellij.psi.codeStyle.CodeStyleSchemes;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.impl.DocumentCommitThread;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageManagerImpl;
import com.intellij.testFramework.*;
import com.intellij.util.ui.UIUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static com.intellij.testFramework.PlatformTestCase.createProject;
import static com.intellij.testFramework.UsefulTestCase.doCheckForSettingsDamage;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Based on PlatformTestCase
 */
public class IdeaHeavyExtension extends IdeaExtension {
    private static final Namespace NAMESPACE = Namespace.create(IdeaHeavyExtension.class);
    private static final String[] PREFIX_CANDIDATES = {
            "AppCode", "CLion", "CidrCommon",
            "DataGrip",
            "Python", "PyCharmCore",
            "Ruby",
            "Rider",
            "UltimateLangXml", "Idea", "PlatformLangXml" };

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        super.beforeAll(context);
        Store store = getStore(context);
        FilesToDelete filesToDelete = new FilesToDelete(getTestName(context));
        store.put(FilesToDelete.class, filesToDelete);
        File tempDir = new File(FileUtilRt.getTempDirectory());
        filesToDelete.add(tempDir);
        IdeaLogger.ourErrorsOccurred = null;
        initApplication(store);
        store.put(EditorListenerTracker.class, new EditorListenerTracker());
        store.put(ThreadTracker.class, new ThreadTracker());

        setUpProject(context);

        storeSettings(context);
        Project project = getProject(context);
        if (project != null) {
            ProjectManagerEx.getInstanceEx().openTestProject(project);
            CodeStyleSettingsManager.getInstance(project).setTemporarySettings(new CodeStyleSettings());
            InjectedLanguageManagerImpl.pushInjectors(project);
        }

        DocumentCommitThread.getInstance().clearQueue();
        UIUtil.dispatchAllInvocationEvents();
        store.put(VirtualFilePointerTracker.class, new VirtualFilePointerTracker());
    }

    private void storeSettings(ExtensionContext context) {
        if (!isStressTest() && ApplicationManager.getApplication() != null) {
            CodeStyleSettings codeStyleSettings = getCurrentCodeStyleSettings(context).clone();
            codeStyleSettings.getIndentOptions(StdFileTypes.JAVA);
            getStore(context).put(CodeStyleSettings.class, codeStyleSettings);
        }
    }

    @NotNull
    private CodeStyleSettings getCurrentCodeStyleSettings(ExtensionContext context) {
        if (CodeStyleSchemes.getInstance().getCurrentScheme() == null) return new CodeStyleSettings();
        return CodeStyleSettingsManager.getSettings(getProject(context));
    }

    private boolean isStressTest() {
        return false;
    }

    private void initApplication(Store store) {
        IdeaTestApplication application = getApplication(store);
        boolean firstTime = application == null;
        application = IdeaTestApplication.getInstance(null);
        DataProvider dataProvider = new HeavyTestDataProvider(store);
        store.put(DataProvider.class, dataProvider);
        application.setDataProvider(dataProvider);
        store.put(IdeaTestApplication.class, application);
        if (firstTime) {
            cleanPersistedVFSContent();
        }
        // try to remember old sdks as soon as possible after the app instantiation
        store.put(SdkLeakTracker.class, new SdkLeakTracker());
    }

    private IdeaTestApplication getApplication(Store store) {
        return store.get(IdeaTestApplication.class, IdeaTestApplication.class);
    }

    private void cleanPersistedVFSContent() {
        ((PersistentFSImpl) PersistentFS.getInstance()).cleanPersistedContents();
    }

    private void setUpProject(ExtensionContext context) throws Exception {
        ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();
        assert projectManager != null;

        File projectFile = getIprFile(context);

        Project project = doCreateProject(projectFile, getTestName(context));
        getStore(context).put(Project.class, project);
        projectManager.openTestProject(project);
        LocalFileSystem.getInstance().refreshIoFiles(getStore(context).get(FilesToDelete.class, FilesToDelete.class));

        setUpModule(project, context);

        setUpJdk(context);

        LightPlatformTestCase.clearUncommittedDocuments(project);

        runStartupActivities(context);
        ((FileTypeManagerImpl) FileTypeManager.getInstance()).drainReDetectQueue();
    }

    private Project getProject(ExtensionContext context) {
        return get(context, Project.class);
    }

    private File getIprFile(ExtensionContext context) throws IOException {
        File tempFile = FileUtil.createTempFile(getTestName(context), ProjectFileType.DOT_DEFAULT_EXTENSION);
        getFilesToDelete(context).add(tempFile);
        return tempFile;
    }

    private FilesToDelete getFilesToDelete(ExtensionContext context) {
        return get(context, FilesToDelete.class);
    }

    private Project doCreateProject(@NotNull File projectFile, String name) {
        return createProject(projectFile, getClass().getName() + "." + name);
    }

    private void setUpModule(Project project, ExtensionContext context) {
        new WriteCommandAction.Simple(project) {
            @Override
            protected void run() {
                getStore(context).put(Module.class, createMainModule(project, context));
            }
        }.execute().throwException();
    }

    @NotNull
    private Module createMainModule(Project project, ExtensionContext context) {
        return createModule(project.getName(), context);
    }

    @NotNull
    private Module createModule(@NonNls final String moduleName, ExtensionContext context) {
        return doCreateRealModule(moduleName, context);
    }

    @NotNull
    private Module doCreateRealModule(final String moduleName, ExtensionContext context) {
        return doCreateRealModuleIn(moduleName, getModuleType(), context);
    }

    @NotNull
    private Module doCreateRealModuleIn(String moduleName, final ModuleType moduleType, ExtensionContext context) {
        final VirtualFile baseDir = getProject(context).getBaseDir();
        assert baseDir != null;
        return createModuleAt(moduleName, moduleType, baseDir.getPath(), context);
    }

    @NotNull
    private Module createModuleAt(String moduleName, ModuleType moduleType, String path, ExtensionContext context) {
        File moduleFile = new File(FileUtil.toSystemDependentName(path), moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION);
        FileUtil.createIfDoesntExist(moduleFile);
        getFilesToDelete(context).add(moduleFile);
        return new WriteAction<Module>() {
            @Override
            protected void run(@NotNull Result<Module> result) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(moduleFile);
                assert virtualFile != null;
                Project project = getProject(context);
                Module module = ModuleManager.getInstance(project).newModule(virtualFile.getPath(), moduleType.getId());
                module.getModuleFile();
                result.setResult(module);
            }
        }.execute().getResultObject();
    }

    private ModuleType getModuleType() {
        return EmptyModuleType.getInstance();
    }

    private void setUpJdk(ExtensionContext context) {
        final Sdk jdk = getTestProjectJdk();
        Module[] modules = ModuleManager.getInstance(getProject(context)).getModules();
        for (Module module : modules) {
            ModuleRootModificationUtil.setModuleSdk(module, jdk);
        }
    }

    @Nullable
    private Sdk getTestProjectJdk() {
        return null;
    }

    private void runStartupActivities(ExtensionContext context) {
        Project project = getProject(context);
        final StartupManagerImpl startupManager = (StartupManagerImpl) StartupManager.getInstance(project);
        startupManager.runStartupActivities();
        startupManager.startCacheUpdate();
        startupManager.runPostStartupActivities();
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        Project project = getProject(context);

        new RunAll()
            .append(() -> disposeRootDisposable(context))
            .append(() -> {
                if (project != null) {
                    IdeaTestApplication application = getApplication(getStore(context));
                    LightPlatformTestCase.doTearDown(project, application);
                }
            })
            .append(() -> PlatformTestCase.closeAndDisposeProjectAndCheckThatNoOpenProjects(project))
            .append(UIUtil::dispatchAllInvocationEvents)
            .append(() -> checkForSettingsDamage(context))
            .append(() -> {
                if (project != null) {
                    InjectedLanguageManagerImpl.checkInjectorsAreDisposed(project);
                }
            })
            .append(() -> {
                ((JarFileSystemImpl) JarFileSystem.getInstance()).cleanupForNextTest();
                FilesToDelete filesToDelete = remove(context, FilesToDelete.class);
                filesToDelete.delete();
            })
            .append(() -> {
                if (IdeaLogger.ourErrorsOccurred != null) {
                    throw IdeaLogger.ourErrorsOccurred;
                }
            })
            .append(() -> super.afterAll(context))
            .append(() -> {
                EditorListenerTracker tracker = remove(context, EditorListenerTracker.class);
                if (tracker != null) {
                    tracker.checkListenersLeak();
                }
            })
            .append(() -> {
                ThreadTracker tracker = remove(context, ThreadTracker.class);
                if (tracker != null) {
                    tracker.checkLeak();
                }
            })
            .append(LightPlatformTestCase::checkEditorsReleased)
            .append(() -> get(context, SdkLeakTracker.class).checkForJdkTableLeaks())
            .append(() -> get(context, VirtualFilePointerTracker.class).assertPointersAreDisposed())
            .append(() -> {
                remove(context, Project.class);
                remove(context, Module.class);
            })
            .run();
    }

    private void checkForSettingsDamage(ExtensionContext context) {
        Application app = ApplicationManager.getApplication();
        if (isStressTest() || app == null || app instanceof MockApplication) {
            return;
        }

        CodeStyleSettings oldCodeStyleSettings = remove(context, CodeStyleSettings.class);
        if (oldCodeStyleSettings == null) {
            return;
        }
        doCheckForSettingsDamage(oldCodeStyleSettings, getCurrentCodeStyleSettings(context));
    }

    private <T> T get(ExtensionContext context, Class<T> type) {
        return getStore(context).get(type, type);
    }

    private <T> T remove(ExtensionContext context, Class<T> type) {
        return getStore(context).remove(type, type);
    }

    private static final class HeavyTestDataProvider implements DataProvider {
        private final Store store;

        private HeavyTestDataProvider(Store store) {
            this.store = store;
        }

        @Nullable
        @Override
        public Object getData(String dataId) {
            Project project = store.get(Project.class, Project.class);
            return project == null ? null : new TestDataProvider(project).getData(dataId);
        }
    }

    private static final class FilesToDelete implements Iterable<File> {
        private final String testName;
        private final Collection<File> files = new THashSet<>();

        private FilesToDelete(String testName) {
            this.testName = testName;
        }

        void add(File toDelete) {
            files.add(toDelete);
        }

        void delete() {
            files.forEach(file -> {
                boolean b = FileUtil.delete(file);
                if (!b && file.exists()) {
                    fail("Can't delete " + file.getAbsolutePath() + " in " + testName);
                }
            });
            LocalFileSystem.getInstance().refreshIoFiles(files);
        }

        @NotNull
        @Override
        public Iterator<File> iterator() {
            return files.iterator();
        }
    }
}
