package zielu.junit5.intellij;

import com.intellij.openapi.util.io.FileUtil;
import java.io.File;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitTestExtension implements BeforeAllCallback, ParameterResolver {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public interface GitTestSetup {

    Path getRootPath();

    void setup(Git git) throws Exception;
  }

  public interface GitTest {

    void prepare(GitTestSetup setup);
  }

  private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(GitTestExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeAll(ExtensionContext context) {
    ParameterHolder holder = ParameterHolder.getHolder(context.getStore(NS));
    holder.register(GitTest.class, GitTestImpl::new);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return RESOLVER.supportsParameter(parameterContext, extensionContext);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return RESOLVER.resolveParameter(parameterContext, extensionContext);
  }

  private static class GitTestImpl implements GitTest {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void prepare(GitTestSetup setup) {
      try {
        prepareImpl(setup);
      } catch (Exception e) {
        throw new RuntimeException("Failed to initialize git", e);
      }
    }

    private void prepareImpl(GitTestSetup setup) throws Exception {
      Path rootPath = setup.getRootPath();
      File rootDir = rootPath.toFile();
      FileUtil.delete(rootDir);
      log.info("Initializing git repository in {}", rootPath);
      Git git = Git.init().setDirectory(rootDir).setBare(false).call();
      StoredConfig config = git.getRepository().getConfig();
      config.load();
      config.setString("user", null, "name", "Jon Snow");
      config.setString("user", null, "email", "JonSnow@email.com");
      config.save();
      log.info("Setup initial git repository state in {}", rootPath);
      setup.setup(git);
      git.close();
      log.info("Git repository ready in {}", rootPath);
    }
  }
}
