package zielu.gittoolbox;

import java.util.function.Supplier;
import javax.swing.Icon;

public enum IconHandle {
  REG_EXP_FORMATTER(ResIcons::getBranchViolet),
  SIMPLE_FORMATTER(ResIcons::getBranchOrange)
  ;

  private final Supplier<Icon> iconSupplier;

  IconHandle(Supplier<Icon> iconSupplier) {
    this.iconSupplier = iconSupplier;
  }

  public Icon getIcon() {
    return iconSupplier.get();
  }
}
