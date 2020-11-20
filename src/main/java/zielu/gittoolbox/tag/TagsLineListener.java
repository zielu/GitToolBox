package zielu.gittoolbox.tag;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TagsLineListener implements GitLineHandlerListener {
  private static final String TAG_MARKER = "tag: ";
  private static final Pattern SINGLE_TAG_PATTERN = Pattern.compile(".*?\\(" + TAG_MARKER + "([^\\s]+?)\\).*");
  private static final Pattern TAG_PATTERN = Pattern.compile(TAG_MARKER + "(.+?)");

  private final Logger log = Logger.getInstance(getClass());
  private final List<String> tags = new ArrayList<>();

  @Override
  public void onLineAvailable(String line, Key outputType) {
    if (ProcessOutputType.isStdout(outputType)) {
      tags.addAll(parseTags(line));
    }
  }

  @Override
  public void processTerminated(int exitCode) {

  }

  @Override
  public void startFailed(Throwable exception) {
    log.warn("Start failed", exception);
  }

  List<String> getTags() {
    return new ArrayList<>(tags);
  }

  private List<String> parseTags(String line) {
    List<String> tags = new ArrayList<>();
    Matcher match = SINGLE_TAG_PATTERN.matcher(line);
    if (match.matches()) {
      tags.add(match.group(1));
    } else if (line.contains(TAG_MARKER)) {
      tags.addAll(parseMultipleTags(line));
    }
    return tags;
  }

  private List<String> parseMultipleTags(String line) {
    String content = extractContent(line);
    List<String> tags = Lists.newArrayList();
    for (String spec : Splitter.on(", ").split(content)) {
      extractTag(spec).ifPresent(tags::add);
    }
    return tags;
  }

  private String extractContent(String line) {
    String content = line;
    if (content.startsWith("(")) {
      content = content.substring(1);
    }
    if (content.endsWith(")")) {
      content = content.substring(0, content.length() - 1);
    }
    return content;
  }

  private Optional<String> extractTag(String spec) {
    Matcher match = TAG_PATTERN.matcher(spec);
    if (match.matches()) {
      return Optional.of(match.group(1));
    } else {
      return Optional.empty();
    }
  }
}
