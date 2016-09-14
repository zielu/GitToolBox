package zielu.gittoolbox.util;

public enum Html {
    ;
    public static final String br = "<br/>";
    public static final String hr = "<hr/>";

    private static String surround(String text, String tag) {
        return String.format("<%2$s>%1$s</%2$s>", text, tag);
    }

    public static String link(String name, String text) {
        return "<a href=\"" + name + "\">" + text + "</a>";
    }

    public static String u(String text) {
        return surround(text, "u");
    }
}
