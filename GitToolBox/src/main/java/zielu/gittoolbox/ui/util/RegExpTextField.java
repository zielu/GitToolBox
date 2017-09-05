package zielu.gittoolbox.ui.util;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.ValidatingTextField;
import com.sun.media.jfxmediaimpl.MediaDisposer.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.DocumentEvent;
import org.apache.commons.lang.StringUtils;

public class RegExpTextField extends ValidatingTextField implements Disposable {
    private final List<Consumer<String>> textConsumers = new ArrayList<>();

    public RegExpTextField() {
        getMainComponent().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String text = getText();
                textConsumers.forEach(c -> c.accept(text));
            }
        });
    }

    public void addTextConsumer(Consumer<String> textConsumer) {
        textConsumers.add(textConsumer);
    }

    @Override
    protected String validateTextOnChange(String text, DocumentEvent e) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        try {
            Pattern.compile(text);
            return "";
        } catch (PatternSyntaxException exp) {
            return exp.getMessage();
        }
    }

    @Override
    public void dispose() {
        textConsumers.clear();
    }
}
