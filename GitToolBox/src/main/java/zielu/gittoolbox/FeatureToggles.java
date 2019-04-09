package zielu.gittoolbox;

public final class FeatureToggles {
  private static final boolean NEW_BLAME = true;
  private static final boolean ANNOTATION_LOCKS = false;

  private FeatureToggles() {
    //do nothing
  }

  public static boolean useAnnotationLocks() {
    return ANNOTATION_LOCKS;
  }

  public static boolean useIncrementalBlame() {
    return NEW_BLAME;
  }

  public static boolean showBlameProgress() {
    return NEW_BLAME;
  }
}
