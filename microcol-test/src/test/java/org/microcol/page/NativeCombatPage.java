package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Human attacks on native braves through the real map gesture and the battle
 * review. The initial position is a current-schema fixture derived from a saved
 * scenario: one armed Dutch soldier is added and the saved combat random state
 * is chosen so the quoted battle ends as wanted. Nothing in the running model
 * is changed by this page.
 */
public final class NativeCombatPage extends AbstractScreen {

    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    /** Smallest distance of a chosen draw from the quoted odds. */
    private static final double DRAW_MARGIN = 1e-6;

    private int rememberedHomeId;
    private double rememberedAlarm;

    public NativeCombatPage(final TestContext context) {
        super(context);
    }

    /**
     * Load the scenario with one more armed Dutch soldier and the saved random
     * state that decides the battle against the quoted odds.
     *
     * @param source            scenario file with the native position
     * @param soldierId         unused unit ID of the new soldier
     * @param soldierAt         land square beside the braves
     * @param attackerWinChance odds the review is expected to quote
     * @param attackerWins      whether the saved draw lets the soldier win
     */
    public void loadFixture(final File source, final int soldierId,
            final Location soldierAt, final double attackerWinChance,
            final boolean attackerWins) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState",
                stateFor(attackerWinChance, attackerWins));
        model.getAsJsonArray("units").add(soldier(soldierId, soldierAt));
        final Path fixture = Files.createTempFile("native-combat-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject soldier(final int id, final Location at) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("id", id);
        unit.addProperty("type", "COLONIST");
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", true);
        unit.addProperty("mounted", false);
        return unit;
    }

    /**
     * First saved state whose first draw ends a battle with the quoted odds as
     * wanted, with the same generator as the model. A winning soldier must not
     * be promoted, so the second draw stays above the promotion chance.
     */
    static long stateFor(final double odds, final boolean attackerWins) {
        for (long state = 0;; state++) {
            final double first = draw(state, 1);
            if (Math.abs(first - odds) > DRAW_MARGIN
                    && (first < odds) == attackerWins
                    && (!attackerWins || draw(state, 2) >= 0.25)) {
                return state;
            }
        }
    }

    static double draw(final long savedState, final int ordinal) {
        long z = savedState + ordinal * GAMMA;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    /**
     * Remember the alarm of the settlement the Dutch know as a brave's home.
     */
    public void rememberHomeAlarm(final int braveId) {
        final Player dutch = getModel().getPlayerByName("Dutch");
        rememberedHomeId = getModel().getNativeBraveHomeKnownTo(dutch,
                getModel().getUnitById(braveId)).orElseThrow().getId();
        rememberedAlarm = homeAlarm();
    }

    private double homeAlarm() {
        final Player dutch = getModel().getPlayerByName("Dutch");
        return getModel().getNativeLocalAlarm(dutch,
                getModel().getNativeSettlementsKnownTo(dutch).stream().filter(
                        settlement -> settlement.getId() == rememberedHomeId)
                        .findFirst().orElseThrow());
    }

    /**
     * The review names both participants, needs no war declaration (natives
     * have no treaty), keeps the ordinary option to hide the advisor, and shows
     * the unit images of the model's units.
     */
    public void verifyReview(final String attacker, final String defender,
            final int attackerId, final int defenderId) {
        assertEquals(attacker, getLabeledById("fightAttacker").getText());
        assertEquals(defender, getLabeledById("fightDefender").getText());
        assertFalse(getLabeledById("fightDeclaration").isVisible());
        assertTrue(getLabeledById("fightHideAdvisor").isVisible());
        final org.microcol.gui.image.ImageProvider images = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        assertSame(images.getUnitImage(getModel().getUnitById(attackerId)),
                getNodeFinder().lookup("#fightAttackerImage")
                        .queryAs(javafx.scene.image.ImageView.class)
                        .getImage());
        assertSame(images.getUnitImage(getModel().getUnitById(defenderId)),
                getNodeFinder().lookup("#fightDefenderImage")
                        .queryAs(javafx.scene.image.ImageView.class)
                        .getImage());
    }

    /** The review explains the native loss fates and the anger effect. */
    public void verifyNativeNotes(final String loss, final String anger) {
        final String text = getLabeledById("fightChance").getText();
        assertTrue(text.contains(loss), text);
        assertTrue(text.contains(anger), text);
    }

    public void verifyDialogFits(final int width, final int height) {
        final javafx.stage.Window window = getButtonById("fightConfirm")
                .getScene().getWindow();
        assertTrue(window.getWidth() <= width,
                "Review width " + window.getWidth());
        assertTrue(window.getHeight() <= height,
                "Review height " + window.getHeight());
    }

    /**
     * The defeated brave is gone with its equipment, and the armed winner kept
     * its muskets, spent its movement and stands on the won square.
     */
    public void verifyBraveDestroyed(final int braveId, final int soldierId,
            final Location soldierAt) throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().getAllUnits().stream()
                        .noneMatch(unit -> unit.getId() == braveId));
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(getModel().getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == braveId));
        final UnitFreeColonist soldier = (UnitFreeColonist) getModel()
                .getUnitById(soldierId);
        assertEquals("Dutch", soldier.getOwner().getName());
        assertEquals(soldierAt, soldier.getLocation());
        assertEquals(0, soldier.getActionPoints());
        assertTrue(soldier.isHoldingGuns());
    }

    /**
     * The defeated soldier survives unarmed where it stood, and the winning
     * brave is unharmed in place; both have spent their movement.
     */
    public void verifySoldierLostMuskets(final int braveId,
            final Location braveAt, final int soldierId,
            final Location soldierAt) throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> !((UnitFreeColonist) getModel().getUnitById(soldierId))
                        .isHoldingGuns());
        GamePage.of(getContext()).waitWhileMoving();
        final UnitFreeColonist soldier = (UnitFreeColonist) getModel()
                .getUnitById(soldierId);
        assertEquals("Dutch", soldier.getOwner().getName());
        assertEquals(soldierAt, soldier.getLocation());
        assertEquals(0, soldier.getActionPoints());
        assertFalse(soldier.isHoldingGuns());
        assertFalse(soldier.isMounted());
        assertEquals(braveAt, getModel().getUnitById(braveId).getLocation());
        assertEquals(0, getModel().getUnitById(braveId).getActionPoints());
        assertTrue(getModel().getUnitById(braveId).getOwner().isNative());
    }

    /** Exactly this many committed native relation effects are recorded. */
    public void verifyRelationOutcomeCount(final int expected) {
        final List<Long> ids = getModel().save().getNativeRelationOutcomeIds();
        assertEquals(expected, ids == null ? 0 : ids.size());
    }

    /** The remembered home settlement's alarm rose by exactly this amount. */
    public void verifyHomeAlarmRaised(final double by) {
        assertEquals(rememberedAlarm + by, homeAlarm(), 1e-9);
    }

    /** Some visible label, such as the status bar, shows the result text. */
    public void verifyFeedback(final String text) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> getListOfNodes(".label")
                        .stream()
                        .filter(javafx.scene.control.Label.class::isInstance)
                        .map(node -> ((javafx.scene.control.Label) node)
                                .getText())
                        .anyMatch(value -> value.contains(text))).get());
        assertTrue(getListOfNodes(".label").stream()
                .filter(javafx.scene.control.Label.class::isInstance)
                .map(node -> ((javafx.scene.control.Label) node).getText())
                .anyMatch(value -> value.contains(text)));
    }
}
