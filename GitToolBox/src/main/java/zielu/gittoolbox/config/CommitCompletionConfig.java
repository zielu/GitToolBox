package zielu.gittoolbox.config;

import com.intellij.util.xmlb.annotations.Transient;
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.formatter.SimpleFormatter;

public class CommitCompletionConfig {
    public CommitCompletionType type = CommitCompletionType.SIMPLE;
    public String pattern;
    public String testInput;

    public static CommitCompletionConfig create(CommitCompletionType type) {
        if (type == CommitCompletionType.SIMPLE) {
            return new CommitCompletionConfig();
        } else if (type == CommitCompletionType.PATTERN) {
            CommitCompletionConfig config = new CommitCompletionConfig();
            config.type = type;
            config.pattern = "(.*)";
            config.testInput = "test input";
            return config;
        } else {
            throw new IllegalStateException("Unsupported type " + type);
        }
    }

    @Transient
    public Formatter createFormatter() {
        if (type == CommitCompletionType.SIMPLE) {
            return SimpleFormatter.instance;
        } else if (type == CommitCompletionType.PATTERN) {
            return RegExpFormatter.create(pattern);
        } else {
            throw new IllegalStateException("Unsupported type " + type);
        }
    }

    @Transient
    public String getPresentableText() {
        if (type == CommitCompletionType.SIMPLE) {
            return "Branch name";
        } else if (type == CommitCompletionType.PATTERN) {
            return pattern;
        } else {
            throw new IllegalStateException("Unsupported type " + type);
        }
    }

    public CommitCompletionConfig copy() {
        CommitCompletionConfig copy = new CommitCompletionConfig();
        copy.pattern = pattern;
        copy.type = type;
        copy.testInput = testInput;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommitCompletionConfig that = (CommitCompletionConfig) o;

        return new EqualsBuilder()
            .append(type, that.type)
            .append(pattern, that.pattern)
            .append(testInput, that.testInput)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pattern, testInput);
    }
}
