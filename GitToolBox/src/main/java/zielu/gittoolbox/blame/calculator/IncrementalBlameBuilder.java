package zielu.gittoolbox.blame.calculator;

import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;
import git4idea.util.StringScanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse output of incremental git blame.
 *
 * <p>Sample command line:
 * <pre>git blame --incremental -l -t -w --encoding=UTF-8 HEAD change-notes.html > blame-incremental.txt</pre>
 * </p>
 */
public class IncrementalBlameBuilder implements GitLineHandlerListener {
  private static final String FILENAME_TAG = "filename ";
  private static final String SUMMARY_TAG = "summary ";
  private static final String AUTHOR_TAG = "author ";
  private static final String AUTHOR_TIME_TAG = "author-time ";

  private final Map<String, CommitInfo> commits = new HashMap<>();
  private final List<Entry> entries = new ArrayList<>();
  private int lineCount = 0;

  private CommitInfo currentCommit;
  private Entry currentEntry;

  List<CommitInfo> buildLineInfos() {
    List<CommitInfo> lineInfos = new ArrayList<>(lineCount);
    for (int i = 0; i < lineCount; i++) {
      lineInfos.add(CommitInfo.NULL);
    }
    for (Entry entry : entries) {
      for (int i = 0; i < entry.numLines; i++) {
        int index = entry.resultLine - 1 + i;
        lineInfos.set(index, entry.commit);
      }
    }
    return lineInfos;
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
    lineCount = Math.max(lineCount, resultLine + (numLines - 1));
  }

  private CommitInfo getCommit(String commitHash) {
    return commits.computeIfAbsent(commitHash, CommitInfo::new);
  }

  private void parseEntryEnd(String line) {
    entries.add(currentEntry);
    currentEntry = null;
    currentCommit = null;
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
    private final CommitInfo commit;
    private final int resultLine;
    private final int numLines;

    private Entry(CommitInfo commit, int resultLine, int numLines) {
      this.commit = commit;
      this.resultLine = resultLine;
      this.numLines = numLines;
    }
  }
}
