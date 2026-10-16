package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceConstructionSlotPo;

class RecruitmentLaborTest {

    private GameModelPo scenario() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() == null)
                .forEach(player -> player
                        .setRecruitment(RecruitmentPool.initial()));
        return data;
    }

    private int outdoor(final UnitType type) {
        final GameModelPo data = scenario();
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(type);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ColonyField field = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.west);
        assertSame(type, field.getUnit().orElseThrow().getType());
        return field.getProduction().orElseThrow().getAmount();
    }

    private ConstructionTurnProduction indoor(final UnitType type,
            final int ore) {
        final GameModelPo data = scenario();
        final var colony = data.getModel().getColonies().get(0);
        colony.getColonyFields().stream()
                .filter(field -> Integer.valueOf(0).equals(field.getWorkerId()))
                .forEach(field -> {
                    field.setWorkerId(null);
                    field.setProducedGoodsType(null);
                });
        final var worker = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        worker.setPlaceColonyField(null);
        worker.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        worker.setType(type);
        colony.getConstructions().stream().filter(
                value -> value.getType() == ConstructionType.BLACKSMITHS_HOUSE)
                .findFirst().orElseThrow().getSlots().stream()
                .filter(slot -> slot.getWorkerId() == null).findFirst()
                .orElseThrow().setWorkerId(0);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ConstructionSlot slot = model.getColonyById(0).orElseThrow()
                .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE)
                .getConstructionSlots().stream()
                .filter(value -> !value.isEmpty()
                        && value.getUnit().getId() == 0)
                .findFirst().orElseThrow();
        assertSame(type, slot.getUnit().getType());
        assertEquals(
                Math.max(0,
                        3 - (type == UnitType.COLONIST ? 0
                                : type == UnitType.INDENTURED_SERVANT ? 1 : 2)),
                slot.getPotentialProduction(slot.getUnit()).getAmount());
        assertEquals(
                Math.max(0,
                        3 - (type == UnitType.COLONIST ? 0
                                : type == UnitType.INDENTURED_SERVANT ? 1 : 2)),
                slot.getPotentialInput(slot.getUnit()).getAmount());
        return slot.getProduction(Goods.of(GoodsType.ORE, ore));
    }

    @Test
    void servantAndCriminalLoseOnlyIndoorOrdinaryOutputAndInputs() {
        final int normalField = outdoor(UnitType.COLONIST);
        assertEquals(normalField, outdoor(UnitType.INDENTURED_SERVANT));
        assertEquals(normalField, outdoor(UnitType.PETTY_CRIMINAL));
        assertEquals(3, indoor(UnitType.COLONIST, 10).getProducedGoods()
                .orElseThrow().getAmount());
        assertEquals(2, indoor(UnitType.INDENTURED_SERVANT, 10)
                .getProducedGoods().orElseThrow().getAmount());
        assertEquals(1, indoor(UnitType.PETTY_CRIMINAL, 10).getProducedGoods()
                .orElseThrow().getAmount());
        assertEquals(1, indoor(UnitType.PETTY_CRIMINAL, 1).getConsumedGoods()
                .orElseThrow().getAmount());
    }
}
