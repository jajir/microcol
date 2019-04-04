package org.microcol.gui.util;

import javafx.scene.layout.Region;

/**
 * Declare that component contains JavaFX component which is available by here
 * defined method.
 * <p>
 * Generally it's better to don't extends from JavaFX components and instead of
 * it use delegate. Main reasons are:
 * </p>
 * <ul>
 * <li>It's easier to use when just method that should be use are
 * available.</li>
 * <li>Component doesn't contains hundred of unused methods.</li>
 * <li>It's easier to later refactor class.</li>
 * </ul>
 */
public interface JavaFxComponent {

    /**
     * Provide JavaFX content that should be used somewhere else.
     *
     * @return JavaFX component content.
     */
    Region getContent();
}
