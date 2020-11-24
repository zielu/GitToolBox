package zielu.junit5.intellij.extension.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import jodd.io.FileUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zielu.junit5.intellij.parameters.ExtensionContextParamResolver;
import zielu.junit5.intellij.parameters.ParameterHolder;

public class GitTestExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(GitTestExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeAll(ExtensionContext context) {
    ParameterHolder holder = ParameterHolder.getHolder(context.getStore(NS));
    holder.register(GitTest.class, GitTestImpl::new);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    ParameterHolder.removeHolder(context.getStore(NS));
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
      if (FileUtil.isExistingFolder(rootDir)) {
        log.info("Deleting existing git root dir {}", rootPath);
        FileUtil.deleteDir(rootDir);
      }
      log.info("Initializing git [bare={}] repository in {}", setup.isBare(), rootPath);
      try (Git git = Git.init().setDirectory(rootDir).setBare(setup.isBare()).call()) {
        if (!setup.isBare()) {
          StoredConfig config = git.getRepository().getConfig();
          config.load();
          config.setString("user", null, "name", "Jon Snow");
          config.setString("user", null, "email", "JonSnow@email.com");
          config.save();
        }
        log.info("Setup initial git repository state in {}", rootPath);
        setup.setup(git);
        git.close();
        log.info("Git repository ready in {}", rootPath);
      }
    }

    @Override
    public void ops(GitOps ops) {
      try {
        opsImpl(ops);
      } catch (IOException e) {
        throw new RuntimeException("Failed to call git", e);
      }
    }

    private void opsImpl(GitOps ops) throws IOException {
      Path rootPath = ops.getRootPath();
      try (Git git = Git.open(rootPath.toFile())) {
        ops.invoke(git);
      }
    }
  }
}
