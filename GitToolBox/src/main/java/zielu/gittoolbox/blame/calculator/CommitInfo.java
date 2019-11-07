package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import git4idea.GitRevisionNumber;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

class CommitInfo {
  static final CommitInfo NULL = new CommitInfo(null);

  private final VcsRevisionNumber revisionNumber;
  private String authorName;
  private Instant authorTime;
  private ZoneOffset authorTimeOffset;
  private ZonedDateTime authorDateTime;
  private String summary;

  CommitInfo(String commitHash) {
    if (commitHash == null) {
      revisionNumber = VcsRevisionNumber.NULL;
    } else {
      revisionNumber = new GitRevisionNumber(commitHash);
    }
  }

  void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  void setAuthorTime(Instant authorTime) {
    this.authorTime = authorTime;
  }

  void setAuthorTimeOffset(ZoneOffset authorTimeOffset) {
    this.authorTimeOffset = authorTimeOffset;
  }

  void setSummary(String summary) {
    this.summary = summary;
  }

  VcsRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }

  String getAuthorName() {
    return authorName;
  }

  @Nullable
  ZonedDateTime getAuthorDateTime() {
    if (authorTime == null) {
      return null;
    }
    if (authorDateTime == null) {
      ZoneOffset offset = authorTimeOffset == null ? ZoneOffset.UTC : authorTimeOffset;
      authorDateTime = ZonedDateTime.ofInstant(authorTime, offset);
    }
    return authorDateTime;
  }

  String getSummary() {
    return summary;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CommitInfo) {
      CommitInfo other = (CommitInfo) obj;
      return Objects.equals(this.revisionNumber, other.revisionNumber);
    }
    return false;
  }
}
