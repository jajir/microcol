package org.microcol.gui.gamemenu;

import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Language;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

@Singleton
public class SettingLanguageView implements JavaFxComponent, UpdatableLanguage {

    public static final String RB_CZECH_ID = "rbCzech";

    public static final String RB_ENGLISH_ID = "rbEnglish";
    
    private final HBox mainPanel;

    private final ToggleGroup toggleGroup;

    private final Label label;

    private final RadioButton rbCzech;

    private final RadioButton rbEnglish;

    @Inject
    public SettingLanguageView(final GamePreferences gamePreferences, final I18n i18n) {
        label = new Label();
        toggleGroup = new ToggleGroup();

        rbCzech = new RadioButton();
        rbCzech.setId(RB_CZECH_ID);
        rbCzech.setToggleGroup(toggleGroup);
        rbCzech.setSelected(gamePreferences.getLanguage().equals(Language.cz));

        rbEnglish = new RadioButton();
        rbEnglish.setId(RB_ENGLISH_ID);
        rbEnglish.setToggleGroup(toggleGroup);
        rbEnglish.setSelected(gamePreferences.getLanguage().equals(Language.en));

        mainPanel = new HBox();
        mainPanel.getChildren().add(label);
        mainPanel.getChildren().add(rbCzech);
        mainPanel.getChildren().add(rbEnglish);

        updateLanguage(i18n);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(I18n i18n) {
        label.setText(i18n.get(GameMenu.settingLanguage));
        rbCzech.setText(i18n.get(GameMenu.settingLanguageCz));
        rbEnglish.setText(i18n.get(GameMenu.settingLanguageEn));
    }

    /**
     * @return the toggleGroup
     */
    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }

    /**
     * @return the rbCzech
     */
    public RadioButton getRbCzech() {
        return rbCzech;
    }

    /**
     * @return the rbEnglish
     */
    public RadioButton getRbEnglish() {
        return rbEnglish;
    }

}
