package zielu.gittoolbox;

import java.util.function.Supplier;
import javax.swing.Icon;

public enum IconHandle {
  REG_EXP_FORMATTER(() -> ResIcons.BranchViolet),
  SIMPLE_FORMATTER(() -> ResIcons.BranchOrange)
  ;

  private final Supplier<Icon> iconSupplier;

  IconHandle(Supplier<Icon> iconSupplier) {
    this.iconSupplier = iconSupplier;
  }

  public Icon getIcon() {
    return iconSupplier.get();
  }
}
