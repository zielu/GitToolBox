package zielu.gittoolbox.blame.calculator

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import git4idea.commands.GitLineHandlerListener
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max

/**
 * Parse output of incremental git blame.
 *
 * <p>Sample command line:
 * <pre>git blame --incremental -l -t -w --encoding=UTF-8 HEAD change-notes.html > blame-incremental.txt</pre>
 * </p>
 *
 * @see (https://git-scm.com/docs/git-blame#_incremental_output)[https://git-scm.com/docs/git-blame#_incremental_output]
 */
internal class IncrementalBlameBuilder : GitLineHandlerListener {
  private val entries: MutableList<Entry> = ArrayList()
  private val commitBuilders: MutableMap<String, CommitInfo.Builder> = HashMap()
  private val commits: MutableMap<String, CommitInfo> = HashMap()
  private var currentEntry: Entry = NULL_ENTRY
  private var currentCommitBuilder = CommitInfo.Builder(VcsRevisionNumber.NULL)
  private var lineCount: Int = 0

  fun buildLineInfos(): List<CommitInfo> {
    val lineInfos: MutableList<CommitInfo> = ArrayList(lineCount)
    for (i in 0 until lineCount) {
      lineInfos.add(CommitInfo.NULL)
    }
    for (entry in entries) {
      for (i in 0 until entry.lineCount) {
        val index = entry.lineNumber - 1 + i
        lineInfos[index] = commits.computeIfAbsent(entry.commitHash) { hash: String ->
          commitBuilders[hash]!!.build()
        }
      }
    }
    return lineInfos
  }

  override fun onLineAvailable(line: String, outputType: Key<*>) {
    if (ProcessOutputType.isStdout(outputType)) {
      parseLine(line)
    }
  }

  private fun parseLine(line: String) {
    if (currentEntry === NULL_ENTRY) {
      parseEntryHeader(line)
    } else {
      if (line.startsWith(filenameTag)) {
        finishEntry()
      } else {
        parseTagLine(line)
      }
    }
  }

  private fun parseEntryHeader(line: String) {
    val matcher: Matcher = entryStartPattern.matcher(line)
    if (matcher.matches()) {
      parseEntryHeader(matcher)
    }
  }

  private fun parseEntryHeader(matcher: Matcher) {
    val commitHash = matcher.group(1)
    currentCommitBuilder = commitBuilders.computeIfAbsent(commitHash, CommitInfo::Builder)
    val lineNumber = matcher.group(2).toInt()
    val numLines = matcher.group(3).toInt()
    currentEntry = Entry(commitHash, lineNumber, numLines)
    lineCount = max(lineCount, lineNumber + (numLines - 1))
  }

  private fun finishEntry() {
    entries.add(currentEntry)
    currentEntry = NULL_ENTRY
    currentCommitBuilder = CommitInfo.Builder(VcsRevisionNumber.NULL)
  }

  private fun parseTagLine(line: String) {
    when {
      line.startsWith(summaryTag) -> currentCommitBuilder.summary(extractTagValue(summaryTag, line))
      line.startsWith(authorTag) -> currentCommitBuilder.authorName(extractTagValue(authorTag, line))
      line.startsWith(authorMailTag) -> currentCommitBuilder.authorEmail(extractEmailValue(authorMailTag, line))
      line.startsWith(authorTimeTag) -> currentCommitBuilder.authorTime(extractTimeValue(authorTimeTag, line))
      line.startsWith(authorTimeZoneTag) ->
        currentCommitBuilder.authorTimeOffset(
          extractTimeZoneValue(authorTimeZoneTag, line)
        )
    }
  }

  companion object {
    private val entryStartPattern = Pattern.compile("([a-zA-Z0-9]+)\\s+\\d+\\s+(\\d+)\\s+(\\d+)")
    private const val filenameTag = "filename "
    private const val summaryTag = "summary "
    private const val authorTag = "author "
    private const val authorTimeTag = "author-time "
    private const val authorTimeZoneTag = "author-tz "
    private const val authorMailTag = "author-mail "
    private val NULL_ENTRY = Entry("NULL", -1, 0)
  }
}

private data class Entry(
  val commitHash: String,
  val lineNumber: Int,
  val lineCount: Int
)

private fun extractTagValue(tag: String, line: String): String {
  return line.substring(tag.length).trim()
}

private fun extractEmailValue(tag: String, line: String): String {
  val value = extractTagValue(tag, line)
  return value.substring(1, value.length - 1)
}

private fun extractTimeValue(tag: String, line: String): Instant {
  val value = extractTagValue(tag, line)
  val secondsOfEpoch = value.toLong()
  return Instant.ofEpochSecond(secondsOfEpoch)
}

private fun extractTimeZoneValue(tag: String, line: String): ZoneId {
  val value = extractTagValue(tag, line)
  return ZoneOffset.of(value)
}
