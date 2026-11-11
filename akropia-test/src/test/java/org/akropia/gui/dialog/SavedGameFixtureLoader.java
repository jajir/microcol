package org.akropia.gui.dialog;

/** Loads chooser-backed test fixtures after removal of the Import UI action. */
public final class SavedGameFixtureLoader {

    private SavedGameFixtureLoader() {
    }

    public static void load(final PersistingService service) {
        service.perform(new SavedGamesDialog.Choice(
                SavedGamesDialog.Action.IMPORT, null, null));
    }
}
