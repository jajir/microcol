package org.akropia.gui.shortcut;

/**
 * Every configurable keyboard action. The key names the action in the default
 * profiles and in the saved player deviations; it must stay stable.
 */
public enum ShortcutAction {

    DEVELOPER_MODE("global.developerMode", ShortcutContext.GLOBAL,
            ShortcutText.actionDeveloperMode, Kind.RELEASE),

    LOAD_GAME("session.load", ShortcutContext.SESSION,
            ShortcutText.actionLoadGame, Kind.PRESS),
    SAVE_GAME("session.save", ShortcutContext.SESSION,
            ShortcutText.actionSaveGame, Kind.PRESS),

    OPEN_SETTINGS("session.settings", ShortcutContext.SESSION,
            ShortcutText.actionOpenSettings, Kind.PRESS),
    QUIT_GAME("session.quit", ShortcutContext.SESSION,
            ShortcutText.actionQuitGame, Kind.PRESS),
    CONTINUE_GAME("menu.continue", ShortcutContext.MAIN_MENU,
            ShortcutText.actionContinueGame, Kind.PRESS),
    SECRET_OPTION("menu.secretOption", ShortcutContext.MAIN_MENU,
            ShortcutText.actionSecretOption, Kind.HOLD),

    NEW_FREE_GAME("menu.freeGame", ShortcutContext.MAIN_MENU,
            ShortcutText.actionNewFreeGame, Kind.PRESS),
    PLAY_CAMPAIGN("menu.campaign", ShortcutContext.MAIN_MENU,
            ShortcutText.actionPlayCampaign, Kind.PRESS),
    HALL_OF_FAME("menu.hallOfFame", ShortcutContext.MAIN_MENU,
            ShortcutText.actionHallOfFame, Kind.PRESS),
    QUICK_SAVE("map.quickSave", ShortcutContext.MAP,
            ShortcutText.actionQuickSave, Kind.PRESS),
    QUICK_LOAD("map.quickLoad", ShortcutContext.MAP,
            ShortcutText.actionQuickLoad, Kind.PRESS),
    END_TURN("map.endTurn", ShortcutContext.MAP, ShortcutText.actionEndTurn,
            Kind.PRESS),
    NEXT_UNIT("map.nextUnit", ShortcutContext.MAP, ShortcutText.actionNextUnit,
            Kind.UNIT_PRESS),
    SKIP_UNIT("map.skipUnit", ShortcutContext.MAP, ShortcutText.actionSkipUnit,
            Kind.UNIT_RELEASE),
    MOVE("map.move", ShortcutContext.MAP, ShortcutText.actionMove,
            Kind.UNIT_PRESS),
    CONFIRM_MOVE("map.confirmMove", ShortcutContext.MAP,
            ShortcutText.actionConfirmMove, Kind.UNIT_PRESS),
    GO_TO("map.goTo", ShortcutContext.MAP, ShortcutText.actionGoTo,
            Kind.UNIT_PRESS),
    GO_TO_TILE("map.goToTile", ShortcutContext.MAP, ShortcutText.actionGoToTile,
            Kind.UNIT_PRESS),
    BUILD_COLONY("map.buildColony", ShortcutContext.MAP,
            ShortcutText.actionBuildColony, Kind.UNIT_PRESS),
    PLOW("map.plow", ShortcutContext.MAP, ShortcutText.actionPlow,
            Kind.UNIT_PRESS),
    CLEAR_FOREST("map.clearForest", ShortcutContext.MAP,
            ShortcutText.actionClearForest, Kind.UNIT_PRESS),
    BUILD_ROAD("map.buildRoad", ShortcutContext.MAP,
            ShortcutText.actionBuildRoad, Kind.UNIT_PRESS),
    FORTIFY("map.fortify", ShortcutContext.MAP, ShortcutText.actionFortify,
            Kind.UNIT_PRESS),
    SENTRY("map.sentry", ShortcutContext.MAP, ShortcutText.actionSentry,
            Kind.UNIT_PRESS),
    TRADE_ROUTES("map.tradeRoutes", ShortcutContext.MAP,
            ShortcutText.actionTradeRoutes, Kind.PRESS),
    EXIT_TO_MAIN_MENU("map.mainMenu", ShortcutContext.MAP,
            ShortcutText.actionExitToMainMenu, Kind.PRESS),
    RESTART_MISSION("map.restartMission", ShortcutContext.MAP,
            ShortcutText.actionRestartMission, Kind.PRESS),
    DECLARE_INDEPENDENCE("map.declareIndependence", ShortcutContext.MAP,
            ShortcutText.actionDeclareIndependence, Kind.PRESS),
    RETIRE("map.retire", ShortcutContext.MAP, ShortcutText.actionRetire,
            Kind.PRESS),
    DISBAND("map.disband", ShortcutContext.MAP, ShortcutText.actionDisband,
            Kind.UNIT_PRESS),
    JETTISON("map.jettison", ShortcutContext.MAP, ShortcutText.actionJettison,
            Kind.UNIT_PRESS),
    NEW_TRADE_ROUTE("map.newTradeRoute", ShortcutContext.MAP,
            ShortcutText.actionNewTradeRoute, Kind.PRESS),
    CENTER_VIEW("map.center", ShortcutContext.MAP,
            ShortcutText.actionCenterView, Kind.PRESS),
    COLONIES("map.colonies", ShortcutContext.MAP, ShortcutText.actionColonies,
            Kind.PRESS),
    EUROPE("map.europe", ShortcutContext.MAP, ShortcutText.actionEurope,
            Kind.PRESS),
    CONGRESS("map.congress", ShortcutContext.MAP, ShortcutText.actionCongress,
            Kind.PRESS),
    NATIVE_SETTLEMENTS("map.nativeSettlements", ShortcutContext.MAP,
            ShortcutText.actionNativeSettlements, Kind.PRESS),
    STATISTICS("map.statistics", ShortcutContext.MAP,
            ShortcutText.actionStatistics, Kind.PRESS),
    TURN_REPORT("map.turnReport", ShortcutContext.MAP,
            ShortcutText.actionTurnReport, Kind.PRESS),
    GOALS("map.goals", ShortcutContext.MAP, ShortcutText.actionGoals,
            Kind.PRESS),
    HELP("map.help", ShortcutContext.MAP, ShortcutText.actionHelp, Kind.PRESS),
    SCROLL_UP("map.scrollUp", ShortcutContext.MAP, ShortcutText.actionScrollUp,
            Kind.HOLD),
    SCROLL_LEFT("map.scrollLeft", ShortcutContext.MAP,
            ShortcutText.actionScrollLeft, Kind.HOLD),
    SCROLL_DOWN("map.scrollDown", ShortcutContext.MAP,
            ShortcutText.actionScrollDown, Kind.HOLD),
    SCROLL_RIGHT("map.scrollRight", ShortcutContext.MAP,
            ShortcutText.actionScrollRight, Kind.HOLD),

