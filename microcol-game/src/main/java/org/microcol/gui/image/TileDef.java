package org.microcol.gui.image;

import com.google.common.base.Preconditions;

public final class TileDef {

    private final String prefix;

    private final Connector start;

    private final Connector end;

    private final ImageWrapper image;

    private TileDef(final String prefix, final Connector start, final Connector end,
            final ImageWrapper image) {
        this.prefix = Preconditions.checkNotNull(prefix);
        this.start = Preconditions.checkNotNull(start);
        this.end = Preconditions.checkNotNull(end);
        this.image = Preconditions.checkNotNull(image);
    }

    public static TileDef of(final String prefix, final Connector start, final Connector end,
            final ImageWrapper image) {
        return new TileDef(prefix, start, end, image);
    }

    public String getCode() {
        return prefix + start.get() + end.get();
    }

    public TileDef storeTo(final ImageCache imageCache) {
        Preconditions.checkNotNull(imageCache);
        imageCache.registerImage(getCode(), image.get());
        return this;
    }

    public TileDef rotateRight(final String newPrefix) {
        return TileDef.of(newPrefix, start.rotateRight(), end.rotateRight(),
                image.getImageRotareRight());
    }

}
