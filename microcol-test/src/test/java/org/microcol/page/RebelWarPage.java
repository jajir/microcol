package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Colony;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Revolution battle modifiers (R-0178) and cause support (R-0188) in the
 * player's attack review. Fixtures are derived from F57, where the human Dutch
 * soldier (unit 0) stands at (18,10) beside Quebec (17,10) and the free French
 * unit 6 stands at (17,11). The Dutch have declared independence in every
 * fixture, so the soldier is a rebel the player controls. Only a human nation
 * can attack through the map, so the Crown is always the defender here.
 */
public final class RebelWarPage extends AbstractScreen {

    private static final File SOURCE = new File(
            "src/test/scenarios/F57-military-promotion.microcol");

    private static final int SOLDIER = 0;

    private static final int RESIDENT = 4;

    private static final int REGULAR = 6;

    /** Row of the tile (17,11) in the trees list, as in the F27 fixtures. */
    private static final int TREE_ROW = 11;

    /** Column of the tile (17,11) in the trees list, as in the F27 fixtures. */
    private static final int TREE_CELL = 16;

    private static final String CROWN = "Dutch's King";

    private static final int QUEBEC_ID = 0;

    /** Montreal, the second colony of F57, at (17,21). */
    private static final int MONTREAL_ID = 1;

    /** The only resident of Montreal, working its west field. */
    private static final int MONTREAL_RESIDENT = 5;

    /**
     * Row of the tile (17,22), Montreal's idle south field, in the tiles list:
     * the first entry is the column header, so row y is entry y.
     */
    private static final int MONTREAL_SEA_ROW = 22;

    /** Cell of column x = 17 in a row; columns are counted from 1. */
    private static final int MONTREAL_SEA_CELL = 16;

    public RebelWarPage(final TestContext context) {
        super(context);
    }

    /**
     * Write a fixture in which Quebec is held by the Dutch Crown, as a real
     * occupation leaves it: the colony and its only resident belong to the
     * Crown while the colonial affiliation is Dutch. The soldier is a
     * Continental of the rebel Dutch.
     *
     * @param name      required file name of this variant
     * @param delivered whether the soldier came with the intervention
     */
    public void prepareCrownColonyFixture(final String name,
            final boolean delivered) throws Exception {
        final JsonObject saved = scenario();
        final JsonObject model = saved.getAsJsonObject("model");
        holdMontreal(model);
        declareDutch(model, QUEBEC_ID);
        occupyQuebec(model, 0);
        makeContinental(unit(model, SOLDIER), delivered);
        write(name, saved);
    }

    /**
     * Write a fixture in which Quebec is held by the Dutch Crown, as in
     * {@link #prepareCrownColonyFixture}, with the given stored liberty. The
     * soldier stays an ordinary colonial soldier of the rebel Dutch, so the
     * rebel share of the colony is the exact fraction liberty / 200.
     *
     * @param name    required file name of this variant
     * @param liberty stored liberty of Quebec, from 0 to 200 for its one
     *                resident
     */
    public void prepareSupportFixture(final String name, final long liberty)
            throws Exception {
        final JsonObject saved = scenario();
        final JsonObject model = saved.getAsJsonObject("model");
        holdMontreal(model);
        declareDutch(model, QUEBEC_ID);
        occupyQuebec(model, liberty);
        write(name, saved);
    }

    /**
     * The Crown holds Quebec and its resident, and the colonial affiliation
     * stays Dutch with the given stored liberty.
     */
    private static void occupyQuebec(final JsonObject model,
            final long liberty) {
        final JsonObject quebec = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        quebec.addProperty("ownerName", CROWN);
        quebec.addProperty("lastColonialAffiliation", "Dutch");
        quebec.addProperty("liberty", liberty);
        unit(model, RESIDENT).addProperty("ownerId", CROWN);
    }

    /**
     * Write a fixture in which a regular of the Dutch Crown stands in the trees
     * of the countryside tile (17,11), next to the rebel soldier.
     *
     * @param name      required file name of this variant
     * @param delivered whether the soldier is a Continental that came with the
     *                  intervention instead of an ordinary colonial soldier
     */
    public void prepareCountrysideFixture(final String name,
            final boolean delivered) throws Exception {
        final JsonObject saved = scenario();
        final JsonObject model = saved.getAsJsonObject("model");
        holdMontreal(model);
        declareDutch(model);
        final JsonObject regular = unit(model, REGULAR);
        regular.addProperty("ownerId", CROWN);
        regular.addProperty("militaryRole", "ROYAL");
        if (delivered) {
            makeContinental(unit(model, SOLDIER), true);
        }
        final JsonArray trees = model.getAsJsonObject("map")
                .getAsJsonArray("trees");
        final String row = trees.get(TREE_ROW).getAsString();
        final String[] cells = row.substring(row.indexOf(':') + 1).split(",");
        cells[TREE_CELL] = "t";
        trees.set(TREE_ROW,
                new JsonPrimitive("row-0011:" + String.join(",", cells)));
        write(name, saved);
    }

