package org.akropia.model.campaign.po;

import java.io.File;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

/**
 * The gameplay state that a successful save or a validated load made
 * recoverable, with the file holding it (R-0387). The state counts as saved
 * only while that file still exists unchanged; a missing, replaced or rewritten
 * file never protects the current game.
 */
public final class SaveBaseline {

    private final File file;

    private final long length;

    private final long modified;

    private final JsonElement state;

    private SaveBaseline(final File file, final JsonElement state) {
        this.file = file.getAbsoluteFile();
        this.state = state;
        length = this.file.length();
        modified = this.file.lastModified();
    }

    /**
     * @param file  required file just written or loaded
     * @param state required gameplay state the file holds
     * @return baseline of the file as it is now
     */
    public static SaveBaseline of(final File file, final JsonElement state) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(state);
        Preconditions.checkArgument(file.isFile(), "Saved file is missing");
        return new SaveBaseline(file, state.deepCopy());
    }

    /**
     * @return the file holding the saved state
     */
    public File getFile() {
        return file;
    }

    /**
     * @return whether the file still exists exactly as saved or loaded
     */
    public boolean isFileAvailable() {
        return file.isFile() && file.length() == length
                && file.lastModified() == modified;
    }

    /**
     * @param current required gameplay state of the live game
     * @return whether leaving would lose nothing: the same gameplay state is
     *         still held by the unchanged file
     */
    public boolean covers(final JsonElement current) {
        Preconditions.checkNotNull(current);
        return isFileAvailable() && state.equals(current);
    }
}
