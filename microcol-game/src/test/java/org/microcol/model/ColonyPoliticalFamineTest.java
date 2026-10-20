package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.turnevent.TurnEventFaminePlagueColony;

/**
 * The real update takes its political snapshot before food, famine and the
 * post-famine output; the lost resident changes only the next update.
 */
class ColonyPoliticalFamineTest {

    private Model model(final long liberty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(Location.of(24, 14), TerrainType.TUNDRA);
        // Arctic center: commons yield nothing, keeping staffed arithmetic.
        terrain.put(Location.of(25, 14), TerrainType.ARCTIC);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(java.util.Collections.emptySet());
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .setLiberty(liberty);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void retain(final Model model, final Colony colony,
            final Set<Integer> selected) {
        for (final Unit unit : new ArrayList<>(colony.getUnitsInsideColony())) {
            if (!selected.contains(unit.getId())) {
                unit.getPlace().destroy();
                model.destroyUnit(unit);
            }
        }
    }

    /**
     * Output of a worked field: base, worker modifier, then political change.
     */
    private int output(final ColonyField field, final int change) {
        final Unit worker = field.getUnit().orElseThrow();
        final GoodsType goods = worker.getPlaceColonyField()
                .getProducedGoodsType();
        final int base = field.getGoodsTypeProduction(goods);
        final int worked = (int) (base * worker.getType()
                .getFieldProductionModifier(goods, field.getTerrainType()));
        return base > 0 ? Math.max(0, worked + change) : worked;
    }

    // The victim is random: the test observes whichever resident starves and
    // checks every survivor against it; it does not force a particular victim.
    @Test
    void survivorsUseTheSnapshotOfTheUpdateStartAndThePreviewUsesTheNewCount() {
        // Residents 0 (corn), 1 and 2 (tobacco). Liberty 400 is 66 % of the 600
        // points of three residents: +1 for every staffed job.
        final Model model = model(400L);
        final Colony colony = model.getColonyById(0).orElseThrow();
        retain(model, colony, Set.of(0, 1, 2));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 0));
        assertEquals(3, colony.getPopulation());
        assertEquals(1, colony.getPoliticalProduction().getBonus());
        assertEquals(1, colony.getPoliticalProduction().getToryCount());
        // Famine happens even with the bonus: 3 + 1 corn against 6 eaten.
        assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        final Map<Unit, ColonyField> fields = colony.getColonyFields().stream()
                .filter(field -> field.getUnit().isPresent()).collect(Collectors
                        .toMap(field -> field.getUnit().get(), field -> field));
        assertEquals(3, fields.size());
        for (final int tobaccoWorker : List.of(1, 2)) {
            final ColonyField field = fields
                    .get(model.getUnitById(tobaccoWorker));
            assertEquals(GoodsType.TOBACCO, field.getUnit().get()
                    .getPlaceColonyField().getProducedGoodsType());
            assertTrue(field.getGoodsTypeProduction(GoodsType.TOBACCO) > 0);
        }
        // Expected tobacco of every resident while the snapshot bonus is 1.
        final Map<Integer, Integer> snapshotTobacco = fields.entrySet().stream()
                .filter(entry -> entry.getValue().getUnit().get()
                        .getPlaceColonyField()
                        .getProducedGoodsType() == GoodsType.TOBACCO)
                .collect(Collectors.toMap(entry -> entry.getKey().getId(),
                        entry -> output(entry.getValue(), 1)));

        colony.startTurn();

        // Exactly one resident starved and the event names a former resident.
        final List<TurnEventFaminePlagueColony> losses = model
                .getTurnEventStore().getForPlayer(colony.getOwner()).stream()
                .filter(TurnEventFaminePlagueColony.class::isInstance)
                .map(TurnEventFaminePlagueColony.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, losses.size());
        final int victim = losses.get(0).getVictimId();
        assertTrue(Set.of(0, 1, 2).contains(victim));
        assertEquals(2, colony.getPopulation());
        int expectedTobacco = 0;
        for (final Map.Entry<Integer, Integer> entry : snapshotTobacco
                .entrySet()) {
            if (entry.getKey() != victim) {
                expectedTobacco += entry.getValue();
            }
        }
        // The survivors made the output of the snapshot (+1), not of two
        // residents with a full 200 of 400 points (+2).
        assertEquals(expectedTobacco,
                colony.getWarehouse().getGoods(GoodsType.TOBACCO).getAmount());
        // The next update and every preview use the changed resident count:
        // the exit capped liberty at 400, which is full support for two.
        assertEquals(2, colony.getPoliticalProduction().getBonus());
        assertEquals(0, colony.getPoliticalProduction().getToryCount());
        int previewTobacco = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            if (field.getUnit().isPresent()
                    && field.getUnit().get().getPlaceColonyField()
                            .getProducedGoodsType() == GoodsType.TOBACCO) {
                previewTobacco += field.getProduction().orElseThrow()
                        .getAmount();
                assertEquals(output(field, 2),
                        field.getProduction().orElseThrow().getAmount());
            }
        }
        assertNotEquals(expectedTobacco, previewTobacco);
    }
}