    private static JsonObject scenario() throws Exception {
        return new Gson().fromJson(Files.readString(SOURCE.toPath()),
                JsonObject.class);
    }

    /**
     * The Dutch declared with the colonies they hold, plus the given colonies
     * that their Crown has taken since (R-0192).
     */
    private static void declareDutch(final JsonObject model,
            final int... capturedMembers) {
        for (final JsonElement entry : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = entry.getAsJsonObject();
            if (player.get("name").getAsString().equals("Dutch")) {
                RebellionJson.declare(model, player, capturedMembers);
            }
        }
    }

    /**
     * The rebels still hold Montreal, with its one resident, and its idle field
     * to the south is a sea square, so Montreal is a coastal port. A declared
     * rebellion without any held coastal colony is already lost (R-0192); this
     * one is not, so the player's battles go on.
     */
    private static void holdMontreal(final JsonObject model) {
        final JsonObject montreal = model.getAsJsonArray("colonies")
                .get(MONTREAL_ID).getAsJsonObject();
        montreal.addProperty("ownerName", "Dutch");
        montreal.addProperty("lastColonialAffiliation", "Dutch");
        unit(model, MONTREAL_RESIDENT).addProperty("ownerId", "Dutch");
        final JsonArray tiles = model.getAsJsonObject("map")
                .getAsJsonArray("tiles");
        final String row = tiles.get(MONTREAL_SEA_ROW).getAsString();
        final String[] cells = row.substring(row.indexOf(':') + 1).split(",");
        cells[MONTREAL_SEA_CELL] = "_";
        tiles.set(MONTREAL_SEA_ROW,
                new JsonPrimitive(row.substring(0, row.indexOf(':') + 1)
                        + String.join(",", cells)));
    }

    private static JsonObject unit(final JsonObject model, final int id) {
        for (final JsonElement entry : model.getAsJsonArray("units")) {
            if (entry.getAsJsonObject().get("id").getAsInt() == id) {
                return entry.getAsJsonObject();
            }
        }
        throw new IllegalStateException("No unit " + id + " in the fixture");
    }

    private static void makeContinental(final JsonObject unit,
            final boolean delivered) {
        unit.addProperty("militaryRole", "CONTINENTAL");
        if (delivered) {
            unit.addProperty("interventionDelivered", true);
        }
    }

    /** Write the fixture and make it the file the game loads. */
    private void write(final String name, final JsonObject saved)
            throws Exception {
        final Path directory = Path.of("target", "rebel-war");
        Files.createDirectories(directory);
        final File file = directory.resolve(name + ".microcol").toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    /** The attacker's strength lines, exactly as the model quotes them. */
    public void verifyAttackerStrength(final String expected) {
        assertEquals(expected,
                getLabeledById("fightAttackerStrength").getText());
    }

    /** The defender's strength lines, exactly as the model quotes them. */
    public void verifyDefenderStrength(final String expected) {
        assertEquals(expected,
                getLabeledById("fightDefenderStrength").getText());
    }

    /** The attacker's win chance, as the start of the chance line. */
    public void verifyWinChance(final String expectedStart) {
        assertTrue(
                getLabeledById("fightChance").getText()
                        .startsWith(expectedStart),
                getLabeledById("fightChance").getText());
    }

    /** Quebec is held by the given owner with the given number of residents. */
    public void verifyColonyHeldBy(final String expectedOwner,
            final int expectedResidents) {
        final Colony quebec = getModel().getColonyById(0).orElseThrow();
        assertEquals(expectedOwner, quebec.getOwner().getName());
        assertEquals(expectedResidents, quebec.getUnitsInsideColony().size());
    }

    /**
     * The attacker lost the colony battle: it survived without its muskets and
     * the Crown still holds Quebec with its one resident.
     */
    public void verifyLostColonyBattle(final int attackerId) throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> getModel()
                .getUnitById(attackerId).getActionPoints() == 0);
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(getModel().tryGetUnitById(attackerId).isPresent(),
                "Unit " + attackerId + " must survive the lost battle");
        assertFalse(((UnitFreeColonist) getModel().getUnitById(attackerId))
                .isHoldingGuns());
        verifyColonyHeldBy(CROWN, 1);
    }
}
