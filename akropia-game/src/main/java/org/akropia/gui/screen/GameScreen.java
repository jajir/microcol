package org.akropia.gui.screen;

import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.UpdatableLanguage;

/**
 * All screen should implement this interface. Merge features that screen should
 * support.
 */
public interface GameScreen
        extends ScreenLifeCycle, JavaFxComponent, UpdatableLanguage {

}
