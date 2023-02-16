package zielu.gittoolbox.cache;

/**
 * Notification interface.
 * <p/>
 * Notification methods are called in following order:
 * <ol>
 *   <li>{@link #updated()}</li>
 * </ol>
 */
public interface VirtualFileRepoCacheListener {
  void updated();
}
