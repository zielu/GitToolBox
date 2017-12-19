package zielu.gittoolbox.util;

public final class Html {
  public static final String BR = "<br/>";
  public static final String HR = "<hr/>";

  private Html() {
    throw new IllegalStateException();
  }

  private static String surround(String text, String tag) {
    return String.format("<%2$s>%1$s</%2$s>", text, tag);
  }

  public static String link(String name, String text) {
    return "<a href=\"" + name + "\">" + text + "</a>";
  }

  public static String underline(String text) {
    return surround(text, "underline");
  }
}
