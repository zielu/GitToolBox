package zielu.gittoolbox.ui.config;

import com.intellij.openapi.util.Disposer;
import com.intellij.ui.DocumentAdapter;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import org.apache.commons.lang.StringUtils;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.config.CommitCompletionConfig;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.ui.util.RegExpTextField;
import zielu.intellij.ui.GtFormUi;

public class GtPatternFormatterForm implements GtFormUi {
  private final Set<Consumer<String>> patternUpdates = new LinkedHashSet<>();

  private RegExpTextField commitCompletionPatternField;
  private JTextField commitCompletionPatternInput;
  private JTextField commitCompletionPatternOutput;
  private JLabel commitCompletionPatternMatchStatus;
  private JPanel content;
  private JLabel commitCompletionPatternStatus;

  private CommitCompletionConfig config;
  private boolean updateEnabled;

  @Override
  public void init() {
    commitCompletionPatternField.addTextConsumer((text, error) -> {
      updateCommitCompletionStatus(error);
      updateCommitCompletionOutput();
      patternUpdates.forEach(c -> c.accept(text));
    });
    commitCompletionPatternInput.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateCommitCompletionOutput();
      }
    });
  }

  public void addPatternUpdate(Consumer<String> updateHandler) {
    patternUpdates.add(updateHandler);
  }

  private void updateCommitCompletionStatus(Optional<String> error) {
    if (error.isPresent()) {
      commitCompletionPatternStatus.setIcon(ResIcons.getError());
      commitCompletionPatternStatus.setToolTipText(error.get());
    } else {
      commitCompletionPatternStatus.setIcon(ResIcons.getOk());
      commitCompletionPatternStatus.setToolTipText(null);
    }
  }

  private void updateCommitCompletionOutput() {
    if (updateEnabled) {
      updateCommitCompletionOutput(commitCompletionPatternField.getText(), commitCompletionPatternInput.getText());
    }
  }

  private void updateCommitCompletionOutput(String pattern, String testInput) {
    Formatted formatted = RegExpFormatter.create(pattern).format(testInput);
    commitCompletionPatternOutput.setText(formatted.getText());
    if (formatted.getMatches()) {
      commitCompletionPatternMatchStatus.setIcon(ResIcons.getOk());
      commitCompletionPatternMatchStatus.setToolTipText(getMatchedToolTip());
    } else {
      commitCompletionPatternMatchStatus.setIcon(ResIcons.getWarning());
      commitCompletionPatternMatchStatus.setToolTipText(getNotMatchedToolTip());
    }
    config.pattern = pattern;
    config.testInput = StringUtils.trimToNull(testInput);
  }

  private String getMatchedToolTip() {
    return ResBundle.message("commit.dialog.completion.pattern.output.matched.label");
  }

  private String getNotMatchedToolTip() {
    return ResBundle.message("commit.dialog.completion.pattern.output.not.matched.label");
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
    Disposer.dispose(commitCompletionPatternField);
    config = null;
    patternUpdates.clear();
  }
}
