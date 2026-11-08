package org.akropia.model.campaign.po;

import java.io.File;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

/** A complete atomically written circular autosave. */
public final class TurnSnapshot {
    private final File file;
    private final JsonElement state;

    public TurnSnapshot(final File file, final JsonElement state) {
        this.file = Preconditions.checkNotNull(file);
        this.state = Preconditions.checkNotNull(state);
    }

    public File getFile() {
        return file;
    }

    public JsonElement getState() {
        return state;
    }
}
