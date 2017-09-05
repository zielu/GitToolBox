package zielu.gittoolbox.ui.config;

import com.intellij.ui.DocumentAdapter;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.config.CommitCompletionConfig;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.ui.util.RegExpTextField;

public class GtPatternFormatterForm implements GtFormUi {
    private RegExpTextField commitCompletionPatternField;
    private JTextField commitCompletionPatternInput;
    private JTextField commitCompletionPatternOutput;
    private JLabel commitCompletionPatternStatus;
    private JPanel content;

    private CommitCompletionConfig config;
    private boolean updateEnabled;

    @Override
    public void init() {
        commitCompletionPatternField.addTextConsumer(text -> updateCommitCompletionOutput());
        commitCompletionPatternInput.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                updateCommitCompletionOutput();
            }
        });
    }

    private void updateCommitCompletionOutput() {
        if (updateEnabled) {
            updateCommitCompletionOutput(commitCompletionPatternField.getText(), commitCompletionPatternInput.getText());
        }
    }

    private void updateCommitCompletionOutput(String pattern, String testInput) {
        Formatted formatted = RegExpFormatter.create(pattern).format(testInput);
        commitCompletionPatternOutput.setText(formatted.text);
        boolean matches = formatted.matches;
        if (matches) {
            commitCompletionPatternStatus.setIcon(ResIcons.Ok);
            commitCompletionPatternStatus.setToolTipText(ResBundle.getString("commit.dialog.completion.pattern.output.matched.label"));
        } else {
            commitCompletionPatternStatus.setIcon(ResIcons.Warning);
            commitCompletionPatternStatus.setToolTipText(ResBundle.getString("commit.dialog.completion.pattern.output.not.matched.label"));
        }
        config.pattern = pattern;
        config.testInput = testInput;
    }

    public CommitCompletionConfig getCommitCompletionConfig() {
        return config;
    }

    public void setCommitCompletionConfig(CommitCompletionConfig config) {
        this.config = config;
    }

    @Override
    public JComponent getContent() {
        return content;
    }

    @Override
    public void afterStateSet() {
        updateEnabled = false;
        commitCompletionPatternField.setText(config.pattern);
        commitCompletionPatternInput.setText(config.testInput);
        updateEnabled = true;
        updateCommitCompletionOutput();
    }

    @Override
    public void dispose() {
        commitCompletionPatternField.dispose();
        config = null;
    }
}
