package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.repo.GitRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.GitCompatUtil;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.ConfigNotifier.Adapter;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.LogWatch;

public class GitToolBoxCompletionProject extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private final List<File> myAffectedFiles = new ArrayList<>();
    private Collection<GitRepository> myAffectedRepositories;
    private MessageBusConnection myConnection;
    private List<Formatter> myFormatters = ImmutableList.of();

    public GitToolBoxCompletionProject(@NotNull Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(ConfigNotifier.CONFIG_TOPIC, new Adapter() {
            @Override
            public void configChanged(Project project, GitToolBoxConfigForProject config) {
                fillFormatters(config);
            }
        });
    }

    private void fillFormatters(GitToolBoxConfigForProject config) {
        myFormatters = ImmutableList.copyOf(config.getCompletionFormatters());
    }

    @Override
    public void projectOpened() {
        fillFormatters(GitToolBoxConfigForProject.getInstance(myProject));
    }

    public static GitToolBoxCompletionProject getInstance(@NotNull Project project) {
        return project.getComponent(GitToolBoxCompletionProject.class);
    }

    public synchronized void updateAffected(Collection<File> affected){
        clearAffected();
        myAffectedFiles.addAll(affected);
    }

    public synchronized void clearAffected() {
        myAffectedRepositories = null;
        myAffectedFiles.clear();
    }

    public synchronized Collection<GitRepository> getAffected() {
        if (myAffectedRepositories == null){
            LogWatch getRepositoriesWatch = LogWatch.createStarted("Get repositories");
            myAffectedRepositories = getRepositories(myProject, myAffectedFiles);
            getRepositoriesWatch.finish();
        }
        return myAffectedRepositories;
    }

    private Collection<GitRepository> getRepositories(Project project, Collection<File> selectedFiles) {
        return GitCompatUtil.getRepositoriesForFiles(project, selectedFiles);
    }

    public List<Formatter> getFormatters() {
        return myFormatters;
    }

    @Override
    public void projectClosed() {
        clearAffected();
    }

    @Override
    public void disposeComponent() {
        myConnection.disconnect();
        myConnection = null;
        myFormatters = null;
    }
}
