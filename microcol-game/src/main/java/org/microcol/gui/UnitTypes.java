package org.microcol.gui;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.UnitType;

/**
 * Define keys for terrain names.
 */
public enum UnitTypes implements MessageKeyResource {

    CARAVEL_name, CARAVEL_name_7, CARAVEL_name_7pl, MERCHANTMAN_name,
    MERCHANTMAN_name_7, MERCHANTMAN_name_7pl, PRIVATEER_name, PRIVATEER_name_7,
    PRIVATEER_name_7pl, MAN_OF_WAR_name, MAN_OF_WAR_name_7, MAN_OF_WAR_name_7pl,
    FRIGATE_name, FRIGATE_name_7, FRIGATE_name_7pl, GALLEON_name,
    GALLEON_name_7, GALLEON_name_7pl, COLONIST_name, COLONIST_name_7,
    COLONIST_name_7pl, MASTER_BLACKSMITH_name, MASTER_BLACKSMITH_name_7,
    MASTER_BLACKSMITH_name_7pl, WAGON_name, WAGON_name_7, WAGON_name_7pl,
    EXPERT_FARMER_name, EXPERT_FARMER_name_7, EXPERT_FARMER_name_7pl,
    EXPERT_FISHERMAN_name, EXPERT_FISHERMAN_name_7, EXPERT_FISHERMAN_name_7pl,
    EXPERT_FUR_TRAPPER_name, EXPERT_FUR_TRAPPER_name_7,
    EXPERT_FUR_TRAPPER_name_7pl, EXPERT_SILVER_MINER_name,
    EXPERT_SILVER_MINER_name_7, EXPERT_SILVER_MINER_name_7pl,
    EXPERT_LUMBERJACK_name, EXPERT_LUMBERJACK_name_7,
    EXPERT_LUMBERJACK_name_7pl, EXPERT_ORE_MINER_name, EXPERT_ORE_MINER_name_7,
    EXPERT_ORE_MINER_name_7pl, MASTER_CARPENTER_name, MASTER_CARPENTER_name_7,
    MASTER_CARPENTER_name_7pl, MASTER_DISTILLER_name, MASTER_DISTILLER_name_7,
    MASTER_DISTILLER_name_7pl, MASTER_WEAVER_name, MASTER_WEAVER_name_7,
    MASTER_WEAVER_name_7pl, MASTER_TOBACCONIST_name, MASTER_TOBACCONIST_name_7,
    MASTER_TOBACCONIST_name_7pl, MASTER_FUR_TRADER_name,
    MASTER_FUR_TRADER_name_7, MASTER_FUR_TRADER_name_7pl, MASTER_GUNSMITH_name,
    MASTER_GUNSMITH_name_7, MASTER_GUNSMITH_name_7pl, FIREBRAND_PREACHER_name,
    FIREBRAND_PREACHER_name_7, FIREBRAND_PREACHER_name_7pl,
    ELDER_STATESMAN_name, ELDER_STATESMAN_name_7, ELDER_STATESMAN_name_7pl,
    SEASONED_SCOUT_name, SEASONED_SCOUT_name_7, SEASONED_SCOUT_name_7pl,
    HARDY_PIONEER_name, HARDY_PIONEER_name_7, HARDY_PIONEER_name_7pl,
    VETERAN_SOLDIER_name, VETERAN_SOLDIER_name_7, VETERAN_SOLDIER_name_7pl,
    JESUIT_MISSIONARY_name, JESUIT_MISSIONARY_name_7,
    JESUIT_MISSIONARY_name_7pl;

    private static final String UNIT_SUFFIX_NAME = "_name";

    private static final String UNIT_SUFFIX_NAME_7 = "_name_7";

    private static final String UNIT_SUFFIX_NAME_7pl = "_name_7pl";

    public static UnitTypes getUnitName(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME;
        return valueOf(key);
    }

    public static UnitTypes getUnitName7(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME_7;
        return valueOf(key);
    }

    public static UnitTypes getUnitName7pl(final UnitType unitType) {
        final String key = unitType.name() + UNIT_SUFFIX_NAME_7pl;
        return valueOf(key);
    }

}
