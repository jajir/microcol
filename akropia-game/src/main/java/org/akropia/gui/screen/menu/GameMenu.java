package org.akropia.gui.screen.menu;

import org.akropia.i18n.MessageKeyResource;
import org.akropia.model.campaign.CampaignName;
import org.akropia.model.campaign.CampaignNames;

public enum GameMenu implements MessageKeyResource {

    setupWorldStep, setupPlayerStep, setupNext, setupSize, setupLayout,
    setupSmall, setupMedium, setupLarge, setupHuge, setupOneLand,
    setupBigIslands, setupArchipelago, setupNewSeed, setupGenerating,
    setupNamePrompt, setupSummary, setupTitle, setupMap, setupMapOriginal,
    setupMapTwoIslands, setupMapRivalExpedition, setupMapReference,
    setupMapGenerated, setupMapNorthAmerica, setupGeneratedClimate,
    setupGeneratedLand, setupGeneratedSeed, setupClimateCold,
    setupClimateTemperate, setupClimateWarm, setupLandLow, setupLandMedium,
    setupLandHigh, setupGeneratedPreview, setupGeneratedSeedError,
    setupGeneratedFailed, setupNation, setupNationDutch, setupNationFrench,
    setupNationEnglish, setupNationSpanish, setupNationNote,
    setupNationReferenceNote, setupPlayerName, setupNameError, setupStart,
    mainMenu, buttonContinue, recoveryTitle, recoveryLatestPosition,
    recoveryLatestTurn, recoveryPreviousDecade, recoveryMissing,
    recoveryCorrupt, buttonLoadSave, buttonPlayCampaign, buttonFreeGame,
    buttonHallOfFame, buttonExitAkropia, buttonSetting, buttonMapEditor,
    settingTitle, buttonBack, campaignTitle, settingLanguage, settingLanguageCz,
    settingLanguageEn, settingShowGrid, settingShowFightAdvisor,
    settingAutosave, settingTurnSummary, settingOpenEuropeOnArrival,
    settingDeveloperHeading, settingDeveloperMode, settingDeveloperDescription,
    settingVersion, campaignPanelButtonBack, campaignPanel_default_findNewWorld,
    campaignPanel_default_buildArmy, campaignPanel_default_thrive;

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
