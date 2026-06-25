package cz.wolverstone.agonia.packbranding.client.screen;

/**
 * The four screen corners a brand text can be anchored to.
 */
public enum Corner {
    TOP_LEFT(false, false),
    TOP_RIGHT(true, false),
    BOTTOM_LEFT(false, true),
    BOTTOM_RIGHT(true, true);

    /** Config key suffix, e.g. {@code TopLeft} -> key {@code mainMenuTopLeft}. */
    private final boolean right;
    private final boolean bottom;

    Corner(boolean right, boolean bottom) {
        this.right = right;
        this.bottom = bottom;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isBottom() {
        return bottom;
    }

    /** Suffix used to build config keys, e.g. {@code TopLeft}. */
    public String configSuffix() {
        return switch (this) {
            case TOP_LEFT -> "TopLeft";
            case TOP_RIGHT -> "TopRight";
            case BOTTOM_LEFT -> "BottomLeft";
            case BOTTOM_RIGHT -> "BottomRight";
        };
    }
}
