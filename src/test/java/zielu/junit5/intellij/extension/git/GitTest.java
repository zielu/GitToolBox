package zielu.junit5.intellij.extension.git;

public interface GitTest {

  void prepare(GitTestSetup setup);

  void ops(GitOps ops);
}
