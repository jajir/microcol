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
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Defense of Quebec in the player's attack review: the colonists as one group,
 * the Paul Revere emergency militiaman and the captured colony. Fixture F68 is
 * derived from F27: a Dutch soldier (unit 0) stands next to Quebec, whose only
 * resident (unit 4) works a field and whose French garrison soldier stands
 * outside the colony tile.
 */
public final class ColonyDefensePage extends AbstractScreen {
    public static final int SOLDIER = 0;
    public static final int RESIDENT = 4;
    private static final int GARRISON = 6;
    private static final int SECOND_RESIDENT = 8;
    private static final File SOURCE = new File(
            "src/test/scenarios/F27-attack-eligibility.microcol");
    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    public ColonyDefensePage(final TestContext context) {
        super(context);
    }

    /**
     * Write F68 and make it the file the game loads.
     *
     * @param name           required file name of this variant
     * @param revere         whether France has recruited Paul Revere
     * @param secondResident whether a second French worker staffs the town hall
     * @param liberty        liberty stored in Quebec
     */
    public void prepareFixture(final String name, final boolean revere,
            final boolean secondResident, final long liberty) throws Exception {
        write(name, revere, secondResident ? 1 : 0, false, false, liberty);
    }

    /**
     * F68 for the independent attack on a field worker: the Dutch soldier
     * stands at (17,11) next to the field of unit 4 at (16,10) and the French
     * garrison soldier has taken the soldier's former square; a second French
     * worker keeps the colony populated.
     */
    public void prepareFieldFixture(final String name, final long liberty)
            throws Exception {
        write(name, false, 1, false, true, liberty);
    }

    /**
     * F68 for a fortified colony of three residents with Paul Revere: a
     * stockade protects it, so a voluntary exit of any resident is refused.
     */
    public void prepareFortifiedFixture(final String name, final long liberty)
            throws Exception {
        write(name, true, 2, true, false, liberty);
    }

