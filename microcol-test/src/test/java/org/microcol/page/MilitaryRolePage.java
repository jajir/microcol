package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.microcol.model.CombatDrawProbe;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Continental and royal soldiers in the combat review and after a defeat,
 * observed through the ordinary attack gestures.
 */
public final class MilitaryRolePage extends AbstractScreen {

    public MilitaryRolePage(final TestContext context) {
        super(context);
    }

    /**
     * Replace the running game by the military scenario in which France has
     * declared independence and its defender is a Continental soldier.
     */
    public void loadContinentalDefenderFixture() throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(
                Files.readString(Path.of(
                        "src/test/scenarios/F57-military-promotion.microcol")),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        for (final JsonElement entry : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = entry.getAsJsonObject();
            if (player.get("name").getAsString().equals("France")) {
                RebellionJson.declare(model, player);
            }
        }
        for (final JsonElement entry : model.getAsJsonArray("units")) {
            final JsonObject unit = entry.getAsJsonObject();
            if (unit.get("id").getAsInt() == 6) {
                unit.addProperty("militaryRole", "CONTINENTAL");
            }
        }
        final Path file = Files.createTempFile("military-continental-",
                ".microcol");
        try {
            Files.writeString(file, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(file.toFile());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Save the combat random state that makes the battle end as wanted for the
     * odds the unit would be quoted now.
     */
    public void chooseOutcome(final int attackerId, final Location target,
            final boolean attackerWins) throws Exception {
        final double chance = WaitForAsyncUtils.asyncFx(
                () -> ((UnitFreeColonist) getModel().getUnitById(attackerId))
                        .prepareAttack(target, true).orElseThrow()
                        .getCombatStrengths().orElseThrow()
                        .getAttackerWinProbability())
                .get(10, TimeUnit.SECONDS);
        CombatDrawProbe.choose(getModel(), chance, attackerWins);
    }

    public void verifyRoleReview(final String attackerStrength,
            final String defenderStrength, final String chance,
            final String notice) {
        assertEquals(attackerStrength,
                getLabeledById("fightAttackerStrength").getText());
        assertEquals(defenderStrength,
                getLabeledById("fightDefenderStrength").getText());
        assertTrue(getLabeledById("fightChance").getText().startsWith(chance));
        assertTrue(getLabeledById("fightChance").getText().contains(notice),
                "The review must say what a defeat costs");
    }

    /** The defeated soldier is gone, with the outcome announced. */
    public void verifyDestroyed(final int unitId, final String feedback)
            throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().tryGetUnitById(unitId).isEmpty());
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(getModel().tryGetUnitById(unitId).isEmpty(),
                "Unit " + unitId + " must not survive");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> getListOfNodes(".label")
                        .stream()
                        .filter(javafx.scene.control.Label.class::isInstance)
                        .map(node -> ((javafx.scene.control.Label) node)
                                .getText())
                        .anyMatch(text -> text.contains(feedback))).get());
    }

    public void verifyAbsent(final int unitId) {
        assertTrue(getModel().tryGetUnitById(unitId).isEmpty(),
                "Unit " + unitId + " must stay destroyed");
    }

    public void verifyArmed(final int unitId, final boolean expectedArmed) {
        assertEquals(expectedArmed,
                ((UnitFreeColonist) getModel().getUnitById(unitId))
                        .isHoldingGuns());
    }
}
