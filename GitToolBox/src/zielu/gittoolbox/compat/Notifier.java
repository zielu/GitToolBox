package zielu.gittoolbox.compat;

import com.google.common.base.Preconditions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsNotifier;
import org.jetbrains.annotations.NotNull;

public class Notifier {
    private final Project myProject;

    private Notifier(Project project) {
        myProject = project;
    }

    public static Notifier getInstance(@NotNull Project project) {
        return new Notifier(Preconditions.checkNotNull(project));
    }

    public void notifySuccess(@NotNull String message) {
        VcsNotifier.getInstance(myProject).notifySuccess(message);
    }

    public void notifyError(@NotNull String title, @NotNull String message) {
        VcsNotifier.getInstance(myProject).notifyError(title, message);
    }

    public void notifyWeakError(@NotNull String message) {
        VcsNotifier.getInstance(myProject).notifyWeakError(message);
    }

    public void notifyMinorInfo(@NotNull String title, @NotNull String message) {
        VcsNotifier.getInstance(myProject).notifyMinorInfo(title, message);
    }

    public void notifyMinorWarning(@NotNull String title, @NotNull String message) {
        VcsNotifier.getInstance(myProject).notifyMinorWarning(title, message);
    }
}
