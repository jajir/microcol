package org.akropia.model.generation;

/** Player-facing map sizes; width is three halves of height. */
public enum GeneratedMapSize {
    SMALL(90, 60), MEDIUM(120, 80), LARGE(150, 100), HUGE(180, 120);

    private final int width;
    private final int height;

    GeneratedMapSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
