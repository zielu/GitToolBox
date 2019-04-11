package zielu.junit5.intellij.extension.git;

import java.nio.file.Path;
import org.eclipse.jgit.api.Git;

public interface GitTestSetup {

  Path getRootPath();

  void setup(Git git) throws Exception;
}
