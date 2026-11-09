package org.akropia.gui.screen.editor;

import org.akropia.i18n.MessageKeyResource;
import org.akropia.model.generation.ScenarioProblem;

/**
 * Texts of the developer map editor (R-0435).
 */
public enum EditorText implements MessageKeyResource {

    topNew, topOpen, topSave, topPlay, topBack, untitled, titleSaved,
    titleModified, nation, tools, toolSelect, toolTerrain, toolForest,
    toolField, toolRiver, riverSmall, riverLarge, toolBonus, toolSettlement,
    toolUnit, capital, tile, tileNone, remove, settlement, tribe, skill, apply,
    units, unitLabel, passengers, addPassenger, newTitle, newWidth, newHeight,
    newBounds, newConfirm, newCancel, unsaved, openTitle, saveTitle, created,
    opened, saved, saveCanceled, openCanceled, openFailed, saveFailed, changed,
    rejected, playRejected, playFailed, problemAt, problemDetail,
    reason_DIMENSION_NOT_NUMBER, reason_DIMENSION_OUT_OF_RANGE,
    reason_STRUCTURE, reason_OUTSIDE_MAP, reason_NEEDS_LAND, reason_GEOGRAPHY,
    reason_OCCUPIED, reason_SETTLEMENT_SITE, reason_SECOND_CAPITAL,
    reason_UNIT_TERRAIN, reason_UNIT_TYPE, reason_NOT_A_SHIP, reason_SHIP_FULL,
    reason_NATION_TAKEN, reason_NATION_FIXED, reason_NO_EXPEDITION,
    reason_NO_CAPITAL, reason_GAME_REJECTED;

    /**
     * @return text naming the reason
     */
    public static EditorText of(final ScenarioProblem.Reason reason) {
        return valueOf("reason_" + reason.name());
    }

}
