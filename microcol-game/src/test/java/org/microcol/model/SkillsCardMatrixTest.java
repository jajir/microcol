package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

/**
 * R-0212 card rows for all twenty-two skills. Every expected value is a literal
 * from the skills card (PDF 133) or the accepted economy rules; none is read
 * back from the tables it checks.
 */
class SkillsCardMatrixTest {

    private static final Set<UnitType> PLANTERS = Set.of(
            UnitType.MASTER_SUGAR_PLANTER, UnitType.MASTER_COTTON_PLANTER,
            UnitType.MASTER_TOBACCO_PLANTER);

    private Model scenario() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Card tier (1 school, 2 college, 3 university), native teaching mark and
     * Europe price; price 0 means the skill is not offered in Europe.
     */
    @ParameterizedTest
    @CsvSource({ "EXPERT_FARMER,1,true,1000", "EXPERT_FISHERMAN,1,true,1000",
            "EXPERT_FUR_TRAPPER,1,true,1000", "EXPERT_SILVER_MINER,1,true,1000",
            "EXPERT_LUMBERJACK,1,false,1000", "EXPERT_ORE_MINER,1,false,1000",
            "MASTER_SUGAR_PLANTER,2,true,0", "MASTER_COTTON_PLANTER,2,true,0",
            "MASTER_TOBACCO_PLANTER,2,true,0",
            "FIREBRAND_PREACHER,3,false,3000", "ELDER_STATESMAN,3,false,3000",
            "MASTER_CARPENTER,1,false,1000", "MASTER_DISTILLER,2,false,2000",
            "MASTER_WEAVER,2,false,2000", "MASTER_TOBACCONIST,2,false,2000",
            "MASTER_FUR_TRADER,2,false,2000", "MASTER_BLACKSMITH,2,false,2200",
            "MASTER_GUNSMITH,2,false,2000", "SEASONED_SCOUT,1,true,1000",
            "HARDY_PIONEER,1,false,1000", "VETERAN_SOLDIER,2,false,2000",
            "JESUIT_MISSIONARY,3,false,3000" })
    void cardRowAgreesWithTierNativeTeachingAndEuropeanOffer(final String name,
            final int tier, final boolean nativeTeachable, final int price) {
        final UnitType skill = UnitType.valueOf(name);
        assertEquals(tier, EducationSkills.tier(skill), name);
        assertEquals(nativeTeachable,
                UnitType.NATIVE_TEACHABLE_SKILLS.contains(skill), name);
        assertEquals(price, skill.getEuropePrice(), name);
        assertEquals(price > 0, UnitType.EUROPEAN_SPECIALISTS.contains(skill),
                name);
    }

    /**
     * The purchase command charges the card price once and refuses one gold
     * less, leaving the treasury unchanged.
     */
    @ParameterizedTest
    @CsvSource({ "EXPERT_FARMER,1000", "EXPERT_FISHERMAN,1000",
            "EXPERT_FUR_TRAPPER,1000", "EXPERT_SILVER_MINER,1000",
            "EXPERT_LUMBERJACK,1000", "EXPERT_ORE_MINER,1000",
            "MASTER_CARPENTER,1000", "FIREBRAND_PREACHER,3000",
            "ELDER_STATESMAN,3000", "MASTER_DISTILLER,2000",
            "MASTER_WEAVER,2000", "MASTER_TOBACCONIST,2000",
            "MASTER_FUR_TRADER,2000", "MASTER_BLACKSMITH,2200",
            "MASTER_GUNSMITH,2000", "SEASONED_SCOUT,1000", "HARDY_PIONEER,1000",
            "VETERAN_SOLDIER,2000", "JESUIT_MISSIONARY,3000" })
    void europeanPurchaseChargesTheCardPriceOnceAndRefusesOneGoldLess(
            final String name, final int price) {
        final UnitType skill = UnitType.valueOf(name);
        final Player buyer = scenario().getPlayerByName("Dutch");
        buyer.setGold(price - 1);
        assertFalse(buyer.canBuyUnit(skill));
        assertThrows(NotEnoughtGoldException.class, () -> buyer.buy(skill));
        assertEquals(price - 1, buyer.getGold());
        buyer.setGold(price);
        assertTrue(buyer.canBuyUnit(skill));
        buyer.buy(skill);
        assertEquals(0, buyer.getGold());
        assertTrue(buyer.getAllUnits().stream()
                .anyMatch(unit -> unit.getType() == skill));
    }

    /**
     * Unsupported combination: the planters are school and native skills only,
     * so Europe refuses them even with ample gold.
     */
    @ParameterizedTest
    @ValueSource(strings = { "MASTER_SUGAR_PLANTER", "MASTER_COTTON_PLANTER",
            "MASTER_TOBACCO_PLANTER" })
    void plantersAreRefusedInEuropeEvenWithAmpleGold(final String name) {
        final UnitType planter = UnitType.valueOf(name);
        final Player buyer = scenario().getPlayerByName("Dutch");
        buyer.setGold(10_000);
        assertFalse(buyer.canBuyUnit(planter));
        assertThrows(IllegalArgumentException.class, () -> buyer.buy(planter));
        assertEquals(10_000, buyer.getGold());
    }

    /**
     * Immigration: the recruitment pool draws its expert candidates only from
     * the nineteen offered specialists, in both the paid and Brewster paths.
     */
    @Test
    void recruitmentDrawsNeverProduceAPlanter() {
        assertEquals(19, UnitType.EUROPEAN_SPECIALISTS.size());
        for (int index = 0; index < 19; index++) {
            final UnitType drawn = RecruitmentPool.draw(90, index);
            final UnitType brewster = RecruitmentPool.draw(90, index, true);
            assertTrue(UnitType.EUROPEAN_SPECIALISTS.contains(drawn));
            assertTrue(UnitType.EUROPEAN_SPECIALISTS.contains(brewster));
            assertFalse(PLANTERS.contains(drawn));
            assertFalse(PLANTERS.contains(brewster));
        }
    }
}
