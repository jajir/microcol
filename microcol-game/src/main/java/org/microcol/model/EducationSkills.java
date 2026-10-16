package org.microcol.model;

import java.util.List;
import java.util.Optional;

/** The accepted school matrix and productive-job specialties. */
public final class EducationSkills {
    private static final List<UnitType> SCHOOLHOUSE = List.of(
            UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
            UnitType.EXPERT_FUR_TRAPPER, UnitType.EXPERT_SILVER_MINER,
            UnitType.EXPERT_LUMBERJACK, UnitType.EXPERT_ORE_MINER,
            UnitType.MASTER_CARPENTER, UnitType.SEASONED_SCOUT,
            UnitType.HARDY_PIONEER);
    private static final List<UnitType> COLLEGE = List.of(
            UnitType.MASTER_SUGAR_PLANTER, UnitType.MASTER_COTTON_PLANTER,
            UnitType.MASTER_TOBACCO_PLANTER, UnitType.MASTER_DISTILLER,
            UnitType.MASTER_WEAVER, UnitType.MASTER_TOBACCONIST,
            UnitType.MASTER_FUR_TRADER, UnitType.MASTER_BLACKSMITH,
            UnitType.MASTER_GUNSMITH, UnitType.VETERAN_SOLDIER);
    private static final List<UnitType> UNIVERSITY = List.of(
            UnitType.FIREBRAND_PREACHER, UnitType.ELDER_STATESMAN,
            UnitType.JESUIT_MISSIONARY);

    private EducationSkills() {
    }

    static int tier(final UnitType skill) {
        if (SCHOOLHOUSE.contains(skill)) {
            return 1;
        }
        if (COLLEGE.contains(skill)) {
            return 2;
        }
        if (UNIVERSITY.contains(skill)) {
            return 3;
        }
        return 0;
    }

    /** @return whether the type is in the accepted school curriculum */
    public static boolean isTeachingSkill(final UnitType skill) {
        return tier(skill) > 0;
    }

    static int tier(final ConstructionType building) {
        if (building == ConstructionType.SCHOOLHOUSE) {
            return 1;
        }
        if (building == ConstructionType.COLLEGE) {
            return 2;
        }
        if (building == ConstructionType.UNIVERSITY) {
            return 3;
        }
        return 0;
    }

    static int requiredUpdates(final UnitType skill,
            final UnitType studentType) {
        if (studentType == UnitType.PETTY_CRIMINAL
                || studentType == UnitType.INDENTURED_SERVANT) {
            return 4;
        }
        return tier(skill) * 2 + 2;
    }

    static Optional<UnitType> forProduction(final GoodsType goods,
            final boolean waterFood) {
        if (goods == GoodsType.CORN) {
            return Optional.of(waterFood ? UnitType.EXPERT_FISHERMAN
                    : UnitType.EXPERT_FARMER);
        }
        if (goods == GoodsType.FUR) {
            return Optional.of(UnitType.EXPERT_FUR_TRAPPER);
        }
        if (goods == GoodsType.SILVER) {
            return Optional.of(UnitType.EXPERT_SILVER_MINER);
        }
        if (goods == GoodsType.LUMBER) {
            return Optional.of(UnitType.EXPERT_LUMBERJACK);
        }
        if (goods == GoodsType.ORE) {
            return Optional.of(UnitType.EXPERT_ORE_MINER);
        }
        if (goods == GoodsType.SUGAR) {
            return Optional.of(UnitType.MASTER_SUGAR_PLANTER);
        }
        if (goods == GoodsType.COTTON) {
            return Optional.of(UnitType.MASTER_COTTON_PLANTER);
        }
        if (goods == GoodsType.TOBACCO) {
            return Optional.of(UnitType.MASTER_TOBACCO_PLANTER);
        }
        if (goods == GoodsType.HAMMERS) {
            return Optional.of(UnitType.MASTER_CARPENTER);
        }
        if (goods == GoodsType.RUM) {
            return Optional.of(UnitType.MASTER_DISTILLER);
        }
        if (goods == GoodsType.SILK) {
            return Optional.of(UnitType.MASTER_WEAVER);
        }
        if (goods == GoodsType.CIGARS) {
            return Optional.of(UnitType.MASTER_TOBACCONIST);
        }
        if (goods == GoodsType.COAT) {
            return Optional.of(UnitType.MASTER_FUR_TRADER);
        }
        if (goods == GoodsType.TOOLS) {
            return Optional.of(UnitType.MASTER_BLACKSMITH);
        }
        if (goods == GoodsType.MUSKET) {
            return Optional.of(UnitType.MASTER_GUNSMITH);
        }
        if (goods == GoodsType.CROSS) {
            return Optional.of(UnitType.FIREBRAND_PREACHER);
        }
        if (goods == GoodsType.BELL) {
            return Optional.of(UnitType.ELDER_STATESMAN);
        }
        return Optional.empty();
    }

    public static boolean isWorkSkill(final UnitType skill) {
        return skill == UnitType.EXPERT_FARMER
                || skill == UnitType.EXPERT_FISHERMAN
                || skill == UnitType.EXPERT_FUR_TRAPPER
                || skill == UnitType.EXPERT_SILVER_MINER
                || skill == UnitType.EXPERT_LUMBERJACK
                || skill == UnitType.EXPERT_ORE_MINER
                || skill == UnitType.MASTER_SUGAR_PLANTER
                || skill == UnitType.MASTER_COTTON_PLANTER
                || skill == UnitType.MASTER_TOBACCO_PLANTER
                || skill == UnitType.MASTER_CARPENTER
                || skill == UnitType.MASTER_DISTILLER
                || skill == UnitType.MASTER_WEAVER
                || skill == UnitType.MASTER_TOBACCONIST
                || skill == UnitType.MASTER_FUR_TRADER
                || skill == UnitType.MASTER_BLACKSMITH
                || skill == UnitType.MASTER_GUNSMITH
                || skill == UnitType.FIREBRAND_PREACHER
                || skill == UnitType.ELDER_STATESMAN;
    }
}
