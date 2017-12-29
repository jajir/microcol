package org.microcol.integration;

import java.util.Map;

import com.google.common.base.Preconditions;

import javafx.scene.image.Image;

public class TileDef {

	private final String prefix;

	private final Connector start;

	private final Connector end;

	private final ImageWrapper image;

	private TileDef(final String prefix, final Connector start, final Connector end, final ImageWrapper image) {
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

	public TileDef storeTo(final Map<String, Image> images) {
		Preconditions.checkNotNull(images);
		images.put(getCode(), image.get());
		return this;
	}

	public TileDef rotateRight(final String newPrefix) {
		return TileDef.of(newPrefix, start.rotateRight(), end.rotateRight(), image.getImageRotareRight());
	}

}
