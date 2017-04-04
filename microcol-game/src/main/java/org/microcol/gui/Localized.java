package org.microcol.gui;

/**
 * All classes that implement this interface can easily access {@link Text}
 * class.
 */
public interface Localized {

	default Text getText() {
		return Text.INSTANCE;
	}

}