package org.microcol.gui.screen.colony;

import org.microcol.gui.buttonpanel.AbstractButtonsPanel;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;

@Singleton
public class ColonyButtonsPanel extends AbstractButtonsPanel {

    public static final String CLOSE_BUTTON_ID = "closeButtonId";

    private final Button buttonClose;
    private final Button exports = new Button();
    private final Button education = new Button();
    private final I18n i18n;
    private org.microcol.model.Colony colony;

    @Inject
    ColonyButtonsPanel(final ImageProvider imageProvider,
            final EventBus eventBus, final I18n i18n,
            final DialogCustomHouse customHouse, final DialogEducation school) {
        super(imageProvider, eventBus, Source.COLONY, i18n);

        this.i18n = i18n;
        getButtonPanel().setId("colonyButtons");
        education.setId("educationOpen");
        education.setOnAction(event -> school.showFor(colony));
        education.setDisable(true);
        exports.setId("customHouseOpen");
        exports.setOnAction(event -> customHouse.showFor(colony));
        exports.setDisable(true);
        buttonClose = makeButon(ImageLoaderButtons.BUTTON_EXIT,
                ColonyMsg.buttonClose);
        buttonClose.setId(CLOSE_BUTTON_ID);
        buttonClose.setOnAction(
                evnt -> eventBus.post(new ShowScreenEvent(Screen.GAME)));

        getButtonPanel().getChildren().addAll(buttonClose, exports, education);
    }

    void setColony(org.microcol.model.Colony colony) {
        this.colony = colony;
        education.setText(i18n.get(EducationText.open));
        education.setDisable(false);
        exports.setText(i18n.get(CustomHouseText.open));
        exports.setDisable(!colony.isContainsConstructionByType(
                org.microcol.model.ConstructionType.CUSTOM_HOUSE));
        exports.setTooltip(new javafx.scene.control.Tooltip(
                i18n.get(exports.isDisabled() ? CustomHouseText.unavailable
                        : CustomHouseText.rule)));
    }

    /**
     * @return the buttonExit
     */
    public Button getButtonClose() {
        return buttonClose;
    }

}
