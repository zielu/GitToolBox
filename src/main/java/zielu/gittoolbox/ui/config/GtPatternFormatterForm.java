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
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.ui.util.RegExpTextField;
import zielu.intellij.ui.GtFormUi;

public class GtPatternFormatterForm implements GtFormUi {
  private final Set<Consumer<String>> patternUpdates = new LinkedHashSet<>();

  private RegExpTextField patternField;
  private JTextField patternInput;
  private JTextField patternOutput;
  private JLabel patternMatchStatus;
  private JPanel content;
  private JLabel patternStatus;

  private GtPatternFormatterData data;
  private boolean updateEnabled;

  @Override
  public void init() {
    patternField.addTextConsumer((text, error) -> {
      updateCommitCompletionStatus(error);
      updateCommitCompletionOutput();
      patternUpdates.forEach(c -> c.accept(text));
    });
    patternInput.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        updateCommitCompletionOutput();
      }
    });
  }

  public void addPatternUpdate(Consumer<String> updateHandler) {
    patternUpdates.add(updateHandler);
  }

  private void updateCommitCompletionStatus(Optional<String> error) {
    if (error.isPresent()) {
      patternStatus.setIcon(ResIcons.getError());
      patternStatus.setToolTipText(error.get());
    } else {
      patternStatus.setIcon(ResIcons.getOk());
      patternStatus.setToolTipText(null);
    }
  }

  private void updateCommitCompletionOutput() {
    if (updateEnabled) {
      updateCommitCompletionOutput(patternField.getText(), patternInput.getText());
    }
  }

  private void updateCommitCompletionOutput(String pattern, String testInput) {
    Formatted formatted = RegExpFormatter.create(pattern).format(testInput);
    patternOutput.setText(formatted.getText());
    if (formatted.getMatches()) {
      patternMatchStatus.setIcon(ResIcons.getOk());
      patternMatchStatus.setToolTipText(getMatchedToolTip());
    } else {
      patternMatchStatus.setIcon(ResIcons.getWarning());
      patternMatchStatus.setToolTipText(getNotMatchedToolTip());
    }
    data.setPattern(pattern);
    data.setTestInput(StringUtils.trimToNull(testInput));
  }

  private String getMatchedToolTip() {
    return ResBundle.message("commit.dialog.completion.pattern.output.matched.label");
  }

  private String getNotMatchedToolTip() {
    return ResBundle.message("commit.dialog.completion.pattern.output.not.matched.label");
  }

  public void setData(GtPatternFormatterData data) {
    this.data = data;
  }

  @Override
  public JComponent getContent() {
    return content;
  }

  @Override
  public void afterStateSet() {
    updateEnabled = false;
    patternField.setText(data.getPattern());
    patternInput.setText(data.getTestInput());
    updateEnabled = true;
    updateCommitCompletionOutput();
  }

  @Override
  public void dispose() {
    Disposer.dispose(patternField);
    data = null;
    patternUpdates.clear();
  }
}
