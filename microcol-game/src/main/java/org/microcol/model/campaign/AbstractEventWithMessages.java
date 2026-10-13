package org.microcol.model.campaign;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Mission message identifiers; the UI owns translation and presentation.
 */
abstract class AbstractEventWithMessages {

    private final List<Missions> keys;

    AbstractEventWithMessages(final Missions... messageKeys) {
        keys = ImmutableList.copyOf(messageKeys);
    }

    public List<Missions> getMessages() {
        return keys;
    }
}
