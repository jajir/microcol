package org.microcol.gui.screen.menu;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;

public enum GameMenu implements MessageKeyResource {

    setupTitle, setupMap, setupMapOriginal, setupMapTwoIslands,
    setupMapRivalExpedition, setupMapReference, setupMapGenerated,
    setupGeneratedClimate, setupGeneratedLand, setupGeneratedSeed,
    setupClimateCold, setupClimateTemperate, setupClimateWarm, setupLandLow,
    setupLandMedium, setupLandHigh, setupGeneratedPreview,
    setupGeneratedSeedError, setupGeneratedFailed, setupNation,
    setupNationDutch, setupNationFrench, setupNationEnglish, setupNationSpanish,
    setupNationNote, setupNationReferenceNote, setupPlayerName, setupNameError,
    setupStart, mainMenu, buttonContinue, buttonRecovery, recoveryTitle,
    recoveryLatestPosition, recoveryLatestTurn, recoveryPreviousDecade,
    recoveryMissing, recoveryCorrupt, buttonLoadSave, buttonPlayCampaign,
    buttonFreeGame, buttonHallOfFame, buttonExitMicroCol, buttonSetting,
    settingTitle, buttonBack, campaignTitle, settingLanguage, settingLanguageCz,
    settingLanguageEn, settingShowGrid, settingShowFightAdvisor,
    settingAutosave, settingVersion, campaignPanelButtonBack,
    campaignPanel_default_findNewWorld, campaignPanel_default_buildArmy,
    campaignPanel_default_thrive;

    public static GameMenu get(final CampaignName campaignName,
            final String missionName) {
        if (CampaignNames.defaultCampaign.equals(campaignName)) {
            if ("findNewWorld".equals(missionName)) {
                return campaignPanel_default_findNewWorld;
            } else if ("buildArmy".equals(missionName)) {
                return campaignPanel_default_buildArmy;
            } else if ("thrive".equals(missionName)) {
                return campaignPanel_default_thrive;
            } else {
                throw new IllegalArgumentException(String
                        .format("Invalid mission name '%s'", missionName));
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid campaign name '%s'", campaignName));
        }
    }

}
