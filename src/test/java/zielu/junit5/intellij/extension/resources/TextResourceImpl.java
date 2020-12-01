package zielu.junit5.intellij.extension.resources;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.IOUtils;

class TextResourceImpl implements TextResource {
  private final ResourcePath resourcePath;

  TextResourceImpl(ResourcePath resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public List<String> getLines() {
    try {
      return IOUtils.readLines(getClass().getResourceAsStream(resourcePath.value()), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load " + resourcePath.value(), e);
    }
  }
}
