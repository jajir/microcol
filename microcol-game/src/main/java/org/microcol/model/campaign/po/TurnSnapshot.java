package org.microcol.model.campaign.po;

import java.io.File;
import java.util.Optional;

import org.microcol.model.MicroColException;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

/**
 * Outcome of one successfully written turn snapshot (R-0388). The snapshot
 * itself is complete; copying it into recovery slots or removing old snapshots
 * may still have failed.
 */
public final class TurnSnapshot {

    private final File file;

    private final JsonElement state;

    private final MicroColException slotFailure;

    private final boolean pruned;

    public TurnSnapshot(final File file, final JsonElement state,
            final MicroColException slotFailure, final boolean pruned) {
        this.file = Preconditions.checkNotNull(file);
        this.state = Preconditions.checkNotNull(state);
        this.slotFailure = slotFailure;
        this.pruned = pruned;
    }

    /**
     * @return the new snapshot file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the gameplay state the snapshot holds
     */
    public JsonElement getState() {
        return state;
    }

    /**
     * @return why a recovery slot was not replaced, if it was not
     */
    public Optional<MicroColException> getSlotFailure() {
        return Optional.ofNullable(slotFailure);
    }

    /**
     * @return <code>false</code> when older snapshots beyond the limit remain
     *         for a later cleanup
     */
    public boolean isPruned() {
        return pruned;
    }
}
