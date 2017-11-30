package zielu.junit5.intellij;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.RunAll;
import com.intellij.testFramework.TestLoggerFactory;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.ui.UIUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.io.File;
import java.util.*;

/**
 * Based on UsefulTestCase
 */
public class IdeaExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(IdeaExtension.class);
    private static final String ORIGINAL_TEMP_DIR = FileUtil.getTempDirectory();
    private static final String DEFAULT_SETTINGS_EXTERNALIZED;
    public static final String TEMP_DIR_MARKER = "unitTest_";

    static {
        Logger.setFactory(TestLoggerFactory.class);
        System.setProperty("apple.awt.UIElement", "true");
        try {
            CodeInsightSettings defaultSettings = new CodeInsightSettings();
            Element oldS = new Element("temp");
            defaultSettings.writeExternal(oldS);
            DEFAULT_SETTINGS_EXTERNALIZED = JDOMUtil.writeElement(oldS);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> DELETE_ON_EXIT_HOOK_DOT_FILES;
    private static final Class DELETE_ON_EXIT_HOOK_CLASS;
    static {
        Class<?> aClass;
        try {
            aClass = Class.forName("java.io.DeleteOnExitHook");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked") Set<String> files = ReflectionUtil.getStaticFieldValue(aClass, Set.class, "files");
        DELETE_ON_EXIT_HOOK_CLASS = aClass;
        DELETE_ON_EXIT_HOOK_DOT_FILES = files;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Store store = getStore(context);
        String testName = FileUtil.sanitizeFileName(getTestName(context));
        store.put(Disposable.class, new TestDisposable(testName));
        if (containsTempFiles(context)) {
            if (StringUtil.isEmptyOrSpaces(testName)) testName = "";
            testName = new File(testName).getName(); // in case the test name contains file separators
            File tempDir = new File(ORIGINAL_TEMP_DIR, TEMP_DIR_MARKER + testName);
            FileUtil.resetCanonicalTempPathCache(tempDir.getPath());
            store.put(TempFilesInfo.class, new DefaultTempFilesInfo(tempDir.toPath()));
        }
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private boolean containsTempFiles(ExtensionContext context) {
        return context.getElement().filter(element -> element.isAnnotationPresent(ContainsTempFiles.class))
                .map(element -> element.getAnnotation(ContainsTempFiles.class))
                .map(ContainsTempFiles::value).orElse(false);
    }

    protected final String getTestName(ExtensionContext context) {
        return context.getRequiredTestClass().getName();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        new RunAll(
                () -> disposeRootDisposable(context),
                IdeaExtension::cleanupSwingDataStructures,
                IdeaExtension::cleanupDeleteOnExitHookList,
                () -> Disposer.setDebugMode(true),
                () -> {
                    if (containsTempFiles(context)) {
                        TempFilesInfo tempFilesInfo = getStore(context).remove(TempFilesInfo.class, TempFilesInfo.class);
                        FileUtil.resetCanonicalTempPathCache(ORIGINAL_TEMP_DIR);
                        FileUtil.delete(tempFilesInfo.tempDir().toFile());
                    }
                },
                UIUtil::removeLeakingAppleListeners
        ).run();
    }

    protected void disposeRootDisposable(ExtensionContext context) {
        Disposer.dispose(getTestRootDisposable(context));
    }

    @NotNull
    public Disposable getTestRootDisposable(ExtensionContext context) {
        return getStore(context).remove(Disposable.class, Disposable.class);
    }

    @SuppressWarnings("ConstantConditions")
    private static void cleanupSwingDataStructures() throws Exception {
        Object manager = ReflectionUtil.getDeclaredMethod(Class.forName("javax.swing.KeyboardManager"), "getCurrentManager").invoke(null);
        Map componentKeyStrokeMap = ReflectionUtil.getField(manager.getClass(), manager, Hashtable.class, "componentKeyStrokeMap");
        componentKeyStrokeMap.clear();
        Map containerMap = ReflectionUtil.getField(manager.getClass(), manager, Hashtable.class, "containerMap");
        containerMap.clear();
    }

    @SuppressWarnings("SynchronizeOnThis")
    private static void cleanupDeleteOnExitHookList() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        // try to reduce file set retained by java.io.DeleteOnExitHook
        List<String> list;
        synchronized (DELETE_ON_EXIT_HOOK_CLASS) {
            if (DELETE_ON_EXIT_HOOK_DOT_FILES.isEmpty()) return;
            list = new ArrayList<>(DELETE_ON_EXIT_HOOK_DOT_FILES);
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            String path = list.get(i);
            File file = new File(path);
            if (file.delete() || !file.exists()) {
                synchronized (DELETE_ON_EXIT_HOOK_CLASS) {
                    DELETE_ON_EXIT_HOOK_DOT_FILES.remove(path);
                }
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return supportsParameter(parameterContext, TempFilesInfo.class);
    }

    final <T> boolean supportsParameter(ParameterContext parameterContext, Class<T> type) {
        return type.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (TempFilesInfo.class.isAssignableFrom(parameterContext.getParameter().getType())) {
            return getStore(extensionContext).get(TempFilesInfo.class, TempFilesInfo.class);
        }
        return null;
    }

    protected class TestDisposable implements Disposable {
        private final String testName;
        private volatile boolean myDisposed;

        public TestDisposable(String testName) {
            this.testName = testName;
        }

        @Override
        public void dispose() {
            myDisposed = true;
        }

        public boolean isDisposed() {
            return myDisposed;
        }

        @Override
        public String toString() {
            return IdeaExtension.this.getClass() + (StringUtil.isEmpty(testName) ? "" : ".test" + testName);
        }
    }
}
