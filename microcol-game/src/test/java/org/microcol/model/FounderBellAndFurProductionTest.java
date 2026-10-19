package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

/**
 * Jefferson, Paine and Hudson on real colonies. Town-hall bells use the
 * accepted composition: the unattended base receives printing and Paine,
 * staffed bells also receive Jefferson, and one floor closes the hall total.
 * Hudson doubles positive fur only in the shared field calculation. Colony 0
 * has five residents, so 500 liberty gives one political point and 1000 gives
 * two.
 */
class FounderBellAndFurProductionTest {

    private GameModelPo data(final long liberty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        saved(data).setLiberty(liberty);
        return data;
    }

    private ColonyPo saved(final GameModelPo data) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
    }

    /** A valid active Congress whose members are exactly the given founders. */
    private void giveMembers(final GameModelPo data,
            final List<Founder> members) {
        giveMembers(data, saved(data).getOwnerName(), members);
    }

    /** As {@link #giveMembers(GameModelPo, List)} for the named nation. */
    private void giveMembers(final GameModelPo data, final String nation,
            final List<Founder> members) {
        final CongressPo congress = data.getModel().getGameManager()
                .getPlayers().stream()
                .filter(player -> nation.equals(player.getName())).findFirst()
                .orElseThrow().getCongress();
        congress.setActivated(true);
        congress.setGeneration(1);
        congress.setBells(0);
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        congress.setMembers(members);
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder other : Founder.values()) {
                if (other.getCategory() == category
                        && !members.contains(other)) {
                    candidates.add(other);
                    break;
                }
            }
        }
        congress.setCandidates(candidates);
    }

    /** Move a saved resident from its field into a town-hall slot. */
    private void moveToTownHall(final GameModelPo data, final int unitId,
            final int slot, final UnitType type) {
        final ColonyPo colony = saved(data);
        colony.getColonyFields().stream().filter(
                field -> Integer.valueOf(unitId).equals(field.getWorkerId()))
                .forEach(field -> {
                    field.setWorkerId(null);
                    field.setProducedGoodsType(null);
                });
        final UnitPo unit = data.getModel().getUnits().stream()
                .filter(saved -> saved.getId() == unitId).findFirst()
                .orElseThrow();
        unit.setType(type);
        unit.setPlaceColonyField(null);
        unit.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        final ConstructionPo hall = colony.getConstructions().stream()
                .filter(saved -> saved.getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow();
        hall.getSlots().get(slot).setWorkerId(unitId);
    }

    private void addBuilding(final GameModelPo data,
            final ConstructionType type) {
        final ConstructionPo improvement = new ConstructionPo();
        improvement.setType(type);
        saved(data).getConstructions().add(improvement);
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private int stored(final Colony colony, final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    private int planned(final Colony colony, final GoodsType type) {
        return colony.getGoodsStats().getStatsByType(type).getRowProduction();
    }

    private Colony colony(final Model model) {
        return model.getColonyById(0).orElseThrow();
    }

    @Test
    void jeffersonAddsHalfOnlyToStaffedStatesmen() {
        final GameModelPo data = data(0);
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        final Model without = load(data);
        // Unattended 1 plus expert 6 gives 7 without the father.
        assertEquals(7, planned(colony(without), GoodsType.BELL));
        final GameModelPo withFather = data(0);
        giveMembers(withFather, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(withFather, 0, 0, UnitType.ELDER_STATESMAN);
        // (1 * 100 + 6 * 150) / 100 = 10: only the staffed 6 gains 50%.
        assertEquals(10, planned(colony(load(withFather)), GoodsType.BELL));
    }

    @Test
    void jeffersonDoesNotChangeUnattendedTownHallBell() {
        final GameModelPo data = data(1000);
        giveMembers(data, List.of(Founder.THOMAS_JEFFERSON));
        final Colony colony = colony(load(data));
        // Empty hall: the unattended base stays 1 and gains nothing.
        assertEquals(1, planned(colony, GoodsType.BELL));
    }

    @Test
    void jeffersonWithOrdinaryColonistAndLaborers() {
        final GameModelPo colonist = data(0);
        giveMembers(colonist, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(colonist, 0, 0, UnitType.COLONIST);
        // (100 + 3 * 150) / 100 = 5.
        assertEquals(5, planned(colony(load(colonist)), GoodsType.BELL));

        final GameModelPo servant = data(0);
        giveMembers(servant, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(servant, 0, 0, UnitType.INDENTURED_SERVANT);
        // The servant's indoor reduction first gives 2; then (100 + 2 * 150) /
        // 100 = 4.
        assertEquals(4, planned(colony(load(servant)), GoodsType.BELL));

        final GameModelPo criminal = data(0);
        giveMembers(criminal, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(criminal, 0, 0, UnitType.PETTY_CRIMINAL);
        // Criminal gives 1; (100 + 1 * 150) / 100 = 2 after the floor.
        assertEquals(2, planned(colony(load(criminal)), GoodsType.BELL));
    }

    @Test
    void paineAddsCurrentTaxToUnattendedAndStaffedBells() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        final Colony colony = colony(load(data));
        colony.getOwner().setTaxRate(0);
        assertEquals(7, planned(colony, GoodsType.BELL));
        colony.getOwner().setTaxRate(20);
        // (1 * 120 + 6 * 120) / 100 = 8.4, floored once to 8.
        assertEquals(8, planned(colony, GoodsType.BELL));
        colony.getOwner().setTaxRate(50);
        // (1 * 150 + 6 * 150) / 100 = 10.5, floored once to 10.
        assertEquals(10, planned(colony, GoodsType.BELL));
    }

    @Test
    void paineUnattendedBellIsFlooredWithoutCreatingExtraBells() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        final Colony colony = colony(load(data));
        colony.getOwner().setTaxRate(20);
        // Only the unattended 1 bell: 1 * 1.2 floors to 1.
        assertEquals(1, planned(colony, GoodsType.BELL));
        colony.getOwner().setTaxRate(75);
        // 1 * 1.75 floors to 1; Paine never adds a bell from zero output.
        assertEquals(1, planned(colony, GoodsType.BELL));
    }

    @Test
    void currentTaxChangeAffectsOnlyTheNextCreditedOutput() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        final Model model = load(data);
        final Colony colony = colony(model);
        colony.getOwner().setTaxRate(20);
        assertEquals(8, planned(colony, GoodsType.BELL));
        final int before = stored(colony, GoodsType.BELL);
        colony.startTurn();
        assertEquals(before + 8, stored(colony, GoodsType.BELL));
        colony.getOwner().setTaxRate(50);
        assertEquals(10, planned(colony, GoodsType.BELL));
        colony.startTurn();
        assertEquals(before + 18, stored(colony, GoodsType.BELL));
    }

    @Test
    void combinedFoundersWithPressGiveSeventeenAndNewspaperTwentyOne() {
        final GameModelPo press = data(500);
        giveMembers(press, List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON));
        moveToTownHall(press, 0, 0, UnitType.ELDER_STATESMAN);
        addBuilding(press, ConstructionType.PRINTING_PRESS);
        final Colony pressColony = colony(load(press));
        pressColony.getOwner().setTaxRate(20);
        // Staffed 7 at 220%, unattended 1 at 170%, floored once to 17.
        assertEquals(17, planned(pressColony, GoodsType.BELL));
        // Penn is absent, so the unattended chapel cross is unchanged.
        assertEquals(1, planned(pressColony, GoodsType.CROSS));

        final GameModelPo newspaper = data(500);
        giveMembers(newspaper, List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON));
        moveToTownHall(newspaper, 0, 0, UnitType.ELDER_STATESMAN);
        addBuilding(newspaper, ConstructionType.NEWSPAPER);
        final Colony newspaperColony = colony(load(newspaper));
        newspaperColony.getOwner().setTaxRate(20);
        // Newspaper replaces press: staffed 270%, unattended 220%.
        assertEquals(21, planned(newspaperColony, GoodsType.BELL));
    }

    @Test
    void combinedFoundersActualTurnMatchesPreviewAndContinue() {
        final GameModelPo data = data(500);
        giveMembers(data, List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON));
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        addBuilding(data, ConstructionType.PRINTING_PRESS);
        final Model model = load(data);
        final Colony colony = colony(model);
        colony.getOwner().setTaxRate(20);
        final int preview = planned(colony, GoodsType.BELL);
        assertEquals(17, preview);
        final int before = stored(colony, GoodsType.BELL);
        // Repeated reads of the preview grant nothing.
        assertEquals(preview, planned(colony, GoodsType.BELL));
        assertEquals(before, stored(colony, GoodsType.BELL));
        colony.startTurn();
        assertEquals(before + preview, stored(colony, GoodsType.BELL));
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String saved = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(saved, new Gson().toJson(resumed.save()));
        assertEquals(preview, planned(colony(resumed), GoodsType.BELL));
    }

    @Test
    void hudsonDoublesOrdinaryAndExpertFurAndKeepsZeroYield() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.HENRY_HUDSON));
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(UnitType.EXPERT_FUR_TRAPPER);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 1)
                .findFirst().orElseThrow().setType(UnitType.COLONIST);
        final Colony colony = colony(load(data));
        final Unit ordinary = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 1).findFirst().orElseThrow();
        final Unit expert = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        int furFields = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            final int base = field.getGoodsTypeProduction(GoodsType.FUR);
            if (base == 0) {
                // Zero yield stays zero, even for an expert trapper.
                assertEquals(0,
                        field.getPotentialProduction(expert, GoodsType.FUR)
                                .getAmount());
                continue;
            }
            furFields++;
            // Ordinary: base doubled. Expert: base doubled by the profession
            // and again by Hudson.
            assertEquals(base * 2,
                    field.getPotentialProduction(ordinary, GoodsType.FUR)
                            .getAmount());
            assertEquals(base * 4, field
                    .getPotentialProduction(expert, GoodsType.FUR).getAmount());
        }
        assertTrue(furFields > 0, "The fixture needs a fur-producing field");
    }

    @Test
    void hudsonDoesNotCreateCoatsAndRemovedMembershipRestoresFur() {
        final GameModelPo withHudson = data(0);
        giveMembers(withHudson, List.of(Founder.HENRY_HUDSON));
        final Colony colony = colony(load(withHudson));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.FUR, 0));
        // Furs stay an input: no stored furs means no coats, with or without
        // Hudson.
        assertEquals(0, colony.getGoodsStats().getStatsByType(GoodsType.COAT)
                .getRowProduction());
        final GameModelPo withoutHudson = data(0);
        giveMembers(withoutHudson, List.of());
        final Colony plain = colony(load(withoutHudson));
        final Unit worker = colony.getUnitsInsideColony().get(0);
        for (int index = 0; index < colony.getColonyFields().size(); index++) {
            final ColonyField field = colony.getColonyFields().get(index);
            final ColonyField plainField = plain.getColonyFields().get(index);
            // Each field doubles against the same field without Hudson.
            assertEquals(
                    plainField.getPotentialProduction(worker, GoodsType.FUR)
                            .getAmount() * 2,
                    field.getPotentialProduction(worker, GoodsType.FUR)
                            .getAmount());
        }
        assertEquals(0, plain.getGoodsStats().getStatsByType(GoodsType.COAT)
                .getRowProduction());
    }

    /**
     * Free-play Quebec (French, one resident, liberty 0) with a valid Dutch
     * rival at peace. Callers add the fixture pieces of the battle themselves.
     */
    private GameModelPo freePlayData() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        return data;
    }

    /** Sum of the preview fur of every field for one worker. */
    private int furPreview(final Colony colony, final Unit worker) {
        return colony.getColonyFields().stream()
                .mapToInt(field -> field
                        .getPotentialProduction(worker, GoodsType.FUR)
                        .getAmount())
                .sum();
    }

    /**
     * Quebec is taken through the real defense battle by a Dutch force. The
     * prior owner keeps its own founders; the new owner's membership and tax
     * decide the hall and fur output in preview, in the turn and after
     * Continue.
     */
    private void captureMovesFoundersWithTheColony(
            final List<Founder> priorMembers, final int priorTax,
            final int beforeBells, final List<Founder> newMembers,
            final int newTax, final int afterBells) {
        final GameModelPo data = freePlayData();
        // Give the capture fixture a positive fur source for the Hudson check.
        final Location trapping = saved(data).getLocation()
                .add(Direction.north.getVector());
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(trapping, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final var resources = data.getModel().getMap().getResourceMap();
        resources.put(trapping, ResourceOverlay.TRAPPING);
        data.getModel().getMap().setResources(resources);
        giveMembers(data, "France", priorMembers);
        giveMembers(data, "Dutch", newMembers);
        final Model model = load(data);
        final Colony quebec = colony(model);
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        assertSame(france, quebec.getOwner());
        france.setTaxRate(priorTax);
        dutch.setTaxRate(newTax);
        quebec.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        // A new colonist takes the town hall beside the unattended bell.
        final Unit statesman = model.addUnitOutSideColony(quebec);
        statesman.placeToColonyStructureSlot(
                quebec.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        model.declareHostilities(france, dutch);
        // The armed French colonist guarding the square would block capture.
        model.getUnitById(6).placeToLocation(Location.of(18, 10));
        final Unit armed = model.addUnitOutSideColony(quebec);
        armed.takeOver(dutch);
        assertEquals(beforeBells, planned(quebec, GoodsType.BELL));
        final int furBefore = furPreview(quebec, statesman);
        final int bellsBefore = stored(quebec, GoodsType.BELL);
        quebec.startTurn();
        assertEquals(bellsBefore + beforeBells, stored(quebec, GoodsType.BELL));

        ColonyBattles.capture(model, quebec, armed);
        assertSame(dutch, quebec.getOwner());
        assertEquals(afterBells, planned(quebec, GoodsType.BELL));
        final int furAfter = furPreview(quebec, statesman);
        final boolean hudsonBefore = priorMembers
                .contains(Founder.HENRY_HUDSON);
        final boolean hudsonAfter = newMembers.contains(Founder.HENRY_HUDSON);
        final int base = hudsonBefore ? furBefore / 2 : furBefore;
        assertTrue(base > 0, "The captured colony needs a fur-producing field");
        assertEquals(hudsonAfter ? base * 2 : base, furAfter);
        final int bellsAfterCapture = stored(quebec, GoodsType.BELL);
        quebec.startTurn();
        assertEquals(bellsAfterCapture + afterBells,
                stored(quebec, GoodsType.BELL));

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony reloaded = colony(resumed);
        assertEquals("Dutch", reloaded.getOwner().getName());
        assertEquals(afterBells, planned(reloaded, GoodsType.BELL));
        assertEquals(priorMembers,
                resumed.getPlayerByName("France").getCongressMembers());
        assertEquals(newMembers,
                resumed.getPlayerByName("Dutch").getCongressMembers());
    }

    @Test
    void captureByAnOwnerWithoutFoundersDropsThePriorOwnersFoundersAndTax() {
        // France: 1 unattended + 3 staffed at 120% and 170%; Dutch: 100%.
        captureMovesFoundersWithTheColony(List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON), 20, 6, List.of(),
                0, 4);
    }

    @Test
    void captureByAnOwnerWithFoundersAddsOnlyTheNewOwnersFoundersAndTax() {
        captureMovesFoundersWithTheColony(List.of(), 0, 4,
                List.of(Founder.THOMAS_JEFFERSON, Founder.THOMAS_PAINE,
                        Founder.HENRY_HUDSON),
                20, 6);
    }

    /**
     * Thrive colony 0 with an ordinary colonist in its fur trader's house and
     * the given stored furs. Hudson is either a member or not.
     */
    private Colony furTraderColony(final boolean hudson, final int furStock) {
        final GameModelPo data = data(0);
        giveMembers(data, hudson ? List.of(Founder.HENRY_HUDSON) : List.of());
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(UnitType.COLONIST);
        final Colony colony = colony(load(data));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.FUR, furStock));
        final Unit worker = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.FUR_TRADERS_HOUSE)
                        .getSlotAt(0));
        return colony;
    }

    @ParameterizedTest
    @CsvSource({ "6,3", "2,2", "0,0" })
    void hudsonDoublesFieldFurButNotFurTraderCoatsOrTheirFurInput(
            final int furStock, final int coats) {
        final Colony plain = furTraderColony(false, furStock);
        final Colony hudson = furTraderColony(true, furStock);
        // One ordinary worker turns three furs into three coats; with less
        // stock the real input is the cap.
        assertEquals(coats, planned(plain, GoodsType.COAT));
        assertEquals(coats, planned(hudson, GoodsType.COAT));
        assertEquals(coats, plain.getGoodsStats().getStatsByType(GoodsType.FUR)
                .getConsumed());
        assertEquals(coats, hudson.getGoodsStats().getStatsByType(GoodsType.FUR)
                .getConsumed());
        final Unit plainWorker = plain
                .getConstructionByType(ConstructionType.FUR_TRADERS_HOUSE)
                .getSlotAt(0).getUnit();
        final Unit hudsonWorker = hudson
                .getConstructionByType(ConstructionType.FUR_TRADERS_HOUSE)
                .getSlotAt(0).getUnit();
        for (int index = 0; index < plain.getColonyFields().size(); index++) {
            // Fields double; the coat house has no field inputs of its own.
            assertEquals(
                    plain.getColonyFields().get(index)
                            .getPotentialProduction(plainWorker, GoodsType.FUR)
                            .getAmount() * 2,
                    hudson.getColonyFields().get(index)
                            .getPotentialProduction(hudsonWorker, GoodsType.FUR)
                            .getAmount());
        }
        plain.startTurn();
        hudson.startTurn();
        assertEquals(coats, stored(plain, GoodsType.COAT));
        assertEquals(coats, stored(hudson, GoodsType.COAT));
        assertEquals(furStock - coats, stored(plain, GoodsType.FUR));
        assertEquals(furStock - coats, stored(hudson, GoodsType.FUR));
    }
}