    EUROPE_RECRUIT("europe.recruit", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeRecruit, Kind.PRESS),
    EUROPE_BUY_UNIT("europe.buyUnit", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeBuyUnit, Kind.PRESS),
    EUROPE_REPAIRS("europe.repairs", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeRepairs, Kind.PRESS),
    EUROPE_TREASURE("europe.treasure", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeTreasure, Kind.PRESS),
    EUROPE_TAX("europe.taxDemand", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeTax, Kind.PRESS),
    EUROPE_BACK_TAX("europe.backTax", ShortcutContext.EUROPE,
            ShortcutText.actionEuropeBackTax, Kind.PRESS),
    COLONY_CONSTRUCTION("colony.construction", ShortcutContext.COLONY,
            ShortcutText.actionColonyConstruction, Kind.PRESS),
    COLONY_EDUCATION("colony.education", ShortcutContext.COLONY,
            ShortcutText.actionColonyEducation, Kind.PRESS),
    COLONY_CUSTOM_HOUSE("colony.customHouse", ShortcutContext.COLONY,
            ShortcutText.actionColonyCustomHouse, Kind.PRESS),
    COLONY_TRADE_ROUTE("colony.tradeRoute", ShortcutContext.COLONY,
            ShortcutText.actionColonyTradeRoute, Kind.PRESS),
    COLONY_ROUTE_CARGO("colony.routeCargo", ShortcutContext.COLONY,
            ShortcutText.actionColonyRouteCargo, Kind.PRESS),
    COLONY_TREASURE("colony.treasure", ShortcutContext.COLONY,
            ShortcutText.actionColonyTreasure, Kind.PRESS),
    EDITOR_SAVE("editor.save", ShortcutContext.EDITOR,
            ShortcutText.actionEditorSave, Kind.PRESS);

    /**
     * When the action runs.
     */
    public enum Kind {
        /** Runs once on key press. */
        PRESS,
        /** Runs once on key release; the press is consumed. */
        RELEASE,
        /** Active from press until the key is released. */
        HOLD,
        /** Unit order on press; the route planner gives none. */
        UNIT_PRESS,
        /** Unit order on release; the route planner gives none. */
        UNIT_RELEASE
    }

    private final String key;
    private final ShortcutContext context;
    private final ShortcutText label;
    private final Kind kind;

    ShortcutAction(final String key, final ShortcutContext context,
            final ShortcutText label, final Kind kind) {
        this.key = key;
        this.context = context;
        this.label = label;
        this.kind = kind;
    }

    /**
     * @return stable property key of the action
     */
    public String getKey() {
        return key;
    }

    public ShortcutContext getContext() {
        return context;
    }

    public ShortcutText getLabel() {
        return label;
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * @return <code>true</code> for orders given to the selected unit
     */
    public boolean isUnitOrder() {
        return kind == Kind.UNIT_PRESS || kind == Kind.UNIT_RELEASE;
    }

    /**
     * @param key property key
     * @return action with the key or <code>null</code>
     */
    public static ShortcutAction byKey(final String key) {
        for (final ShortcutAction action : values()) {
            if (action.key.equals(key)) {
                return action;
            }
        }
        return null;
    }

}
