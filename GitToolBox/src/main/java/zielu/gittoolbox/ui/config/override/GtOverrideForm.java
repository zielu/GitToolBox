package zielu.gittoolbox.ui.config.override;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
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
  private JButton appliedAutoFetchEnabled;
  private JButton appliedAutoFetchOnBranchSwitchEnabled;

  private List<String> appliedAutoFetchEnabledPaths = Collections.emptyList();
  private List<String> appliedAutoFetchOnBranchSwitchEnabledPaths = Collections.emptyList();

  public GtOverrideForm() {
    appliedAutoFetchOnBranchSwitchEnabled.addActionListener(e -> showAppliedAutoFetchEnabledOnBranchSwitch());
    appliedAutoFetchEnabled.addActionListener(e -> showAppliedAutoFetchEnabled());
  }

  @Override
  public void init() {
    autoFetchEnabledOverride.addItemListener(e -> onAutoFetchEnabledOverride());
    autoFetchOnBranchSwitchOverride.addItemListener(e -> onAutoFetchOnBranchSwitchEnabledOverride());
  }

  private void onAutoFetchEnabledOverride() {
    autoFetchEnabled.setEnabled(autoFetchEnabledOverride.isSelected());
  }

  private void onAutoFetchOnBranchSwitchEnabledOverride() {
    autoFetchOnBranchSwitch.setEnabled(autoFetchOnBranchSwitchOverride.isSelected());
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

  boolean getAutoFetchOnBranchSwitchEnabledOverride() {
    return autoFetchOnBranchSwitchOverride.isSelected();
  }

  void setAutoFetchOnBranchSwitchEnabledOverride(boolean enabledOverride) {
    autoFetchOnBranchSwitchOverride.setSelected(enabledOverride);
  }

  boolean getAutoFetchOnBranchSwitchEnabled() {
    return autoFetchOnBranchSwitch.isSelected();
  }

  void setAutoFetchOnBranchSwitchEnabled(boolean enabled) {
    autoFetchOnBranchSwitch.setSelected(enabled);
  }

  public void setAppliedAutoFetchEnabledPaths(List<String> appliedAutoFetchEnabledPaths) {
    this.appliedAutoFetchEnabledPaths = new ArrayList<>(appliedAutoFetchEnabledPaths);
  }

  public void setAppliedAutoFetchOnBranchSwitchEnabledPaths(List<String> appliedAutoFetchOnBranchSwitchEnabledPaths) {
    this.appliedAutoFetchOnBranchSwitchEnabledPaths = new ArrayList<>(appliedAutoFetchOnBranchSwitchEnabledPaths);
  }

  private void showAppliedAutoFetchEnabled() {
    showApplied(appliedAutoFetchEnabledPaths);
  }

  private void showAppliedAutoFetchEnabledOnBranchSwitch() {
    showApplied(appliedAutoFetchOnBranchSwitchEnabledPaths);
  }

  private void showApplied(List<String> paths) {
    AppliedProjectsDialog dialog = new AppliedProjectsDialog(root);
    dialog.setAppliedPaths(paths);
    dialog.show();
  }

  @Override
  public void afterStateSet() {
    onAutoFetchEnabledOverride();
    onAutoFetchOnBranchSwitchEnabledOverride();
  }

  @Override
  public void dispose() {

  }
}
