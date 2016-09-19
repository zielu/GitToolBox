package zielu.gittoolbox.repo;

import com.google.common.collect.ImmutableSet;
import com.intellij.dvcs.repo.RepoStateException;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.jetbrains.annotations.NotNull;

public class GtConfig {
    private static final Logger LOG = Logger.getInstance(GtConfig.class);
    private static final Pattern SVN_REMOTE_SECTION = Pattern.compile("svn-remote \"(.*)\"");
    private static final GtConfig EMPTY = new GtConfig();

    private final ImmutableSet<String> svnRemotes;

    private GtConfig(ImmutableSet.Builder<String> svnRemotes) {
        this.svnRemotes = svnRemotes.build();
    }

    private GtConfig() {
        svnRemotes = ImmutableSet.of();
    }

    public boolean isSvnRemote(String name) {
        return svnRemotes.contains(name);
    }

    @NotNull
    public static GtConfig load(@NotNull File configFile) {
        if (!configFile.exists()) {
            LOG.info("No .git/config file at " + configFile.getPath());
            return EMPTY;
        } else {
            Ini ini = new Ini();
            ini.getConfig().setMultiOption(true);
            ini.getConfig().setTree(false);

            try {
                ini.load(configFile);
            } catch (IOException var8) {
                LOG.warn(new RepoStateException("Couldn\'t load .git/config file at " + configFile.getPath(), var8));
                return EMPTY;
            }
            ImmutableSet.Builder<String> svnRemotes = ImmutableSet.builder();
            for (Entry<String, Section> section : ini.entrySet()) {
                Matcher matcher = SVN_REMOTE_SECTION.matcher(section.getKey());
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    svnRemotes.add(name);
                }
            }
            return new GtConfig(svnRemotes);
        }
    }
}
