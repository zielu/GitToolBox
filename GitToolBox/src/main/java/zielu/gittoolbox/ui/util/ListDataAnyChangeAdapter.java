package zielu.gittoolbox.ui.util;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public abstract class ListDataAnyChangeAdapter implements ListDataListener {
  @Override
  public final void intervalAdded(ListDataEvent e) {
    changed(e);
  }

  @Override
  public final void intervalRemoved(ListDataEvent e) {
    changed(e);
  }

  @Override
  public final void contentsChanged(ListDataEvent e) {
    changed(e);
  }

  public abstract void changed(ListDataEvent e);
}
