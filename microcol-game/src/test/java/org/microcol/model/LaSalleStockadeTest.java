package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlayerPo;

import com.google.gson.Gson;

/**
 * La Salle gives one free stockade to eligible colonies of the current owner.
 * Fixture colonies: 0 New Amsterdam (5 residents), 1 Delft (1), 2 Haarlem (6).
 */
class LaSalleStockadeTest {
    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    private static final List<Founder> OFFER_WITH_LA_SALLE = List.of(
            Founder.JOHN_PAUL_JONES, Founder.THOMAS_JEFFERSON,
            Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.SIEUR_DE_LA_SALLE);

    private static final List<ConstructionType> DEFENSES = List.of(
            ConstructionType.STOCKADE, ConstructionType.FORT,
            ConstructionType.FORTRESS);

    private CongressPo congress(final List<Founder> members,
            final List<Founder> offer, final Founder selected,
            final int bells) {
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setBells(bells);
        po.setMembers(members);
        po.setCandidates(offer);
        po.setSelected(selected);
        po.setGeneration(1);
        return po;
    }

    private GameModelPo data(final CongressPo dutch) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getName().equals("Dutch")) {
                player.setCongress(dutch);
            }
        }
        return data;
    }

    private GameModelPo memberData(final Founder founder) {
        return data(congress(List.of(founder), OFFER, null, 0));
    }

    private GameModelPo memberData() {
        return memberData(Founder.SIEUR_DE_LA_SALLE);
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Model member() {
        return load(memberData());
    }

    private Model loadJoining(final Founder selected) {
        final GameModelPo data = data(
                congress(List.of(), OFFER_WITH_LA_SALLE, selected, 1000));
        addDefense(data, 2, ConstructionType.FORT);
        return load(data);
    }

    private void addDefense(final GameModelPo data, final int colonyId,
            final ConstructionType defense) {
        final ConstructionPo completed = new ConstructionPo();
        completed.setType(defense);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == colonyId).findFirst()
                .orElseThrow().getConstructions().add(completed);
    }

    private Colony colony(final Model model, final int id) {
        return model.getColonyById(id).orElseThrow();
    }

    /** Forced losses, as famine does; never leaves a colony empty. */
    private void retain(final Model model, final Colony colony,
            final int number) {
        for (final Unit resident : List.copyOf(colony.getUnitsInsideColony())) {
            if (colony.getUnitsInsideColony().size() <= number) {
                break;
            }
            resident.getPlace().destroy();
            model.destroyUnit(resident);
        }
        assertEquals(number, colony.getUnitsInsideColony().size());
    }

    private Unit enterBuilding(final Model model, final Colony colony) {
        final Unit worker = model.addUnitOutSideColony(colony);
        final ConstructionSlot slot = colony.getConstructions().stream()
                .flatMap(building -> building.getConstructionSlots().stream())
                .filter(candidate -> candidate.canAccept(worker)).findFirst()
                .orElseThrow();
        worker.placeToColonyStructureSlot(slot);
        return worker;
    }

    private Unit enterField(final Model model, final Colony colony) {
        final Unit worker = model.addUnitOutSideColony(colony);
        final ColonyField field = colony.getEmptyFieldsWithMaxCornProduction()
                .stream().findFirst().orElseThrow();
        worker.placeToColonyField(field, GoodsType.CORN);
        return worker;
    }

    private long count(final Colony colony, final ConstructionType type) {
        return colony.getConstructions().stream()
                .filter(building -> building.getType() == type).count();
    }

    /** Exactly the expected defense tier exists once; null means none. */
    private void assertDefenseOnly(final Colony colony,
            final ConstructionType expected) {
        for (final ConstructionType defense : DEFENSES) {
            assertEquals(defense == expected ? 1 : 0, count(colony, defense),
                    colony.getName() + " " + defense);
        }
    }

    private String gson(final Object value) {
        return new Gson().toJson(value);
    }

    /** Warehouse and building queue, the parts a free grant must not touch. */
    private String stock(final Colony colony) {
        final ColonyPo po = colony.save();
        return gson(po.getColonyWarehouse()) + gson(po.getBuildingQueue());
    }

    private String withoutConstructions(final Colony colony) {
        final ColonyPo po = colony.save();
        po.setConstructions(List.of());
        return gson(po);
    }

    @Test
    void joiningGrantsEveryEligibleExistingColonyAndKeepsHigherDefense() {
        final Model joined = loadJoining(Founder.SIEUR_DE_LA_SALLE);
        final Model control = loadJoining(null);
        // Loading never grants: nothing exists before the actual join.
        assertDefenseOnly(colony(joined, 0), null);
        assertDefenseOnly(colony(joined, 1), null);
        assertDefenseOnly(colony(joined, 2), ConstructionType.FORT);
        assertTrue(joined.getCurrentPlayer().startTurn());
        assertTrue(control.getCurrentPlayer().startTurn());
        assertTrue(joined.getCurrentPlayer().getCongressMembers()
                .contains(Founder.SIEUR_DE_LA_SALLE));
        assertTrue(control.getCurrentPlayer().getCongressMembers().isEmpty());
        // Five residents: granted. One resident: not eligible. Fort stays.
        assertDefenseOnly(colony(joined, 0), ConstructionType.STOCKADE);
        assertDefenseOnly(colony(joined, 1), null);
        assertDefenseOnly(colony(joined, 2), ConstructionType.FORT);
        assertDefenseOnly(colony(control, 0), null);
        // The same update without the father shows no hammer, tool, queue,
        // worker or treasury effect of the grant.
        for (int id = 0; id < 3; id++) {
            assertEquals(stock(colony(control, id)), stock(colony(joined, id)));
            assertEquals(withoutConstructions(colony(control, id)),
                    withoutConstructions(colony(joined, id)));
        }
        assertEquals(control.getCurrentPlayer().getGold(),
                joined.getCurrentPlayer().getGold());
    }

    @Test
    void joiningWithFewerThanThreeResidentsGrantsNothing() {
        final Model model = loadJoining(Founder.SIEUR_DE_LA_SALLE);
        for (int id = 0; id < 3; id++) {
            retain(model, colony(model, id), id == 1 ? 1 : 2);
        }
        assertTrue(model.getCurrentPlayer().startTurn());
        assertTrue(model.getCurrentPlayer().getCongressMembers()
                .contains(Founder.SIEUR_DE_LA_SALLE));
        assertDefenseOnly(colony(model, 0), null);
        assertDefenseOnly(colony(model, 1), null);
        assertDefenseOnly(colony(model, 2), ConstructionType.FORT);
    }

    @Test
    void otherMembershipsNeverGrantAStockade() {
        final GameModelPo data = data(
                congress(List.of(), OFFER, Founder.ADAM_SMITH, 1000));
        final Model model = load(data);
        assertTrue(model.getCurrentPlayer().startTurn());
        assertTrue(model.getCurrentPlayer().getCongressMembers()
                .contains(Founder.ADAM_SMITH));
        for (int id = 0; id < 3; id++) {
            assertDefenseOnly(colony(model, id), null);
        }
        final Model fugger = load(memberData(Founder.JAKOB_FUGGER));
        final Colony colony = colony(fugger, 0);
        retain(fugger, colony, 2);
        enterBuilding(fugger, colony);
        colony.grantLaSalleStockade();
        assertDefenseOnly(colony, null);
    }

    @Test
    void canceledOrUnfinishedFounderChoiceChangesNoBuilding() {
        final Model model = load(
                data(congress(List.of(), OFFER_WITH_LA_SALLE, null, 0)));
        final Player owner = model.getCurrentPlayer();
        final String before = gson(model.save());
        // Opening the offer and canceling it only reads the model.
        final FounderOffer laSalle = owner.getFounderOffers().stream().filter(
                offer -> offer.getFounder() == Founder.SIEUR_DE_LA_SALLE)
                .findFirst().orElseThrow();
        assertTrue(owner.canChooseFounder(laSalle));
        assertEquals(before, gson(model.save()));
        final String[] buildings = new String[3];
        for (int id = 0; id < 3; id++) {
            buildings[id] = gson(colony(model, id).save().getConstructions());
        }
        // Selecting the objective is not membership: no effect yet.
        owner.chooseFounder(laSalle);
        assertEquals(Founder.SIEUR_DE_LA_SALLE,
                owner.getSelectedFounder().orElseThrow());
        assertTrue(owner.startTurn());
        assertTrue(owner.getCongressMembers().isEmpty());
        for (int id = 0; id < 3; id++) {
            assertDefenseOnly(colony(model, id), null);
            assertEquals(buildings[id],
                    gson(colony(model, id).save().getConstructions()));
        }
    }

    @Test
    void secondToThirdResidentThroughBuildingGrantsOnceWithoutSpending() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        colony.getColonyBuildingQueue().addAtEnd(ConstructionType.STOCKADE);
        retain(model, colony, 1);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 70));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 5));
        final int gold = model.getCurrentPlayer().getGold();
        final String before = stock(colony);
        enterBuilding(model, colony);
        assertEquals(2, colony.getUnitsInsideColony().size());
        assertDefenseOnly(colony, null);
        enterBuilding(model, colony);
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        // Free grant: the queued project, hammers, tools and gold are intact.
        assertEquals(before, stock(colony));
        assertEquals(gold, model.getCurrentPlayer().getGold());
        assertEquals(1,
                colony.getColonyBuildingQueue().getBuildingQueue().size());
        enterBuilding(model, colony);
        colony.getColonyBuildingQueue().startTurn();
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        assertEquals(before, stock(colony));
    }

    @Test
    void secondToThirdResidentThroughFieldGrantsOnce() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        retain(model, colony, 2);
        assertDefenseOnly(colony, null);
        final String before = stock(colony);
        enterField(model, colony);
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        assertEquals(before, stock(colony));
        enterField(model, colony);
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
    }

    @Test
    void colonyStartingWithOneResidentIsGrantedOnlyAtThree() {
        final Model model = member();
        final Colony delft = colony(model, 1);
        assertEquals(1, delft.getUnitsInsideColony().size());
        enterField(model, delft);
        assertDefenseOnly(delft, null);
        enterBuilding(model, delft);
        assertEquals(3, delft.getUnitsInsideColony().size());
        assertDefenseOnly(delft, ConstructionType.STOCKADE);
    }

    @Test
    void higherDefenseIsNeverDowngradedOrAccompaniedByStockade() {
        for (final ConstructionType defense : List.of(ConstructionType.FORT,
                ConstructionType.FORTRESS)) {
            final GameModelPo data = memberData();
            addDefense(data, 0, defense);
            final Model model = load(data);
            final Colony colony = colony(model, 0);
            final int buildings = colony.getConstructions().size();
            retain(model, colony, 2);
            enterBuilding(model, colony);
            enterField(model, colony);
            colony.grantLaSalleStockade();
            assertDefenseOnly(colony, defense);
            assertEquals(buildings, colony.getConstructions().size());
        }
    }

    @Test
    void forcedLossBelowThreeAndLaterReturnNeverDuplicateTheGrant() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        retain(model, colony, 2);
        final Unit third = enterBuilding(model, colony);
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        // A forced loss, like famine, is not blocked and keeps the stockade.
        third.getPlace().destroy();
        model.destroyUnit(third);
        assertEquals(2, colony.getUnitsInsideColony().size());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        enterBuilding(model, colony);
        assertEquals(3, colony.getUnitsInsideColony().size());
        enterField(model, colony);
        colony.grantLaSalleStockade();
        colony.grantLaSalleStockade();
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
    }

    @Test
    void grantedStockadeKeepsTheFortifiedPopulationRestriction() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        retain(model, colony, 2);
        final Unit third = enterBuilding(model, colony);
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        assertFalse(third.canLeaveColony());
        final String before = gson(model.save());
        assertThrows(IllegalStateException.class,
                () -> third.placeToMap(colony.getLocation()));
        assertEquals(before, gson(model.save()));
        // Only a fourth resident may leave; three stay protected.
        final Unit fourth = enterField(model, colony);
        assertTrue(fourth.canLeaveColony());
        fourth.placeToMap(colony.getLocation());
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertFalse(third.canLeaveColony());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
    }

    @Test
    void captureGrantsOnlyForNewOwnerWithLaSalleAndNeverDuplicates() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        final Player dutch = colony.getOwner();
        final Player king = model.getPlayerByName("Dutch's King");
        assertDefenseOnly(colony, null);
        final Unit force = model.addUnitOutSideColony(colony);
        force.takeOver(king);
        colony.captureColony(force);
        assertSame(king, colony.getOwner());
        // The new owner has no La Salle, whatever the previous owner had.
        assertDefenseOnly(colony, null);
        force.takeOver(dutch);
        colony.captureColony(force);
        assertSame(dutch, colony.getOwner());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        // Existing defense stays in both directions; no second building.
        force.takeOver(king);
        colony.captureColony(force);
        assertSame(king, colony.getOwner());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        force.takeOver(dutch);
        colony.captureColony(force);
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
    }

    @Test
    void captureOfSmallColonyOrFortifiedColonyGrantsNothingMore() {
        final GameModelPo data = memberData();
        addDefense(data, 2, ConstructionType.FORTRESS);
        final Model model = load(data);
        final Player dutch = model.getCurrentPlayer();
        final Player king = model.getPlayerByName("Dutch's King");
        for (final int id : new int[] { 1, 2 }) {
            final Colony colony = colony(model, id);
            final Unit force = model.addUnitOutSideColony(colony);
            force.takeOver(king);
            colony.captureColony(force);
            force.takeOver(dutch);
            colony.captureColony(force);
            assertSame(dutch, colony.getOwner());
        }
        // One resident is below the threshold; a fortress is never lowered.
        assertDefenseOnly(colony(model, 1), null);
        assertDefenseOnly(colony(model, 2), ConstructionType.FORTRESS);
    }

    @Test
    void loadNeverMutatesAndNextCompletedPlacementGrantsEligibleColony() {
        final Model model = member();
        assertDefenseOnly(colony(model, 0), null);
        final String saved = gson(model.save());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(saved, gson(restored.save()));
        restored.startGame();
        assertEquals(saved, gson(restored.save()));
        assertDefenseOnly(colony(restored, 0), null);
        // Rearranging an already eligible colony completes a placement.
        final Colony colony = colony(restored, 0);
        final Unit worker = restored.getUnitById(4);
        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getFirstEmptySlot().orElseThrow());
        assertEquals(5, colony.getUnitsInsideColony().size());
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
    }

    @Test
    void savedGrantSurvivesRestoreWithoutDuplicateOrMutation() {
        final Model model = member();
        final Colony colony = colony(model, 0);
        retain(model, colony, 2);
        enterBuilding(model, colony);
        assertDefenseOnly(colony, ConstructionType.STOCKADE);
        final String saved = gson(model.save());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony again = colony(restored, 0);
        assertDefenseOnly(again, ConstructionType.STOCKADE);
        assertEquals(saved, gson(restored.save()));
        assertTrue(restored.getCurrentPlayer().getCongressMembers()
                .contains(Founder.SIEUR_DE_LA_SALLE));
        // Reload and later checks cannot create a second stockade.
        again.grantLaSalleStockade();
        enterField(restored, again);
        assertEquals(4, again.getUnitsInsideColony().size());
        assertDefenseOnly(again, ConstructionType.STOCKADE);
    }

    @Test
    void destroyedColonyIsIgnored() {
        final Model model = member();
        final Colony delft = colony(model, 1);
        final Unit last = delft.getUnitsInsideColony().get(0);
        last.placeToMap(delft.getLocation());
        assertFalse(delft.isValid());
        final String before = gson(model.save());
        delft.grantLaSalleStockade();
        assertEquals(before, gson(model.save()));
        assertDefenseOnly(delft, null);
    }
}
