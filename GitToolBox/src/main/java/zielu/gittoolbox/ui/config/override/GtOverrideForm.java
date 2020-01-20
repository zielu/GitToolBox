package zielu.gittoolbox.ui.config.override;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import zielu.intellij.ui.GtFormUi;

public class GtOverrideForm implements GtFormUi {
  private JPanel root;
  private JCheckBox autoFetchEnabledOverride;
  private JCheckBox autoFetchEnabled;
  private JCheckBox autoFetchOnBranchSwitchOverride;
  private JCheckBox autoFetchOnBranchSwitch;

  @Override
  public void init() {
    autoFetchEnabledOverride.addItemListener(e -> onAutoFetchEnabledOverride());
  }

  private void onAutoFetchEnabledOverride() {
    autoFetchEnabled.setEnabled(autoFetchEnabledOverride.isSelected());
  }

  @Override
  public JComponent getContent() {
    return root;
  }

  boolean getAutoFetchEnabledOverride() {
    return autoFetchEnabledOverride.isSelected();
  }

  void setAutoFetchEnabledOverride(boolean enabledOverride) {
    autoFetchEnabledOverride.setSelected(enabledOverride);
  }

  boolean getAutoFetchEnabled() {
    return autoFetchEnabled.isSelected();
  }

  void setAutoFetchEnabled(boolean enabled) {
    autoFetchEnabled.setSelected(enabled);
  }

  @Override
  public void afterStateSet() {

  }

  @Override
  public void dispose() {

  }
}
