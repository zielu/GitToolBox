package zielu.gittoolbox.blame.calculator;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import git4idea.GitRevisionNumber;
import java.util.Date;
import java.util.Objects;

class CommitInfo {
  static final CommitInfo NULL = new CommitInfo(null);

  private final VcsRevisionNumber revisionNumber;
  private String authorName;
  private Date authorDate;
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

  void setAuthorTime(long authorTime) {
    this.authorDate = new Date(authorTime * 1000);
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

  Date getAuthorDate() {
    return authorDate;
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
