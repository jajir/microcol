package org.akropia.model;

import java.util.List;
import java.util.Optional;

/** The accepted school matrix and productive-job specialties. */
public final class EducationSkills {
    private static final List<UnitType> SCHOOLHOUSE = List.of(
            UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
            UnitType.EXPERT_CLAY_DIGGER, UnitType.EXPERT_SILVER_MINER,
            UnitType.EXPERT_LUMBERJACK, UnitType.EXPERT_ORE_MINER,
            UnitType.MASTER_CARPENTER, UnitType.SEASONED_SCOUT,
            UnitType.HARDY_PIONEER);
    private static final List<UnitType> COLLEGE = List.of(
            UnitType.MASTER_OLIVE_GROWER, UnitType.MASTER_SHEPHERD,
            UnitType.MASTER_GRAPE_GROWER, UnitType.MASTER_OIL_MAKER,
            UnitType.MASTER_WEAVER, UnitType.MASTER_WINEMAKER,
            UnitType.MASTER_POTTER, UnitType.MASTER_BLACKSMITH,
            UnitType.MASTER_WEAPONSMITH, UnitType.VETERAN_SOLDIER);
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
        if (goods == GoodsType.GRAIN) {
            return Optional.of(waterFood ? UnitType.EXPERT_FISHERMAN
                    : UnitType.EXPERT_FARMER);
        }
        if (goods == GoodsType.CLAY) {
            return Optional.of(UnitType.EXPERT_CLAY_DIGGER);
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
        if (goods == GoodsType.OLIVES) {
            return Optional.of(UnitType.MASTER_OLIVE_GROWER);
        }
        if (goods == GoodsType.WOOL) {
            return Optional.of(UnitType.MASTER_SHEPHERD);
        }
        if (goods == GoodsType.GRAPES) {
            return Optional.of(UnitType.MASTER_GRAPE_GROWER);
        }
        if (goods == GoodsType.HAMMERS) {
            return Optional.of(UnitType.MASTER_CARPENTER);
        }
        if (goods == GoodsType.OLIVE_OIL) {
            return Optional.of(UnitType.MASTER_OIL_MAKER);
        }
        if (goods == GoodsType.CLOTH) {
            return Optional.of(UnitType.MASTER_WEAVER);
        }
        if (goods == GoodsType.WINE) {
            return Optional.of(UnitType.MASTER_WINEMAKER);
        }
        if (goods == GoodsType.AMPHORAE) {
            return Optional.of(UnitType.MASTER_POTTER);
        }
        if (goods == GoodsType.TOOLS) {
            return Optional.of(UnitType.MASTER_BLACKSMITH);
        }
        if (goods == GoodsType.WEAPONS) {
            return Optional.of(UnitType.MASTER_WEAPONSMITH);
        }
        if (goods == GoodsType.TORCHES) {
            return Optional.of(UnitType.FIREBRAND_PREACHER);
        }
        if (goods == GoodsType.OWLS) {
            return Optional.of(UnitType.ELDER_STATESMAN);
        }
        return Optional.empty();
    }

    public static boolean isWorkSkill(final UnitType skill) {
        return skill == UnitType.EXPERT_FARMER
                || skill == UnitType.EXPERT_FISHERMAN
                || skill == UnitType.EXPERT_CLAY_DIGGER
                || skill == UnitType.EXPERT_SILVER_MINER
                || skill == UnitType.EXPERT_LUMBERJACK
                || skill == UnitType.EXPERT_ORE_MINER
                || skill == UnitType.MASTER_OLIVE_GROWER
                || skill == UnitType.MASTER_SHEPHERD
                || skill == UnitType.MASTER_GRAPE_GROWER
                || skill == UnitType.MASTER_CARPENTER
                || skill == UnitType.MASTER_OIL_MAKER
                || skill == UnitType.MASTER_WEAVER
                || skill == UnitType.MASTER_WINEMAKER
                || skill == UnitType.MASTER_POTTER
                || skill == UnitType.MASTER_BLACKSMITH
                || skill == UnitType.MASTER_WEAPONSMITH
                || skill == UnitType.FIREBRAND_PREACHER
                || skill == UnitType.ELDER_STATESMAN;
    }
}