    private void write(final String name, final boolean revere,
            final int extraResidents, final boolean stockade,
            final boolean swapSoldiers, final long liberty) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject colony = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        colony.addProperty("liberty", liberty);
        if (revere) {
            for (final JsonElement element : model
                    .getAsJsonObject("gameManager").getAsJsonArray("players")) {
                final JsonObject player = element.getAsJsonObject();
                if ("France".equals(player.get("name").getAsString())) {
                    final JsonArray members = new JsonArray();
                    members.add("PAUL_REVERE");
                    player.getAsJsonObject("congress").add("members", members);
                }
            }
        }
        final JsonArray units = model.getAsJsonArray("units");
        for (int index = 0; index < extraResidents; index++) {
            JsonObject worker = null;
            for (final JsonElement element : units) {
                if (element.getAsJsonObject().get("id")
                        .getAsInt() == RESIDENT) {
                    worker = element.getAsJsonObject().deepCopy();
                }
            }
            worker.addProperty("id", SECOND_RESIDENT + index);
            worker.remove("placeColonyField");
            worker.add("placeConstructionSlot", new JsonObject());
            units.add(worker);
            for (final JsonElement element : colony
                    .getAsJsonArray("constructions")) {
                final JsonObject construction = element.getAsJsonObject();
                if ("TOWN_HALL"
                        .equals(construction.get("type").getAsString())) {
                    construction.getAsJsonArray("slots").get(index)
                            .getAsJsonObject()
                            .addProperty("workerId", SECOND_RESIDENT + index);
                }
            }
        }
        if (stockade) {
            final JsonObject defense = new JsonObject();
            defense.addProperty("type", "STOCKADE");
            defense.add("slots", new JsonArray());
            colony.getAsJsonArray("constructions").add(defense);
        }
        if (swapSoldiers) {
            for (final JsonElement element : units) {
                final JsonObject unit = element.getAsJsonObject();
                final int id = unit.get("id").getAsInt();
                if (id == SOLDIER || id == GARRISON) {
                    final JsonObject at = unit.getAsJsonObject("placeMap")
                            .getAsJsonObject("location");
                    at.addProperty("x", id == SOLDIER ? 17 : 18);
                    at.addProperty("y", id == SOLDIER ? 11 : 10);
                }
            }
        }
        final Path directory = Path.of("target", "f68");
        Files.createDirectories(directory);
        final File file = directory.resolve(name + ".microcol").toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    private static double draw(final long savedState, final int ordinal) {
        long z = savedState + ordinal * GAMMA;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    /**
     * Choose the saved combat state before the player confirms: a draw far
     * below any quoted chance wins for the attacker, a draw above 0.99 loses,
     * and the second draw never promotes the winner.
     */
    public void chooseColonyOutcome(final boolean attackerWins)
            throws Exception {
        long state = 0;
        while (!(attackerWins ? draw(state, 1) < 0.3 : draw(state, 1) > 0.99)
                || draw(state, 2) < 0.25) {
            state++;
        }
        final java.lang.reflect.Field random = Model.class
                .getDeclaredField("combatRandomState");
        random.setAccessible(true);
        random.setLong(getModel(), state);
    }

    /** Move the French garrison soldier onto the colony tile. */
    public void garrisonColonyTile() {
        getRobot().interact(() -> getModel().getUnitById(6)
                .placeToLocation(Location.of(17, 10)));
    }

    private boolean hasLabel(final String text) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> getListOfNodes(".label").stream()
                .filter(javafx.scene.control.Label.class::isInstance)
                .map(node -> ((javafx.scene.control.Label) node).getText())
                .anyMatch(label -> label.contains(text))).get();
    }

    private void waitForFeedback(final String text) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> hasLabel(text));
    }

    /** Close the notice every captured colony shows. */
    public void closeCapturedColonyMessage() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .asyncFx(() -> isCssIdVisible("buttonOk")).get());
        clickOnButtonWithId("buttonOk");
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    private void waitForBattle() throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getModel().getUnitById(SOLDIER).getActionPoints() == 0);
        GamePage.of(getContext()).waitWhileMoving();
    }

    public void verifyColonyReview(final String defender, final String strength,
            final String note) {
        assertEquals(defender, getLabeledById("fightDefender").getText());
        assertTrue(getLabeledById("fightDefenderStrength").getText()
                .startsWith(strength));
        assertTrue(getLabeledById("fightChance").getText().contains(note));
        assertTrue(getNodeFinder().lookup("#fightDefenderImage")
                .queryAs(javafx.scene.image.ImageView.class)
                .getImage() != null);
    }

    /** The colony changed hands with all its workers and keeps its identity. */
    public void verifyColonyCaptured(final String owner, final int residents,
            final long liberty, final Location soldierAt, final String feedback)
            throws Exception {
        waitForBattle();
        final Colony colony = colony();
        assertEquals("Quebec", colony.getName());
        assertEquals(owner, colony.getOwner().getName());
        assertEquals(owner, colony.getLastColonialAffiliation().getName());
        assertEquals(residents, colony.getUnitsInsideColony().size());
        for (final Unit resident : colony.getUnitsInsideColony()) {
            assertEquals(owner, resident.getOwner().getName());
        }
        assertEquals(liberty, colony.getLiberty());
        assertEquals(soldierAt, getModel().getUnitById(SOLDIER).getLocation());
        if (feedback != null) {
            waitForFeedback(feedback);
            assertTrue(hasLabel(feedback));
        }
    }

    /** The colony stays French and the losing attacker is unarmed. */
    public void verifyColonyHeld(final int residents, final long liberty,
            final int musketsInWarehouse, final Location soldierAt,
            final String feedback) throws Exception {
        waitForBattle();
        final Colony colony = colony();
        assertEquals("France", colony.getOwner().getName());
        assertEquals(residents, colony.getUnitsInsideColony().size());
        assertEquals(liberty, colony.getLiberty());
        assertEquals(musketsInWarehouse,
                colony.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        final UnitFreeColonist soldier = (UnitFreeColonist) getModel()
                .getUnitById(SOLDIER);
        assertFalse(soldier.isHoldingGuns());
        assertEquals(soldierAt, soldier.getLocation());
        if (feedback != null) {
            waitForFeedback(feedback);
            assertTrue(hasLabel(feedback));
        }
    }

    /**
     * The emergency militiaman kept its muskets and stands either on the colony
     * tile or in its job.
     */
    public void verifyMilitia(final int unitId, final boolean outside) {
        final UnitFreeColonist militia = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        assertTrue(militia.isHoldingGuns());
        assertEquals("France", militia.getOwner().getName());
        assertEquals(outside, militia.isAtPlaceLocation());
        assertEquals(!outside, militia.isAtPlaceColonyField()
                || militia.isAtPlaceConstructionSlot());
        if (outside) {
            assertEquals(Location.of(17, 10), militia.getLocation());
        }
    }

    /**
     * Only the attacked field worker changed hands: it stands on the capturer's
     * square, the colony stays French with its other jobs.
     */
    public void verifyWorkerCaptured(final int workerId, final Location at,
            final int remainingResidents, final long liberty,
            final String feedback) throws Exception {
        waitForBattle();
        final Unit worker = getModel().getUnitById(workerId);
        assertEquals("Dutch", worker.getOwner().getName());
        assertTrue(worker.isAtPlaceLocation());
        assertEquals(at, worker.getLocation());
        final Colony colony = colony();
        assertEquals("France", colony.getOwner().getName());
        assertEquals(remainingResidents, colony.getUnitsInsideColony().size());
        for (final Unit resident : colony.getUnitsInsideColony()) {
            assertEquals("France", resident.getOwner().getName());
        }
        assertEquals(liberty, colony.getLiberty());
        if (feedback != null) {
            waitForFeedback(feedback);
            assertTrue(hasLabel(feedback));
        }
    }

    public void verifyWarDeclared(final boolean expected) {
        assertEquals(expected,
                getModel().areHostile(getModel().getPlayerByName("Dutch"),
                        getModel().getPlayerByName("France")));
    }

    /** The fortification rule still refuses a voluntary exit of the unit. */
    public void verifyVoluntaryExitRefused(final int unitId) {
        assertFalse(getModel().getUnitById(unitId).canLeaveColony());
    }

    public void verifyStoredMuskets(final int expected) {
        assertEquals(expected,
                colony().getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
    }
}
