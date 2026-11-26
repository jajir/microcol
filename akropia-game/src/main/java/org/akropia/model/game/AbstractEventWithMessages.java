package org.akropia.model.game;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Game message identifiers; the UI owns translation and presentation.
 */
abstract class AbstractEventWithMessages {

    private final List<GameMessages> keys;

    AbstractEventWithMessages(final GameMessages... messageKeys) {
        keys = ImmutableList.copyOf(messageKeys);
    }

    public List<GameMessages> getMessages() {
        return keys;
    }
}
