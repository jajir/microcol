package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;

/**
 * Opens the religion report (R-0199) through the statistics screen. Colony rows
 * are addressed by stable colony IDs; the verify methods read the shown text,
 * and the actions only press the controls or replace the saved fixture.
 */
public final class ReligionReportPage extends AbstractScreen {

    private static final String FIXTURE = "src/test/scenarios/F58-religious-production.microcol";

    public ReligionReportPage(final TestContext context) {
        super(context);
    }

    /** Opens statistics and the religion tab with the mouse. */
    public void open() {
        new FreePlayAuditPage(getContext()).openStatistics();
        clickOnStatisticsTab("religionTab");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Opens statistics and reaches the religion tab with Tab and arrow keys:
     * Tab enters the toggle group and the arrow keys select its other members.
     */
    public void openWithKeyboard() {
        new FreePlayAuditPage(getContext()).openStatistics();
        final ToggleButton religion = religionTab();
        focusWithKeyboard((Node) religion.getToggleGroup().getSelectedToggle());
        for (int i = 0; i < religion.getToggleGroup().getToggles().size()
                && !religion.isSelected(); i++) {
            getRobot().push(KeyCode.RIGHT);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!religion.isSelected()) {
            throw new IllegalStateException(
                    "Religion tab was not selected by keyboard");
        }
    }

    /** Closes the report with the existing Escape path of the statistics. */
    public GamePage closeWithEscape() {
        return new FreePlayAuditPage(getContext()).closeWithEscape();
    }

    /**
     * Replaces the saved game with the F58 fixture whose Dutch nation holds the
     * given crosses. Everything else of the fixture stays as saved.
     */
    public void loadCrossFixture(final int crosses) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson
                .fromJson(Files.readString(Path.of(FIXTURE)), JsonObject.class);
        for (final JsonElement entry : game.getAsJsonObject("model")
                .getAsJsonObject("gameManager").getAsJsonArray("players")) {
            final JsonObject player = entry.getAsJsonObject();
            if (player.get("name").getAsString().equals("Dutch")) {
                player.getAsJsonObject("recruitment").addProperty("crosses",
                        crosses);
            }
        }
        final Path file = Files.createTempFile("religion-report-", ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private ToggleButton religionTab() {
        return getNodeFinder().lookup("#religionTab")
                .queryAs(ToggleButton.class);
    }

    private void focusWithKeyboard(final Node node) {
        for (int i = 0; i < 40 && !node.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!node.isFocused()) {
            throw new IllegalStateException(
                    "Statistics control is not keyboard reachable: "
                            + node.getId());
        }
    }

    private String text(final String id) {
        return getNodeFinder().lookup("#" + id).queryAs(Label.class).getText();
    }

    public void verifyTabSelected() {
        assertTrue(religionTab().isSelected(), "Religion tab selected");
    }

    /** Keyboard arrival leaves the religion tab both selected and focused. */
    public void verifyTabSelectedAndFocused() {
        assertTrue(religionTab().isSelected(), "Religion tab selected");
        assertTrue(religionTab().isFocused(), "Religion tab focused");
    }

    public void verifyNationalOutput(final int expected) {
        assertEquals("National output: " + expected + " crosses per turn",
                text("religionNational"), "National output");
    }

    /** The colony row names the colony and its output at the next update. */
    public void verifyColonyOutput(final int colonyId, final String name,
            final int crosses) {
        assertEquals("Colony " + name + ": " + crosses + " crosses per turn",
                text("religionColony-" + colonyId), "Output of " + name);
    }

    public void verifyAccumulated(final int expected) {
        assertEquals("Accumulated crosses: " + expected,
                text("religionAccumulated"), "Accumulated crosses");
    }

    /** The threshold and the crosses still missing to it. */
    public void verifyTarget(final int target, final int remaining) {
        assertEquals(
                "Next immigrant at " + target + " crosses; " + remaining
                        + " more needed",
                text("religionTarget"), "Next immigrant");
    }

    /**
     * The same figures read from the Czech labels. The fixture has one colony
     * that produces crosses, so the national row repeats its output.
     */
    public void verifyCzechLabels(final int colonyId, final String name,
            final int output, final int accumulated, final int target,
            final int remaining, final String immigration) {
        assertEquals("Kolonie " + name + " — počet křížů za tah: " + output,
                text("religionColony-" + colonyId), "Czech colony output");
        assertEquals("Počet křížů za tah celkem: " + output,
                text("religionNational"), "Czech national output");
        assertEquals("Nasbírané kříže: " + accumulated,
                text("religionAccumulated"), "Czech accumulated crosses");
        assertEquals(
                "Další přistěhovalec při " + target + " křížích; zbývá "
                        + remaining,
                text("religionTarget"), "Czech next immigrant");
        assertEquals(immigration, text("religionImmigration"),
                "Czech earned immigration state");
    }

    /** The shown earned state, compared as the exact sentence. */
    public void verifyImmigration(final String expected) {
        assertEquals(expected, text("religionImmigration"),
                "Earned immigration state");
    }
}
