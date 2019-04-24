package zielu.gittoolbox;

public final class FeatureToggles {
  private static final boolean ANNOTATION_LOCKS = false;

  private FeatureToggles() {
    //do nothing
  }

  public static boolean useAnnotationLocks() {
    return ANNOTATION_LOCKS;
  }
}
