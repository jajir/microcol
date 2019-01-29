package org.microcol.gui.gamemenu;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.campaign.CampaignName;
import org.microcol.model.campaign.CampaignNames;

public enum GameMenu implements MessageKeyResource {

    mainMenu,
    buttonContinue,
    buttonLoadSave,
    buttonPlayCampaign,
    buttonFreeGame,
    buttonExitMicroCol,
    buttonSetting,
    settingTitle,
    buttonBack,
    campaignTitle,
    settingLanguage,
    settingLanguageCz,
    settingLanguageEn,
    settingShowGrid,
    settingShowFightAdvisor,
    campaignPanelButtonBack,
    campaignPanel_default_findNewWorld,
    campaignPanel_default_buildArmy,
    campaignPanel_default_thrive;

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

    public static GameMenu get(final CampaignName campaignName, final String missionName) {
        if (CampaignNames.defaultCampaign.equals(campaignName)) {
            if ("findNewWorld".equals(missionName)) {
                return campaignPanel_default_findNewWorld;
            } else if ("buildArmy".equals(missionName)) {
                return campaignPanel_default_buildArmy;
            } else if ("thrive".equals(missionName)) {
                return campaignPanel_default_thrive;
            } else {
                throw new IllegalArgumentException(
                        String.format("Invalid mission name '%s'", missionName));
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Invalid campaign name '%s'", campaignName));
        }
    }

}
