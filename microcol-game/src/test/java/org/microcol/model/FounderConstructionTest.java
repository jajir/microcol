package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

import com.google.gson.Gson;

/** Founder membership gates construction admission and completion. */
class FounderConstructionTest {
    private static Stream<ConstructionType> gatedProjects() {
        return Stream.of(ConstructionType.IRON_WORKS,
                ConstructionType.CIGAR_FACTORY, ConstructionType.TEXTILE_MILL,
                ConstructionType.RUM_FACTORY, ConstructionType.FUR_FACTORY,
                ConstructionType.ARSENAL, ConstructionType.CUSTOM_HOUSE);
    }

    private GameModelPo fixture() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void giveFounder(final GameModelPo data, final Founder founder) {
        final var congress = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow().getCongress();
        congress.setMembers(List.of(founder));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.JAKOB_FUGGER,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setGeneration(2);
    }

    @Test
    void earnedSmithMembershipUnlocksAdmissionWithoutRebuildingColony() {
        final GameModelPo data = fixture();
        final var congress = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow().getCongress();
        congress.setBells(100);
        final Model model = load(data);
        final Player owner = model.getCurrentPlayer();
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        preparePrerequisites(model, colony, ConstructionType.IRON_WORKS);
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_FOUNDER,
                queue.getUnavailableReason(ConstructionType.IRON_WORKS)
                        .orElseThrow());
        owner.chooseFounder(owner.getFounderOffers().stream()
                .filter(offer -> offer.getFounder() == Founder.ADAM_SMITH)
                .findFirst().orElseThrow());
        owner.startTurn();
        assertTrue(owner.getCongressMembers().contains(Founder.ADAM_SMITH));
        assertTrue(queue.getUnavailableReason(ConstructionType.IRON_WORKS)
                .isEmpty());
        queue.addAtEnd(ConstructionType.IRON_WORKS);
    }

    private void preparePrerequisites(final Model model, final Colony colony,
            final ConstructionType type) {
        colony.getConstructions().removeIf(building -> type.getUpgradeChain()
                .contains(building.getType()));
        type.getUpgradeFrom().ifPresent(predecessor -> {
            if (colony.getConstructions().stream()
                    .noneMatch(building -> building.getType() == predecessor)) {
                colony.getConstructions()
                        .add(Construction.build(model, colony, predecessor));
            }
        });
        while (colony.getUnitsInsideColony().size() < type
                .getRequiredColonyPopulation()) {
            final ConstructionSlot free = colony.getConstructions().stream()
                    .flatMap(building -> building.getConstructionSlots()
                            .stream())
                    .filter(ConstructionSlot::isEmpty).findFirst()
                    .orElseThrow();
            model.addUnitOutSideColony(colony).placeToColonyStructureSlot(free);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("gatedProjects")
    void allSevenRequireTheirOwnFounderAfterOtherPrerequisites(
            final ConstructionType type) {
        final Founder required = type.getRequiredFounder().orElseThrow();
        final Model model = load(fixture());
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        preparePrerequisites(model, colony, type);
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_FOUNDER,
                queue.getUnavailableReason(type).orElseThrow());
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(type));
        assertEquals(before, new Gson().toJson(model.save()));

        final GameModelPo wrongFounder = fixture();
        giveFounder(wrongFounder,
                required == Founder.ADAM_SMITH ? Founder.PETER_STUYVESANT
                        : Founder.ADAM_SMITH);
        final Model unrelated = load(wrongFounder);
        final Colony stillLocked = unrelated.getColonyByName("Delft")
                .orElseThrow();
        preparePrerequisites(unrelated, stillLocked, type);
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_FOUNDER,
                stillLocked.getColonyBuildingQueue().getUnavailableReason(type)
                        .orElseThrow());

        final GameModelPo allowed = fixture();
        giveFounder(allowed, required);
        final Model withFounder = load(allowed);
        final Colony unlocked = withFounder.getColonyByName("Delft")
                .orElseThrow();
        preparePrerequisites(withFounder, unlocked, type);
        final ColonyBuildingQueue unlockedQueue = unlocked
                .getColonyBuildingQueue();
        assertTrue(unlockedQueue.getUnavailableReason(type).isEmpty());
        unlockedQueue.addAtEnd(type);
        assertEquals(type,
                ((ColonyBuildingItemProgressConstruction) unlockedQueue
                        .getBuildingQueue().get(0)).getConstructionType());
    }

    @Test
    void founderMembershipDoesNotWaivePredecessorOrPopulation() {
        final GameModelPo data = fixture();
        giveFounder(data, Founder.ADAM_SMITH);
        final Model model = load(data);
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        colony.getConstructions().removeIf(building -> building
                .getType() == ConstructionType.BLACKSMITHS_SHOP);
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_PREDECESSOR,
                queue.getUnavailableReason(ConstructionType.IRON_WORKS)
                        .orElseThrow());
        colony.getConstructions().add(Construction.build(model, colony,
                ConstructionType.BLACKSMITHS_SHOP));
        assertEquals(ColonyBuildingQueue.UnavailableReason.POPULATION,
                queue.getUnavailableReason(ConstructionType.IRON_WORKS)
                        .orElseThrow());
    }

    @Test
    void savedUnfinishedHeadWaitsWithoutFounderAndThenCompletesOnce() {
        final GameModelPo eligible = fixture();
        giveFounder(eligible, Founder.PETER_STUYVESANT);
        final Model admitted = load(eligible);
        final Colony colony = admitted.getColonyByName("Delft").orElseThrow();
        colony.getColonyBuildingQueue().addAtEnd(ConstructionType.CUSTOM_HOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 160));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 50));
        final ModelPo pausedPo = admitted.save();
        final var congress = pausedPo.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().getCongress();
        congress.setMembers(List.of());
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.PETER_STUYVESANT,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setGeneration(1);
        final Model paused = Model.make(pausedPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony waiting = paused.getColonyByName("Delft").orElseThrow();
        final ColonyBuildingQueue queue = waiting.getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_FOUNDER,
                queue.getCompletionUnavailableReason().orElseThrow());
        assertTrue(queue.getRushQuote().isEmpty());
        final String before = new Gson().toJson(paused.save());
        queue.startTurn();
        assertEquals(before, new Gson().toJson(paused.save()));
        assertEquals(160,
                waiting.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(50,
                waiting.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());

        final ModelPo restoredPo = paused.save();
        final var restoredCongress = restoredPo.getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("Dutch"))
                .findFirst().orElseThrow().getCongress();
        restoredCongress.setMembers(List.of(Founder.PETER_STUYVESANT));
        restoredCongress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.JAKOB_FUGGER,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        restoredCongress.setGeneration(2);
        final Model resumed = Model.make(restoredPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony completed = resumed.getColonyByName("Delft").orElseThrow();
        completed.getColonyBuildingQueue().startTurn();
        assertTrue(completed
                .isContainsConstructionByType(ConstructionType.CUSTOM_HOUSE));
        assertTrue(completed.getColonyBuildingQueue().getBuildingQueue()
                .isEmpty());
        assertEquals(0, completed.getWarehouse().getGoods(GoodsType.HAMMERS)
                .getAmount());
        assertEquals(0,
                completed.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        completed.getColonyBuildingQueue().startTurn();
        assertEquals(1,
                completed.getConstructions().stream()
                        .filter(construction -> construction
                                .getType() == ConstructionType.CUSTOM_HOUSE)
                        .count());
    }

    @Test
    void realCapturePausesCompletionAndStalesRushUntilOwnerRegainsColony() {
        final GameModelPo eligible = fixture();
        giveFounder(eligible, Founder.PETER_STUYVESANT);
        final Model model = load(eligible);
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        queue.addAtEnd(ConstructionType.CUSTOM_HOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 159));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 50));
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        final Player dutch = colony.getOwner();
        final Player king = model.getPlayerByName("Dutch's King");
        final Unit capturing = model.addUnitOutSideColony(colony);
        capturing.takeOver(king);
        ColonyBattles.capture(model, colony, capturing);
        assertSame(king, colony.getOwner());
        final String captured = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, () -> queue.rush(quote));
        assertEquals(captured, new Gson().toJson(model.save()));

        colony.getWarehouse().addGoods(Goods.of(GoodsType.HAMMERS, 1));
        final ModelPo heldPo = model.save();
        heldPo.getGameManager().setCurrentPlayer(king.getName());
        final Model held = Model.make(heldPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony capturedColony = held.getColonyByName("Delft")
                .orElseThrow();
        final ColonyBuildingQueue capturedQueue = capturedColony
                .getColonyBuildingQueue();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_FOUNDER,
                capturedQueue.getCompletionUnavailableReason().orElseThrow());
        assertTrue(capturedQueue.getRushQuote().isEmpty());
        final String waiting = new Gson().toJson(held.save());
        capturedQueue.startTurn();
        assertEquals(waiting, new Gson().toJson(held.save()));

        final Unit returning = held.getUnitById(capturing.getId());
        final Player returningDutch = held.getPlayerByName(dutch.getName());
        returning.takeOver(returningDutch);
        ColonyBattles.capture(held, capturedColony, returning);
        final ModelPo returnedPo = held.save();
        returnedPo.getGameManager().setCurrentPlayer(dutch.getName());
        final Model returned = Model.make(returnedPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony returnedColony = returned.getColonyByName("Delft")
                .orElseThrow();
        returnedColony.getColonyBuildingQueue().startTurn();
        assertTrue(returnedColony.getColonyBuildingQueue().getBuildingQueue()
                .isEmpty());
        assertTrue(returnedColony
                .isContainsConstructionByType(ConstructionType.CUSTOM_HOUSE));
        assertEquals(0, returnedColony.getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(0, returnedColony.getWarehouse().getGoods(GoodsType.TOOLS)
                .getAmount());
        returnedColony.getColonyBuildingQueue().startTurn();
        assertEquals(1, returnedColony.getConstructions().stream().filter(
                building -> building.getType() == ConstructionType.CUSTOM_HOUSE)
                .count());

        final Unit recapturing = returned.getUnitById(capturing.getId());
        recapturing.takeOver(returned.getPlayerByName("Dutch's King"));
        ColonyBattles.capture(returned, returnedColony, recapturing);
        assertSame(returned.getPlayerByName("Dutch's King"),
                returnedColony.getOwner());
        assertTrue(returnedColony
                .isContainsConstructionByType(ConstructionType.CUSTOM_HOUSE));
    }
}
