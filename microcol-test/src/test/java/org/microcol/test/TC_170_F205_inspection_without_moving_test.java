package org.microcol.test;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.CoastalPortPage;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.InspectionPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Inspection without moving (R-0205) on the naval scenario. Clicking and the
 * keyboard selection key read the right panel of the Dutch frigate and of the
 * French galleon beside it. The galleon shows its number of occupied holds
 * only, with no passenger or goods named. Pressing on the frigate shows the
 * existing move-mode overlay, which must highlight exactly the squares the
 * model says the frigate can reach, and a cancelled preview clears it and
 * changes nothing. A removed selected unit is not disclosed after the square is
 * inspected again. The Czech panel fits the window. Save and Continue keep the
 * same facts. The model rules are checked in ForeignOccupiedHoldsTest, and the
 * entry cost, partial and zero movement and rejection paths in TC_87 and TC_38,
 * and the disband and colony paths in TC_104.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_170_F205_inspection_without_moving_test
        extends AbstractMicroColTest {

    /** Scenario with a Dutch frigate next to a French galleon in sight. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F55-naval-combat.microcol");

    private static final Location FRIGATE_AT = Location.of(20, 10);

    private static final int FRIGATE_ID = 0;

    private static final Location GALLEON_AT = Location.of(21, 10);

    private static final int GALLEON_ID = 6;

    /** Four goods holds and one passenger hold of the galleon. */
    private static final String HOLDS_EN = "Occupied holds: 5";

    private static final String HOLDS_CS = "Obsazené nákladní prostory: 5";

    /**
     * Squares around the frigate that the overlay check covers; its six
     * movement points carry it at most six squares.
     */
    private static final int RANGE_RADIUS = 8;

    private static final String[] HIDDEN_CONTENTS = { "With:", "No goods",
            "Silver", "Ore", "Fur", "Colonist" };

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_mouse_and_keyboard_inspection_show_occupied_holds_and_move_nothing()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        InspectionPage inspection = new InspectionPage(getContext());
        final Map<String, String> before = inspection.worldFacts();
        // Mouse inspection of the visible foreign galleon.
        game.panToTile(GALLEON_AT);
        game.selectTile(GALLEON_AT);
        // Only the number of occupied holds is shown, not the cargo inside.
        inspection.verifyOccupiedHoldsLine(GALLEON_ID, HOLDS_EN);
        inspection.verifyHiddenContentsNotShown(GALLEON_ID, HIDDEN_CONTENTS);
        inspection.captureRightPanel("inspection-foreign-holds-en.png");
        // Inspecting the foreign galleon moved no unit and changed no goods.
        inspection.verifyWorldFactsUnchanged(before);
        // Mouse inspection of the own frigate square.
        game.selectTile(FRIGATE_AT);
        inspection.verifyWorldFactsUnchanged(before);
        // The keyboard selection key reaches an own unit without moving it.
        inspection.pressTab();
        inspection.verifySelectedUnitOwner("Dutch");
        inspection.verifyWorldFactsUnchanged(before);
    }

    @Test
    @Tag("local")
    void TC_02_occupied_holds_survive_save_and_continue_without_revealing_contents()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        InspectionPage inspection = new InspectionPage(getContext());
        game.panToTile(GALLEON_AT);
        game.selectTile(GALLEON_AT);
        inspection.verifyOccupiedHoldsLine(GALLEON_ID, HOLDS_EN);
        final Map<String, String> before = inspection.worldFacts();
        // Save and Continue show the same count and keep every fact unchanged.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.panToTile(GALLEON_AT);
        game.selectTile(GALLEON_AT);
        inspection.verifyOccupiedHoldsLine(GALLEON_ID, HOLDS_EN);
        inspection.verifyHiddenContentsNotShown(GALLEON_ID, HIDDEN_CONTENTS);
        inspection.verifyWorldFactsUnchanged(before);
    }

    @Test
    @Tag("local")
    void TC_03_move_range_overlay_matches_the_model_and_cancel_changes_nothing()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        InspectionPage inspection = new InspectionPage(getContext());
        CoastalPortPage preview = new CoastalPortPage(getContext());
        FogPage map = new FogPage(getContext());
        game.panToTile(FRIGATE_AT);
        // The model names one square the frigate may simply move to.
        final Location step = inspection.plainMoveStepOf(FRIGATE_ID);
        final Map<String, String> before = inspection.worldFacts();
        preview.rememberWorld();
        // Pressing on the own frigate and pointing at that square starts the
        // move preview without moving anything.
        map.preview(FRIGATE_AT, step);
        preview.verifyPreview(step, "MOVE");
        // The map highlights every square the model says the frigate can reach
        // this turn, and no other square: not its own, not land, not squares
        // beyond its movement.
        inspection.verifyRangeOverlayMatchesModel(FRIGATE_ID, RANGE_RADIUS);
        // Escape cancels the preview: the range disappears and no unit, order,
        // movement, hold, gold or year changed.
        preview.cancelPreview();
        preview.verifyPreviewCanceled();
        inspection.verifyRangeOverlayCleared(FRIGATE_ID, RANGE_RADIUS);
        preview.verifyWorldUnchanged();
        inspection.verifyWorldFactsUnchanged(before);
    }

    @Test
    @Tag("local")
    void TC_04_removed_selected_unit_is_not_disclosed_after_inspection_again()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        InspectionPage inspection = new InspectionPage(getContext());
        game.panToTile(FRIGATE_AT);
        game.selectTile(FRIGATE_AT);
        inspection.verifySelectedUnitId(FRIGATE_ID);
        inspection.verifyUnitPanelPresent(FRIGATE_ID, true);
        // The frigate is removed live while its panel may still be shown.
        inspection.removeUnitWhileShown(FRIGATE_ID);
        // Inspecting the square again rebuilds the panel from live facts only.
        game.selectTile(FRIGATE_AT);
        inspection.verifyUnitPanelPresent(FRIGATE_ID, false);
        inspection.verifyStaleSelectionNotDisclosed(FRIGATE_ID);
        // Save and Continue keep the frigate removed.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        game.panToTile(FRIGATE_AT);
        game.selectTile(FRIGATE_AT);
        inspection.verifyUnitPanelPresent(FRIGATE_ID, false);
        inspection.verifyStaleSelectionNotDisclosed(FRIGATE_ID);
    }

    @Test
    @Tag("local")
    void TC_05_czech_panel_fits_and_states_occupancy() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        GamePage game = settings.goBack().loadGame();
        InspectionPage inspection = new InspectionPage(getContext());
        final Map<String, String> before = inspection.worldFacts();
        // The foreign galleon shows its occupied holds in Czech and fits.
        game.panToTile(GALLEON_AT);
        game.selectTile(GALLEON_AT);
        inspection.verifyOccupiedHoldsLine(GALLEON_ID, HOLDS_CS);
        inspection.verifyRightPanelFitsWindow();
        inspection.captureRightPanel("inspection-foreign-holds-cs.png");
        // The own frigate's panel fits as well, and nothing changed.
        game.selectTile(FRIGATE_AT);
        inspection.verifySelectedUnitId(FRIGATE_ID);
        inspection.verifyRightPanelFitsWindow();
        inspection.captureRightPanel("inspection-own-unit-cs.png");
        inspection.verifyWorldFactsUnchanged(before);
    }

}
