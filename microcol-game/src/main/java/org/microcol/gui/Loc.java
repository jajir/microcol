package org.microcol.gui;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.Calendar.Season;

/**
 * When some text localization doesn't belong to specific place that it should
 * be here.
 */
public enum Loc implements MessageKeyResource {

    ok, gameTitle, statusBar_tile_notExplored, statusBar_tile_start,
    statusBar_tile_withUnit, statusBar_tile_unitCount, statusBar_era,
    statusBar_era_description, statusBar_gold, statusBar_gold_description,
    statusBar_status_description, statusBar_season_spring,
    statusBar_season_summer, statusBar_season_autumn, statusBar_season_winter,
    unitsPanel_description, unitsPanel_unexplored, unitsPanel_units,
    unitsPanel_veteran, unitsPanel_continental, unitsPanel_royal,
    unitsPanel_availableMoves, unitsPanel_owner, unitsPanel_currentUser,
    unitsPanel_with, unitsPanel_empty, unitsPanel_roadWork,
    rightPanel_description, turnReport_title, turnReport_caption,
    turnReport_noEvents, no_2_7p, no_3_7p, no_4_7p, no_5_7p, and, withColony,
    colonyLastSeen, adjustAmountOfGoods, moveCost, difficulty,
    lostCityRumorStatus, nativeSettlementStatus, nativeCapitalStatus,
    nativeBraveHome, nativeBraveHomeCapital, nativeBraveHomeUnknown,
    nativeBraveHomeless, nativeNationArawak, nativeNationAztec,
    nativeNationInca, nativeNationTupi, nativeNationCherokee,
    nativeNationIroquois, nativeNationSioux, nativeNationApache,
    nativeClassCamp, nativeClassVillage, nativeClassCity, nativeLandClaimant,
    nativeLandClaimantCapital, nativeLandHoverUnpaid, nativeLandHoverPaid,
    nativeLandHoverWaived, nativeLandTitle, nativeLandClaimedBy,
    nativeLandPrice, nativeLandConsequence, nativeLandPay, nativeLandProceed,
    nativeLandCancel, nativeLandCannotPay, nativeLandChanged,
    nativeLandColonySquare, nativeLandFirstField, nativeLandFoundingNote,
    nativeLandFoundingPay, nativeLandFoundingUnpaid, nativeLandPaidKept,
    nativeAttitudeContent, nativeAttitudeUneasy, nativeAttitudeAngry,
    nativeAttitudeHostile, nativeTrendRising, nativeTrendFalling,
    nativeTrendStable, nativeAttitudeLine, nativeAlarmChangeLine,
    nativeRivalProvoker, nativeRivalUnknown, nativeDetailsHint,
    nativeSettlementsTitle, nativeSettlementsNone, nativeVisitTitle,
    nativeVisitIntro, nativeVisitSkillLine, nativeVisitActionScoutChief,
    nativeVisitActionTeaching, nativeVisitActionTrade, nativeVisitActionTribute,
    nativeVisitActionMission, nativeVisitActionAttack, nativeVisitLine,
    nativeVisitLimitRoleRefused, nativeVisitLimitRelationRefused,
    nativeVisitLimitNotYetAvailable, nativeVisitLimitAvailable,
    nativeVisitLimitSettlementOccupied, nativeVisitTeachButton,
    nativeTeachingTitle, nativeTeachingLine, nativeTeachingConfirm,
    nativeTeachingBack, nativeTeachingDone, nativeTeachingHostility,
    nativeTeachingProgress, nativeTeachingStop, nativeTeachingRejected,
    nativeTeachingNoSquare, nativeTeachingCancelRejected,
    nativeTeachingWaitingCompleted, nativeTeachingWaitingCanceled,
    nativeTeachingLeave, nativeMarketTitle, nativeMarketRejected,
    nativeMarketContactKnown, nativeMarketContactNone,
    nativeMarketIntroduceButton, nativeMarketOpenButton, nativeMarketAdmitted,
    nativeMarketNoContact, nativeMarketNotTrader, nativeMarketNoGoods,
    nativeMarketShipNotCoastal, nativeMarketRelationTense,
    nativeMarketShipUneasy, nativeMarketExpeditionOpen,
    nativeMarketExpeditionClosed, nativeMarketOfferLine,
    nativeMarketOfferUnavailable, nativeMarketWantedLine,
    nativeMarketTreasuryLine, nativeSaleRejected, nativeSaleSellButton,
    nativeSaleGiftButton, nativeSaleHaggleButton, nativeSaleGoodsLabel,
    nativeSaleQuantityLabel, nativeSalePreviewSale, nativeSalePreviewGift,
    nativeSaleHaggleOffered, nativeSaleHaggleAccepted, nativeSaleHaggleRefused,
    nativeSaleHaggleUnavailable, nativeSaleNoGoods, nativeSaleNeedsExpedition,
    nativeSaleNoLimit, nativeSaleConfirmTitle, nativeSaleConfirmBack,
    nativeSaleConfirmSale, nativeSaleConfirmGift, nativePurchaseHeader,
    nativePurchaseGoodsLabel, nativePurchaseQuantityLabel,
    nativePurchaseOfferLine, nativePurchasePreview, nativePurchaseHaggleOffered,
    nativePurchaseHaggleAccepted, nativePurchaseHaggleRefused,
    nativePurchaseBuyButton, nativePurchaseHaggleButton,
    nativePurchaseDeclineButton, nativePurchaseNoStock, nativePurchaseNotOpen,
    nativePurchaseNoLimit, nativePurchaseConfirmTitle,
    nativePurchaseConfirmButton, nativePurchaseConfirmBack,
    nativePurchaseBought, nativePurchaseRejected, nativePurchaseDeclined,
    difficulty_discoverer, difficulty_explorer, difficulty_conquistador,
    difficulty_governor, difficulty_viceroy,;

    private final static String SEASON_PREFIX = "statusBar_season_";

    public static Loc getTerrainDescription(final Season season) {
        final String key = SEASON_PREFIX + season.getKey();
        return valueOf(key);
    }

}
