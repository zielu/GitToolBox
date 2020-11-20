package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TagsPushSpec {
  private final VirtualFile gitRoot;
  private final boolean all;
  private final ImmutableList<String> tags;
  private final boolean force;

  private TagsPushSpec(VirtualFile gitRoot, Builder builder) {
    this.gitRoot = gitRoot;
    if (builder.tags == null) {
      all = true;
      tags = ImmutableList.of();
    } else {
      all = false;
      tags = builder.tags;
    }
    force = builder.force;
  }

  public static Builder builder() {
    return new Builder();
  }

  public VirtualFile gitRoot() {
    return gitRoot;
  }

  private List<String> initSpec() {
    List<String> spec = Lists.newArrayListWithCapacity(tags.size() * 2 + 2);
    if (force) {
      spec.add("--force");
    }
    return spec;
  }

  public List<String> specs() {
    List<String> spec = initSpec();
    if (all) {
      spec.add("--tags");
    } else {
      addSpecsForTags(spec);
    }
    return spec;
  }

  private void addSpecsForTags(List<String> spec) {
    for (String tag : tags) {
      spec.add("tag");
      spec.add(tag);
    }
  }

  public static class Builder {
    private ImmutableList<String> tags;
    private boolean force;

    private Builder() {
    }

    public Builder tags(Iterable<String> tags) {
      this.tags = ImmutableList.copyOf(tags);
      return this;
    }

    public Builder force() {
      force = true;
      return this;
    }

    public TagsPushSpec build(@NotNull VirtualFile gitRoot) {
      return new TagsPushSpec(Preconditions.checkNotNull(gitRoot), this);
    }
  }
}
