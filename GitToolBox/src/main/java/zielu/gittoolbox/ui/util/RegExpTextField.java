package zielu.gittoolbox.ui.util;

import com.intellij.openapi.Disposable;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang.StringUtils;

import javax.swing.event.DocumentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpTextField extends JBTextField implements Disposable {
    private final List<BiConsumer<String, Optional<String>>> textConsumers = new ArrayList<>();

    public RegExpTextField() {
        getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String text = getText();
                Optional<String> error = validateTextOnChange(text);
                textConsumers.forEach(c -> c.accept(text, error));
            }
        });
    }

    public void addTextConsumer(BiConsumer<String, Optional<String>> textConsumer) {
        textConsumers.add(textConsumer);
    }

    private Optional<String> validateTextOnChange(String text) {
        if (StringUtils.isBlank(text)) {
            return Optional.empty();
        }
        try {
            Pattern.compile(text);
            return Optional.empty();
        } catch (PatternSyntaxException exp) {
            return Optional.of(exp.getMessage());
        }
    }

    @Override
    public void dispose() {
        textConsumers.clear();
    }
}
