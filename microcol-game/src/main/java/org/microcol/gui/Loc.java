package org.microcol.gui;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.Calendar.Season;

/**
 * When some text localization doesn't belong to specific place that it should
 * be here.
 */
public enum Loc implements MessageKeyResource {

    ok, 
    gameTitle,
    statusBar_tile_notExplored,
    statusBar_tile_start,
    statusBar_tile_withUnit,
    statusBar_tile_unitCount,
    statusBar_era,
    statusBar_era_description,
    statusBar_gold,
    statusBar_gold_description,
    statusBar_status_description,
    statusBar_season_spring,
    statusBar_season_summer,
    statusBar_season_autumn,
    statusBar_season_winter,
    unitsPanel_description,
    unitsPanel_unexplored,
    unitsPanel_units,
    unitsPanel_availableMoves,
    unitsPanel_owner,
    unitsPanel_currentUser,
    unitsPanel_with,
    unitsPanel_empty,
    rightPanel_description,
    turnReport_title,
    turnReport_caption,
    turnReport_noEvents,
    statistics_title,
    statistics_caption,
    statistics_year,
    statistics_gold,
    statistics_militaryPower,
    statistics_wealth,
    statistics_score,
    no_2_7p,
    no_3_7p,
    no_4_7p,
    no_5_7p,
    and,
    withColony,
    ;

    private final static String SEASON_PREFIX = "statusBar_season_";
    
    public static Loc getTerrainDescription(final Season season) {
        final String key = SEASON_PREFIX+season.getKey();
        return valueOf(key);
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
    
}
