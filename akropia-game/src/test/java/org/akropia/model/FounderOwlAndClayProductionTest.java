package org.akropia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.ColonyPo;
import org.akropia.model.store.ConstructionPo;
import org.akropia.model.store.CongressPo;
import org.akropia.model.store.PlaceConstructionSlotPo;
import org.akropia.model.store.UnitPo;

import com.google.gson.Gson;

/**
 * Jefferson, Paine and Hudson on real colonies. Town-hall owls use the accepted
 * composition: the unattended base receives printing and Paine, staffed owls
 * also receive Jefferson, and one floor closes the hall total. Hudson doubles
 * positive clay only in the shared field calculation. Colony 0 has five
 * residents, so 500 liberty gives one political point and 1000 gives two.
 */
class FounderOwlAndClayProductionTest {

    private GameModelPo data(final long liberty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
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
        congress.setOwls(0);
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
        assertEquals(7, planned(colony(without), GoodsType.OWLS));
        final GameModelPo withFather = data(0);
        giveMembers(withFather, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(withFather, 0, 0, UnitType.ELDER_STATESMAN);
        // (1 * 100 + 6 * 150) / 100 = 10: only the staffed 6 gains 50%.
        assertEquals(10, planned(colony(load(withFather)), GoodsType.OWLS));
    }

    @Test
    void jeffersonDoesNotChangeUnattendedTownHallOwl() {
        final GameModelPo data = data(1000);
        giveMembers(data, List.of(Founder.THOMAS_JEFFERSON));
        final Colony colony = colony(load(data));
        // Empty hall: the unattended base stays 1 and gains nothing.
        assertEquals(1, planned(colony, GoodsType.OWLS));
    }

    @Test
    void jeffersonWithOrdinaryColonistAndLaborers() {
        final GameModelPo colonist = data(0);
        giveMembers(colonist, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(colonist, 0, 0, UnitType.COLONIST);
        // (100 + 3 * 150) / 100 = 5.
        assertEquals(5, planned(colony(load(colonist)), GoodsType.OWLS));

        final GameModelPo servant = data(0);
        giveMembers(servant, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(servant, 0, 0, UnitType.INDENTURED_SERVANT);
        // The servant's indoor reduction first gives 2; then (100 + 2 * 150) /
        // 100 = 4.
        assertEquals(4, planned(colony(load(servant)), GoodsType.OWLS));

        final GameModelPo criminal = data(0);
        giveMembers(criminal, List.of(Founder.THOMAS_JEFFERSON));
        moveToTownHall(criminal, 0, 0, UnitType.PETTY_CRIMINAL);
        // Criminal gives 1; (100 + 1 * 150) / 100 = 2 after the floor.
        assertEquals(2, planned(colony(load(criminal)), GoodsType.OWLS));
    }

    @Test
    void paineAddsCurrentTaxToUnattendedAndStaffedOwls() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        final Colony colony = colony(load(data));
        colony.getOwner().setTaxRate(0);
        assertEquals(7, planned(colony, GoodsType.OWLS));
        colony.getOwner().setTaxRate(20);
        // (1 * 120 + 6 * 120) / 100 = 8.4, floored once to 8.
        assertEquals(8, planned(colony, GoodsType.OWLS));
        colony.getOwner().setTaxRate(50);
        // (1 * 150 + 6 * 150) / 100 = 10.5, floored once to 10.
        assertEquals(10, planned(colony, GoodsType.OWLS));
    }

    @Test
    void paineUnattendedOwlIsFlooredWithoutCreatingExtraOwls() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        final Colony colony = colony(load(data));
        colony.getOwner().setTaxRate(20);
        // Only the unattended 1 owl: 1 * 1.2 floors to 1.
        assertEquals(1, planned(colony, GoodsType.OWLS));
        colony.getOwner().setTaxRate(75);
        // 1 * 1.75 floors to 1; Paine never adds an owl from zero output.
        assertEquals(1, planned(colony, GoodsType.OWLS));
    }

    @Test
    void currentTaxChangeAffectsOnlyTheNextCreditedOutput() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.THOMAS_PAINE));
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        final Model model = load(data);
        final Colony colony = colony(model);
        colony.getOwner().setTaxRate(20);
        assertEquals(8, planned(colony, GoodsType.OWLS));
        final int before = stored(colony, GoodsType.OWLS);
        colony.startTurn();
        assertEquals(before + 8, stored(colony, GoodsType.OWLS));
        colony.getOwner().setTaxRate(50);
        assertEquals(10, planned(colony, GoodsType.OWLS));
        colony.startTurn();
        assertEquals(before + 18, stored(colony, GoodsType.OWLS));
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
        assertEquals(17, planned(pressColony, GoodsType.OWLS));
        // Penn is absent, so the unattended chapel torch is unchanged.
        assertEquals(1, planned(pressColony, GoodsType.TORCHES));

        final GameModelPo newspaper = data(500);
        giveMembers(newspaper, List.of(Founder.THOMAS_JEFFERSON,
                Founder.THOMAS_PAINE, Founder.HENRY_HUDSON));
        moveToTownHall(newspaper, 0, 0, UnitType.ELDER_STATESMAN);
        addBuilding(newspaper, ConstructionType.NEWSPAPER);
        final Colony newspaperColony = colony(load(newspaper));
        newspaperColony.getOwner().setTaxRate(20);
        // Newspaper replaces press: staffed 270%, unattended 220%.
        assertEquals(21, planned(newspaperColony, GoodsType.OWLS));
    }

    /** An ELDER_STATESMAN hall whose owner joins the founder on one update. */
    private Model hallReadyFor(final Founder founder) {
        final GameModelPo data = data(0);
        moveToTownHall(data, 0, 0, UnitType.ELDER_STATESMAN);
        RealFounderJoin.readyFor(RealFounderJoin.congressOf(data.getModel(),
                saved(data).getOwnerName()), founder);
        return load(data);
    }

    @Test
    void jeffersonJoiningThroughOwnerUpdateRaisesTheStaffedHallOnly() {
        final Model model = hallReadyFor(Founder.THOMAS_JEFFERSON);
        // Unattended 1 plus expert 6, as in the control without the father.
        assertEquals(7, planned(colony(model), GoodsType.OWLS));
        RealFounderJoin.joinThroughOwnerUpdate(model, Founder.THOMAS_JEFFERSON);
        // (1 * 100 + 6 * 150) / 100 = 10.
        assertEquals(10, planned(colony(model), GoodsType.OWLS));
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(10, planned(colony(resumed), GoodsType.OWLS));
    }

    @Test
    void paineJoiningThroughOwnerUpdateAddsTheCurrentTaxToTheHall() {
        final Model model = hallReadyFor(Founder.THOMAS_PAINE);
        colony(model).getOwner().setTaxRate(20);
        assertEquals(7, planned(colony(model), GoodsType.OWLS));
        RealFounderJoin.joinThroughOwnerUpdate(model, Founder.THOMAS_PAINE);
        // (1 * 120 + 6 * 120) / 100 = 8.4, floored once to 8.
        assertEquals(8, planned(colony(model), GoodsType.OWLS));
        // A later tax change moves the bonus: (1 * 150 + 6 * 150) / 100 = 10.
        colony(model).getOwner().setTaxRate(50);
        assertEquals(10, planned(colony(model), GoodsType.OWLS));
    }

    @Test
    void hudsonJoiningThroughOwnerUpdateDoublesEveryPositiveClayField() {
        final Model model = hallReadyFor(Founder.HENRY_HUDSON);
        final Colony colony = colony(model);
        final Unit worker = colony.getUnitsInsideColony().get(0);
        final List<Integer> before = new ArrayList<>();
        for (final ColonyField field : colony.getColonyFields()) {
            before.add(field.getPotentialProduction(worker, GoodsType.CLAY)
                    .getAmount());
        }
        RealFounderJoin.joinThroughOwnerUpdate(model, Founder.HENRY_HUDSON);
        int doubled = 0;
        for (int index = 0; index < before.size(); index++) {
            final int after = colony.getColonyFields().get(index)
                    .getPotentialProduction(worker, GoodsType.CLAY).getAmount();
            assertEquals(before.get(index) * 2, after);
            if (before.get(index) > 0) {
                doubled++;
            }
        }
        assertTrue(doubled > 0, "The fixture needs a clay-producing field");
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
        final int preview = planned(colony, GoodsType.OWLS);
        assertEquals(17, preview);
        final int before = stored(colony, GoodsType.OWLS);
        // Repeated reads of the preview grant nothing.
        assertEquals(preview, planned(colony, GoodsType.OWLS));
        assertEquals(before, stored(colony, GoodsType.OWLS));
        colony.startTurn();
        assertEquals(before + preview, stored(colony, GoodsType.OWLS));
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String saved = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(saved, new Gson().toJson(resumed.save()));
        assertEquals(preview, planned(colony(resumed), GoodsType.OWLS));
    }

    @Test
    void hudsonDoublesOrdinaryAndExpertClayAndKeepsZeroYield() {
        final GameModelPo data = data(0);
        giveMembers(data, List.of(Founder.HENRY_HUDSON));
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(UnitType.EXPERT_CLAY_DIGGER);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 1)
                .findFirst().orElseThrow().setType(UnitType.COLONIST);
        final Colony colony = colony(load(data));
        final Unit ordinary = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 1).findFirst().orElseThrow();
        final Unit expert = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        int clayFields = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            final int base = field.getGoodsTypeProduction(GoodsType.CLAY);
            if (base == 0) {
                // Zero yield stays zero, even for an expert clay digger.
                assertEquals(0,
                        field.getPotentialProduction(expert, GoodsType.CLAY)
                                .getAmount());
                continue;
            }
            clayFields++;
            // Ordinary: base doubled. Expert: base doubled by the profession
            // and again by Hudson.
            assertEquals(base * 2,
                    field.getPotentialProduction(ordinary, GoodsType.CLAY)
                            .getAmount());
            assertEquals(base * 4,
                    field.getPotentialProduction(expert, GoodsType.CLAY)
                            .getAmount());
        }
        assertTrue(clayFields > 0, "The fixture needs a clay-producing field");
    }

    @Test
    void hudsonDoesNotCreateAmphoraeAndRemovedMembershipRestoresClay() {
        final GameModelPo withHudson = data(0);
        giveMembers(withHudson, List.of(Founder.HENRY_HUDSON));
        final Colony colony = colony(load(withHudson));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CLAY, 0));
        // Clay stay an input: no stored clay means no amphorae, with or without
        // Hudson.
        assertEquals(0, colony.getGoodsStats()
                .getStatsByType(GoodsType.AMPHORAE).getRowProduction());
        final GameModelPo withoutHudson = data(0);
        giveMembers(withoutHudson, List.of());
        final Colony plain = colony(load(withoutHudson));
        final Unit worker = colony.getUnitsInsideColony().get(0);
        for (int index = 0; index < colony.getColonyFields().size(); index++) {
            final ColonyField field = colony.getColonyFields().get(index);
            final ColonyField plainField = plain.getColonyFields().get(index);
            // Each field doubles against the same field without Hudson.
            assertEquals(
                    plainField.getPotentialProduction(worker, GoodsType.CLAY)
                            .getAmount() * 2,
                    field.getPotentialProduction(worker, GoodsType.CLAY)
                            .getAmount());
        }
        assertEquals(0, plain.getGoodsStats().getStatsByType(GoodsType.AMPHORAE)
                .getRowProduction());
    }

    /**
     * Free-play Quebec (French, one resident, liberty 0) with a valid Dutch
     * rival at peace. Callers add the fixture pieces of the battle themselves.
     */
    private GameModelPo freePlayData() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        return data;
    }

    /** Sum of the preview clay of every field for one worker. */
    private int clayPreview(final Colony colony, final Unit worker) {
        return colony.getColonyFields().stream()
                .mapToInt(field -> field
                        .getPotentialProduction(worker, GoodsType.CLAY)
                        .getAmount())
                .sum();
    }

    /**
     * Quebec is taken through the real defense battle by a Dutch force. The
     * prior owner keeps its own founders; the new owner's membership and tax
     * decide the hall and clay output in preview, in the turn and after
     * Continue.
     */
    private void captureMovesFoundersWithTheColony(
            final List<Founder> priorMembers, final int priorTax,
            final int beforeOwls, final List<Founder> newMembers,
            final int newTax, final int afterOwls) {
        final GameModelPo data = freePlayData();
        // Give the capture fixture a positive clay source for the Hudson check.
        final Location clayPit = saved(data).getLocation()
                .add(Direction.north.getVector());
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(clayPit, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final var resources = data.getModel().getMap().getResourceMap();
        resources.put(clayPit, ResourceOverlay.CLAY_PIT);
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
        quebec.getWarehouse().setGoods(Goods.of(GoodsType.GRAIN, 100));
        // A new colonist takes the town hall beside the unattended owl.
        final Unit statesman = model.addUnitOutSideColony(quebec);
        statesman.placeToColonyStructureSlot(
                quebec.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        model.declareHostilities(france, dutch);
        // The armed French colonist guarding the square would block capture.
        model.getUnitById(6).placeToLocation(Location.of(18, 10));
        final Unit armed = model.addUnitOutSideColony(quebec);
        armed.takeOver(dutch);
        assertEquals(beforeOwls, planned(quebec, GoodsType.OWLS));
        final int clayBefore = clayPreview(quebec, statesman);
        final int owlsBefore = stored(quebec, GoodsType.OWLS);
        quebec.startTurn();
        assertEquals(owlsBefore + beforeOwls, stored(quebec, GoodsType.OWLS));

        ColonyBattles.capture(model, quebec, armed);
        assertSame(dutch, quebec.getOwner());
        assertEquals(afterOwls, planned(quebec, GoodsType.OWLS));
        final int clayAfter = clayPreview(quebec, statesman);
        final boolean hudsonBefore = priorMembers
                .contains(Founder.HENRY_HUDSON);
        final boolean hudsonAfter = newMembers.contains(Founder.HENRY_HUDSON);
        final int base = hudsonBefore ? clayBefore / 2 : clayBefore;
        assertTrue(base > 0,
                "The captured colony needs a clay-producing field");
        assertEquals(hudsonAfter ? base * 2 : base, clayAfter);
        final int owlsAfterCapture = stored(quebec, GoodsType.OWLS);
        quebec.startTurn();
        assertEquals(owlsAfterCapture + afterOwls,
                stored(quebec, GoodsType.OWLS));

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony reloaded = colony(resumed);
        assertEquals("Dutch", reloaded.getOwner().getName());
        assertEquals(afterOwls, planned(reloaded, GoodsType.OWLS));
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
     * Thrive colony 0 with an ordinary colonist in its small pottery and the
     * given stored clay. Hudson is either a member or not.
     */
    private Colony potterColony(final boolean hudson, final int clayStock) {
        final GameModelPo data = data(0);
        giveMembers(data, hudson ? List.of(Founder.HENRY_HUDSON) : List.of());
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(UnitType.COLONIST);
        final Colony colony = colony(load(data));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.GRAIN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CLAY, clayStock));
        final Unit worker = colony.getUnitsInsideColony().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.SMALL_POTTERY)
                        .getSlotAt(0));
        return colony;
    }

    @ParameterizedTest
    @CsvSource({ "6,3", "2,2", "0,0" })
    void hudsonDoublesFieldClayButNotPotterAmphoraeOrTheirClayInput(
            final int clayStock, final int amphorae) {
        final Colony plain = potterColony(false, clayStock);
        final Colony hudson = potterColony(true, clayStock);
        // One ordinary worker turns three clay into three amphorae; with less
        // stock the real input is the cap.
        assertEquals(amphorae, planned(plain, GoodsType.AMPHORAE));
        assertEquals(amphorae, planned(hudson, GoodsType.AMPHORAE));
        assertEquals(amphorae, plain.getGoodsStats()
                .getStatsByType(GoodsType.CLAY).getConsumed());
        assertEquals(amphorae, hudson.getGoodsStats()
                .getStatsByType(GoodsType.CLAY).getConsumed());
        final Unit plainWorker = plain
                .getConstructionByType(ConstructionType.SMALL_POTTERY)
                .getSlotAt(0).getUnit();
        final Unit hudsonWorker = hudson
                .getConstructionByType(ConstructionType.SMALL_POTTERY)
                .getSlotAt(0).getUnit();
        for (int index = 0; index < plain.getColonyFields().size(); index++) {
            // Fields double; the amphora house has no field inputs of its own.
            assertEquals(
                    plain.getColonyFields().get(index)
                            .getPotentialProduction(plainWorker, GoodsType.CLAY)
                            .getAmount() * 2,
                    hudson.getColonyFields().get(index).getPotentialProduction(
                            hudsonWorker, GoodsType.CLAY).getAmount());
        }
        plain.startTurn();
        hudson.startTurn();
        assertEquals(amphorae, stored(plain, GoodsType.AMPHORAE));
        assertEquals(amphorae, stored(hudson, GoodsType.AMPHORAE));
        assertEquals(clayStock - amphorae, stored(plain, GoodsType.CLAY));
        assertEquals(clayStock - amphorae, stored(hudson, GoodsType.CLAY));
    }
}
