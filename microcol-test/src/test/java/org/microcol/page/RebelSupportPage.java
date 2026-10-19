package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Colony;
import org.microcol.model.GoodsType;
import org.microcol.model.MovementPoints;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Own rebel support in the political report and the stored colony liberty
 * behind it. Fixture F67 is derived from F64: one Dutch colony, New Amsterdam,
 * with two field workers and a third resident in the town hall.
 */
public final class RebelSupportPage extends AbstractScreen {
    private static final File SOURCE = new File(
            "src/test/scenarios/F64-la-salle-stockade.microcol");
    private static final File TRANSFER_SOURCE = new File(
            "src/test/scenarios/F31-shared-colony-fields.microcol");
    private static final int TOWN_HALL_WORKER = 4;
    private static final int MOVING_RESIDENT = 17;

    private long libertyBefore;
    private int bellsBefore;

    public RebelSupportPage(final TestContext context) {
        super(context);
    }

    /**
     * F67: New Amsterdam holds the given liberty and has three residents, the
     * third one working in the town hall where it produces bells.
     */
    public void prepareFixture(final long liberty) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject colony = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        colony.addProperty("liberty", liberty);
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            if ("TOWN_HALL".equals(construction.get("type").getAsString())) {
                construction.getAsJsonArray("slots").get(1).getAsJsonObject()
                        .addProperty("workerId", TOWN_HALL_WORKER);
            }
        }
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == TOWN_HALL_WORKER) {
                unit.remove("placeMap");
                unit.add("placeConstructionSlot", new JsonObject());
            }
        }
        final Path directory = Path.of("target", "f67");
        Files.createDirectories(directory);
        final File file = directory.resolve("rebel-support.microcol").toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    /** Remember stored liberty and the bells the next update will produce. */
    public void rememberLibertyAndPlannedBells() {
        libertyBefore = colony().getLiberty();
        bellsBefore = Math.max(0, colony().getGoodsStats()
                .getStatsByType(GoodsType.BELL).getRowProduction());
    }

    public void verifyNationalSupport(final int percent, final int residents) {
        assertEquals(
                "National rebel support: " + percent + "% (" + residents
                        + " residents)",
                getLabeledById("nationalSupport").getText());
    }

    public void verifyColonyRow(final String name, final int population,
            final long liberty, final int percent) {
        verifyColonyRow(0, name, population, liberty, percent);
    }

    public void verifyColonyRow(final int colonyId, final String name,
            final int population, final long liberty, final int percent) {
        assertEquals(
                name + " — population " + population + ", stored liberty "
                        + liberty + ", local support " + percent + "%",
                getLabeledById("supportColony-" + colonyId).getText());
        assertFalse(isCssIdVisible("supportNone"));
    }

    /** The open colony view shows the same local values read-only. */
    public void verifyColonyTooltip(final String name, final int population,
            final long liberty, final int percent, final int bonus,
            final int tories, final int threshold, final int penalty) {
        assertEquals(name + " — Rename colony\nPopulation " + population
                + ", stored liberty " + liberty + ", local rebel support "
                + percent + "%\n"
                + "Political production per job: support bonus +" + bonus
                + ", Tories " + tories + " (penalty from " + threshold
                + "), Tory penalty " + penalty + ", net " + (bonus + penalty),
                getNodeFinder().lookup("#colonyName")
                        .queryAs(javafx.scene.control.Label.class).getTooltip()
                        .getText());
    }

    public void verifyColonyLiberty(final int colonyId, final long liberty,
            final int population) {
        final Colony colony = getModel().getColonyById(colonyId).orElseThrow();
        assertEquals(population, colony.getPopulation());
        assertEquals(liberty, colony.getLiberty());
    }

    /**
     * F67 transfer fixture, derived from F31: New Amsterdam has two residents
     * with liberty 200 and Delft eight residents with liberty 400. Unit 17 is
     * the New Amsterdam town hall resident who will move; it can walk the two
     * tiles to Delft in one turn.
     */
    public void prepareTransferFixture() throws Exception {
        final JsonObject saved = new Gson().fromJson(
                Files.readString(TRANSFER_SOURCE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final com.google.gson.JsonArray units = model.getAsJsonArray("units");
        for (int index = units.size() - 1; index >= 0; index--) {
            // The unattached tool carrier would share the colony square.
            if (units.get(index).getAsJsonObject().get("id").getAsInt() == 4) {
                units.remove(index);
            }
        }
        // A connected road permits the two-tile transfer within a legal turn.
        final Gson gson = new Gson();
        final org.microcol.model.store.WorldMapPo map = gson.fromJson(
                model.getAsJsonObject("map"),
                org.microcol.model.store.WorldMapPo.class);
        final java.util.Set<org.microcol.model.Location> roads = map
                .getRoadSet();
        for (int x = 24; x <= 26; x++) {
            roads.add(org.microcol.model.Location.of(x, 16));
        }
        map.setRoads(roads);
        model.add("map", gson.toJsonTree(map));
        final JsonObject newAmsterdam = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        final JsonObject delft = model.getAsJsonArray("colonies").get(1)
                .getAsJsonObject();
        newAmsterdam.addProperty("liberty", 200);
        delft.addProperty("liberty", 400);
        units.add(resident(MOVING_RESIDENT));
        slot(newAmsterdam, "TOWN_HALL", 1, MOVING_RESIDENT);
        final int[][] delftSlots = { { 0 }, { 1 }, { 2 } };
        int next = 10;
        for (final int[] position : delftSlots) {
            units.add(resident(next));
            slot(delft, "TOWN_HALL", position[0], next++);
            units.add(resident(next));
            slot(delft, "TOBACCONISTS_HOUSE", position[0], next++);
        }
        units.add(resident(next));
        slot(delft, "WEAVERS_HOUSE", 0, next);
        final Path directory = Path.of("target", "f67");
        Files.createDirectories(directory);
        final File file = directory.resolve("rebel-transfer.microcol").toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    private JsonObject resident(final int id) {
        final JsonObject unit = new JsonObject();
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("id", id);
        unit.addProperty("type", "COLONIST");
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeConstructionSlot", new JsonObject());
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new com.google.gson.JsonArray());
        unit.add("cargo", cargo);
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        unit.add("action", action);
        unit.addProperty("movementThirds", MovementPoints.ofPoints(1));
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    private void slot(final JsonObject colony, final String building,
            final int index, final int workerId) {
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            if (building.equals(construction.get("type").getAsString())) {
                construction.getAsJsonArray("slots").get(index)
                        .getAsJsonObject().addProperty("workerId", workerId);
            }
        }
    }

    /** Drag the unit on the map from one colony square to another. */
    public void walkResident(final org.microcol.model.Location from,
            final org.microcol.model.Location to) throws Exception {
        final GamePage game = GamePage.of(getContext());
        game.moveMouseAtLocation(from);
        game.dragMouseAtLocation(to);
        org.testfx.util.WaitForAsyncUtils.waitFor(10,
                java.util.concurrent.TimeUnit.SECONDS, () -> getModel()
                        .getUnitById(MOVING_RESIDENT).getLocation().equals(to));
        game.waitWhileMoving();
    }

    /** The resident has left colony work and stands on the map square. */
    public void verifyResidentOutside(final org.microcol.model.Location at) {
        assertTrue(getModel().getUnitById(MOVING_RESIDENT).isAtPlaceLocation());
        assertEquals(at, getModel().getUnitById(MOVING_RESIDENT).getLocation());
    }

    public void verifyResidentWorksAt(final int colonyId) {
        assertTrue(getModel().getUnitById(MOVING_RESIDENT)
                .isAtPlaceConstructionSlot());
        assertEquals(colonyId, getModel().getUnitById(MOVING_RESIDENT)
                .getPlaceConstruction().getColony().getId());
    }

    /** The open report shows exactly what the model currently holds. */
    public void verifyReportMatchesModel() {
        final Colony colony = colony();
        final org.microcol.model.RebelSupport national = colony.getOwner()
                .getNationalRebelSupport();
        verifyNationalSupport(national.getDisplayPercent(),
                (int) national.getPopulation());
        verifyColonyRow(colony.getName(), colony.getPopulation(),
                colony.getLiberty(),
                colony.getRebelSupport().getDisplayPercent());
    }

    public void verifyLiberty(final long liberty, final int population) {
        assertEquals(population, colony().getPopulation());
        assertEquals(liberty, colony().getLiberty());
    }

    /** One update credited the bells that were planned, and only once. */
    public void verifyLibertyCreditedOnce() {
        assertTrue(bellsBefore > 0, "The town hall worker must make bells");
        assertEquals(
                Math.min(libertyBefore + bellsBefore,
                        200L * colony().getPopulation()),
                colony().getLiberty());
    }

    public void verifyLibertyUnchanged() {
        assertEquals(libertyBefore, colony().getLiberty());
    }
}
