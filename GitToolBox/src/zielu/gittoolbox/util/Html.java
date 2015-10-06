package zielu.gittoolbox.util;

public enum Html {
    ;
    public static final String br = "<br/>";

    public static String link(String name, String text) {
        return "<a href=\"" + name + "\">" + text + "</a>";
    }
}
