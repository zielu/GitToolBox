package zielu.gittoolbox.blame;

import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;
import git4idea.util.StringScanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parse output of <pre>git blame --incremental -l -t -w --encoding=UTF-8 HEAD change-notes.html > blame-incremental.txt</pre>
 */
public class IncrementalBlameCalculator implements GitLineHandlerListener {
  private static final String FILENAME_TAG = "filename ";
  private static final String SUMMARY_TAG = "summary ";
  private static final String AUTHOR_TAG = "author ";
  private static final String AUTHOR_TIME_TAG = "author-time ";

  private final Map<String, Commit> commits = new HashMap<>();
  private final List<Entry> entries = new ArrayList<>();

  private Commit currentCommit;
  private Entry currentEntry;

  public int getEntriesCount() {
    return entries.size();
  }

  @Override
  public void onLineAvailable(String line, Key outputType) {
    if (ProcessOutputType.isStdout(outputType)) {
      parseLine(line);
    }
  }

  private void parseLine(String line) {
    if (currentEntry == null) {
      parseEntryStart(line);
    } else {
      if (line.startsWith(FILENAME_TAG)) {
        parseEntryEnd(line);
      } else {
        parseTagLine(line);
      }
    }
  }

  private void parseEntryStart(String line) {
    StringScanner scanner = new StringScanner(line);
    String commitHash = scanner.spaceToken();
    currentCommit = getCommit(commitHash);
    scanner.spaceToken(); //skip source line
    int resultLine = Integer.parseInt(scanner.spaceToken());
    int numLines = Integer.parseInt(scanner.line());
    currentEntry = new Entry(currentCommit, resultLine, numLines);
  }

  private Commit getCommit(String commitHash) {
    return commits.computeIfAbsent(commitHash, Commit::new);
  }

  private void parseEntryEnd(String line) {
    entries.add(currentEntry);
    currentEntry = null;
  }

  private void parseTagLine(String line) {
    if (line.startsWith(SUMMARY_TAG)) {
      currentCommit.setSummary(line.substring(SUMMARY_TAG.length()).trim());
    } else if (line.startsWith(AUTHOR_TAG)) {
      currentCommit.setAuthorName(line.substring(AUTHOR_TAG.length()).trim());
    } else if (line.startsWith(AUTHOR_TIME_TAG)) {
      currentCommit.setAuthorTime(Long.parseLong(line.substring(AUTHOR_TIME_TAG.length()).trim()));
    }
  }

  private static class Entry {
    private final Commit commit;
    private final int resultLine;
    private final int numLines;

    private Entry(Commit commit, int resultLine, int numLines) {
      this.commit = commit;
      this.resultLine = resultLine;
      this.numLines = numLines;
    }
  }

  private static class Commit {
    private final String commitHash;
    private String authorName;
    private long authorTime;
    private String summary;

    private Commit(String commitHash) {
      this.commitHash = commitHash;
    }

    public void setAuthorName(String authorName) {
      this.authorName = authorName;
    }

    public void setAuthorTime(long authorTime) {
      this.authorTime = authorTime;
    }

    public void setSummary(String summary) {
      this.summary = summary;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Commit) {
        Commit other = (Commit) obj;
        return Objects.equals(this.commitHash, other.commitHash);
      }
      return false;
    }
  }
}
