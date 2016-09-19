package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitCommand;
import git4idea.commands.GitHandlerUtil;
import git4idea.commands.GitSimpleHandler;
import git4idea.util.StringScanner;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;

public class GitTagCalculator {
    private static final Pattern singleTagPattern = Pattern.compile(".*?\\(tag: (.+?)\\).*");
    private static final Pattern tagPattern = Pattern.compile("tag: (.+?)");

    private final Project myProject;

    private GitTagCalculator(Project project) {
        myProject = project;
    }

    public static GitTagCalculator create(@NotNull Project project) {
        return new GitTagCalculator(Preconditions.checkNotNull(project));
    }

    public List<String> tagsForBranch(@NotNull VirtualFile gitRoot, @NotNull String branch) {
        GitSimpleHandler h = new GitSimpleHandler(myProject, Preconditions.checkNotNull(gitRoot), GitCommand.LOG);
        h.addParameters("--simplify-by-decoration", "--pretty=format:%d", "--encoding=UTF-8", Preconditions.checkNotNull(branch));
        h.setSilent(true);
        String output = GitHandlerUtil.doSynchronously(h, ResBundle.getString("tag.getting.existing.tags"), h.printableCommandLine());
        List<String> tags = Lists.newArrayList();
        for (StringScanner s = new StringScanner(output); s.hasMoreData(); ) {
            String line = s.line();
            Matcher match = singleTagPattern.matcher(line);
            if (match.matches()) {
                tags.add(match.group(1));
            } else if (line.contains("tag: ")) {
                tags.addAll(parseMultipleTags(line));
            }
        }
        return tags;
    }

    private List<String> parseMultipleTags(String line) {
        List<String> tags = Lists.newArrayList();
        for (String spec : Splitter.on(", ").split(line)) {
            Matcher match = tagPattern.matcher(spec);
            if (match.matches()) {
                tags.add(match.group(1));
            }
        }
        return tags;
    }

    public List<String> allTags(@NotNull VirtualFile gitRoot) {
        GitSimpleHandler h = new GitSimpleHandler(myProject, Preconditions.checkNotNull(gitRoot), GitCommand.TAG);
        h.setSilent(true);
        String output = GitHandlerUtil.doSynchronously(h, ResBundle.getString("tag.getting.tags.for.branch"), h.printableCommandLine());
        LinkedList<String> tags = Lists.newLinkedList();
        for (StringScanner s = new StringScanner(output); s.hasMoreData(); ) {
            String line = s.line();
            if (line.length() == 0) {
                continue;
            }
            tags.addFirst(line);
        }
        return Lists.newArrayList(tags);
    }
}
