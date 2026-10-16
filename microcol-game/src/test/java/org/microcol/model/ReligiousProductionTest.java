package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ColonyBuildingQueueItemPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.UnitPo;

class ReligiousProductionTest {

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private int crosses(final Colony colony) {
        return colony.getGoodsStats().getStatsByType(GoodsType.CROSS)
                .getRowProduction();
    }

    private void setup(final GameModelPo data, final ConstructionType tier,
            final UnitType workerType, final boolean penn) {
        final ColonyPo colony = data.getModel().getColonies().stream()
                .filter(value -> value.getId() == 0).findFirst().orElseThrow();
        final ConstructionPo religious = colony.getConstructions().stream()
                .filter(value -> value.getType() == ConstructionType.CHAPEL)
                .findFirst().orElseThrow();
        religious.setType(tier);
        if (workerType != null) {
            final UnitPo worker = data.getModel().getUnits().stream()
                    .filter(value -> value.getId() == 0).findFirst()
                    .orElseThrow();
            colony.getColonyFields().stream().filter(
                    field -> Integer.valueOf(0).equals(field.getWorkerId()))
                    .forEach(field -> {
                        field.setWorkerId(null);
                        field.setProducedGoodsType(null);
                    });
            worker.setPlaceColonyField(null);
            worker.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
            worker.setType(workerType);
            religious.getSlots().get(0).setWorkerId(0);
        }
        if (penn) {
            final var congress = data.getModel().getGameManager().getPlayers()
                    .stream()
                    .filter(player -> player.getName()
                            .equals(colony.getOwnerName()))
                    .findFirst().orElseThrow().getCongress();
            final List<Founder> members = new ArrayList<>(
                    congress.getMembers());
            members.add(Founder.WILLIAM_PENN);
            congress.setMembers(members);
            final List<Founder> candidates = new ArrayList<>(
                    congress.getCandidates());
            candidates.set(candidates.indexOf(Founder.WILLIAM_PENN),
                    Founder.JUAN_DE_SEPULVEDA);
            congress.setCandidates(candidates);
        }
    }

    private static Stream<Arguments> tiers() {
        return Stream.of(Arguments.of(ConstructionType.CHAPEL, null, false, 1),
                Arguments.of(ConstructionType.CHURCH, null, false, 2),
                Arguments.of(ConstructionType.CATHEDRAL, null, false, 3),
                Arguments.of(ConstructionType.CHAPEL, UnitType.COLONIST, false,
                        2),
                Arguments.of(ConstructionType.CHURCH, UnitType.COLONIST, false,
                        5),
                Arguments.of(ConstructionType.CATHEDRAL, UnitType.COLONIST,
                        false, 9),
                Arguments.of(ConstructionType.CHAPEL,
                        UnitType.FIREBRAND_PREACHER, false, 3),
                Arguments.of(ConstructionType.CHURCH,
                        UnitType.FIREBRAND_PREACHER, false, 8),
                Arguments.of(ConstructionType.CATHEDRAL,
                        UnitType.FIREBRAND_PREACHER, false, 15),
                Arguments.of(ConstructionType.CHURCH, UnitType.MASTER_CARPENTER,
                        false, 5),
                Arguments.of(ConstructionType.CHAPEL, null, true, 1),
                Arguments.of(ConstructionType.CHURCH, UnitType.COLONIST, true,
                        7),
                Arguments.of(ConstructionType.CATHEDRAL, UnitType.COLONIST,
                        true, 13),
                Arguments.of(ConstructionType.CATHEDRAL,
                        UnitType.FIREBRAND_PREACHER, true, 22));
    }

    @ParameterizedTest
    @MethodSource("tiers")
    void previewAndCommittedProductionShareTierSkillAndPennRule(
            final ConstructionType tier, final UnitType worker,
            final boolean penn, final int expected) {
        final GameModelPo data = scenario();
        setup(data, tier, worker, penn);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        assertEquals(expected, crosses(colony));
        colony.startTurn();
        assertEquals(expected,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony resumed = restored.getColonyById(0).orElseThrow();
        assertEquals(expected,
                resumed.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(expected, crosses(resumed));
    }

    @Test
    void pennBonusRoundsOnceAcrossSeveralWorkersAndCanBeRemoved() {
        final GameModelPo data = scenario();
        setup(data, ConstructionType.CHURCH, UnitType.COLONIST, true);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Construction church = colony
                .getConstructionByType(ConstructionType.CHURCH);
        assertEquals(7, crosses(colony));
        final Unit second = model.addUnitOutSideColony(colony);
        second.placeToColonyStructureSlot(church.getSlotAt(1));
        assertEquals(12, crosses(colony));
        second.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(7, crosses(colony));
    }

    @Test
    void pennMembershipAppliesToEveryOwnedColonyWithoutReplayingOnLoad() {
        final GameModelPo data = scenario();
        setup(data, ConstructionType.CHURCH, null, true);
        final ColonyPo secondSaved = data.getModel().getColonies().stream()
                .filter(value -> value.getId() == 1).findFirst().orElseThrow();
        secondSaved.getConstructions().stream()
                .filter(value -> value.getType() == ConstructionType.CHAPEL)
                .findFirst().orElseThrow().setType(ConstructionType.CHURCH);
        final Model model = load(data);
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        assertEquals(first.getOwner(), second.getOwner());
        first.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
        second.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
        first.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        second.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        assertEquals(3, crosses(first));
        assertEquals(3, crosses(second));
        first.startTurn();
        second.startTurn();
        assertEquals(3,
                first.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(3,
                second.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(3, resumed.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.CROSS).getAmount());
        assertEquals(3, resumed.getColonyById(1).orElseThrow().getWarehouse()
                .getGoods(GoodsType.CROSS).getAmount());
    }

    @Test
    void churchCompletesAfterChapelProductionAndChangesNextPreview() {
        final GameModelPo data = scenario();
        final ColonyPo saved = data.getModel().getColonies().stream()
                .filter(value -> value.getId() == 0).findFirst().orElseThrow();
        final ColonyBuildingQueueItemPo project = new ColonyBuildingQueueItemPo();
        project.setType(QueueItemType.CONSTRUCTION);
        project.setId(42);
        project.setConstructionType(ConstructionType.CHURCH);
        saved.setBuildingQueue(List.of(project));
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 52));
        assertEquals(1, crosses(colony));
        colony.startTurn();
        assertEquals(ConstructionType.CHURCH, colony
                .getConstructionByType(ConstructionType.CHURCH).getType());
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(2, crosses(colony));
        colony.startTurn();
        assertEquals(3,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
    }
}
