package zielu.gittoolbox.formatter;

public final class Formatted {
    public final String text;
    public final boolean matches;

    Formatted(String text, boolean matches) {
        this.text = text;
        this.matches = matches;
    }

    public boolean matches() {
        return matches;
    }
}
