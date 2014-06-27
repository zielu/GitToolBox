package zielu.gittoolbox.tag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TagsPushSpec {
    private final VirtualFile myGitRoot;
    private final boolean myAll;
    private final ImmutableList<String> myTags;
    private final boolean myForce;

    private TagsPushSpec(VirtualFile gitRoot, Builder builder) {
        myGitRoot = gitRoot;
        if (builder.myTags == null) {
            myAll = true;
            myTags = ImmutableList.of();
        } else {
            myAll = false;
            myTags = builder.myTags;
        }
        myForce = builder.force;
    }

    public VirtualFile gitRoot() {
        return myGitRoot;
    }

    private List<String> initSpec() {
        List<String> spec = Lists.newArrayListWithCapacity(myTags.size() * 2 + 2);
        if (myForce) {
            spec.add("--force");
        }
        return spec;
    }

    public List<String> specs() {
        List<String> spec = initSpec();
        if (myAll) {
            spec.add("--tags");
        } else {
            for (String tag : myTags) {
                spec.add("tag");
                spec.add(tag);
            }
            return spec;
        }
        return spec;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ImmutableList<String> myTags;
        private boolean force;

        private Builder() {}

        public Builder tags(Iterable<String> tags) {
            myTags = ImmutableList.copyOf(tags);
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
