package org.akropia.gui.screen.editor;

import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Holds the tool chosen in the right panel; without one, a click only selects
 * the square.
 */
@Singleton
class MouseOperationManager {

    private MouseOperation currentMouseOperation;

    @Inject
    MouseOperationManager() {
    }

    void setOperation(final MouseOperation operation) {
        currentMouseOperation = operation;
    }

    Optional<MouseOperation> getMouseOperation() {
        return Optional.ofNullable(currentMouseOperation);
    }

}
