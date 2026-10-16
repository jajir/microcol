package org.microcol.gui.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import org.microcol.gui.Loc;
import org.microcol.gui.screen.menu.GameMenu;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.MicroColException;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.RecoverySlot;

import com.google.common.base.Preconditions;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/** Read-only chooser for the three recovery points. */
public final class RecoveryDialog extends AbstractMessageWindow {

    private final I18n i18n;
    private final PersistingTool persistingTool;
    private final GameModelPoDao saveReader;
    private Optional<RecoverySlot> result = Optional.empty();

    @Inject
    public RecoveryDialog(final ViewUtil viewUtil, final I18n i18n,
            final PersistingTool persistingTool,
            final GameModelPoDao saveReader) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.persistingTool = persistingTool;
        this.saveReader = saveReader;
    }

    public Optional<RecoverySlot> choose() {
        result = Optional.empty();
        final ToggleGroup group = new ToggleGroup();
        final VBox rows = new VBox(12);
        rows.setPadding(new Insets(12));
        rows.getChildren().add(new Label(i18n.get(GameMenu.recoveryTitle)));
        for (final RecoverySlot slot : RecoverySlot.values()) {
            final File file = persistingTool.getRecoveryFile(slot);
            final RadioButton choice = new RadioButton(
                    slotName(slot) + " — " + describe(file));
            choice.setId("recovery" + slot.name());
            choice.setToggleGroup(group);
            choice.setUserData(slot);
            choice.setDisable(!file.isFile());
            rows.getChildren().add(choice);
        }
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(GameMenu.buttonLoadSave), i18n.get(Dialog.cancel));
        buttons.getButtonYes().setId("recoveryLoad");
        buttons.getButtonNo().setId("recoveryCancel");
        buttons.getButtonYes().disableProperty()
                .bind(group.selectedToggleProperty().isNull());
        buttons.getButtonYes().setOnAction(event -> {
            result = Optional
                    .of((RecoverySlot) group.getSelectedToggle().getUserData());
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        rows.getChildren().add(buttons.getContent());
        init(rows);
        setTitle(i18n.get(GameMenu.recoveryTitle));
        showAndWait();
        return result;
    }

    private String slotName(final RecoverySlot slot) {
        switch (slot) {
        case LATEST_POSITION:
            return i18n.get(GameMenu.recoveryLatestPosition);
        case LATEST_TURN:
            return i18n.get(GameMenu.recoveryLatestTurn);
        case PREVIOUS_DECADE:
            return i18n.get(GameMenu.recoveryPreviousDecade);
        default:
            throw new IllegalArgumentException("Unknown recovery slot");
        }
    }

    private String describe(final File file) {
        if (!file.isFile()) {
            return i18n.get(GameMenu.recoveryMissing);
        }
        final String modified;
        try {
            modified = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(i18n.getCurrentLocale())
                    .format(Files.getLastModifiedTime(file.toPath()).toInstant()
                            .atZone(ZoneId.systemDefault()));
        } catch (IOException unreadable) {
            return i18n.get(GameMenu.recoveryCorrupt);
        }
        try {
            final GameModelPo saved = saveReader.loadFromFile(file);
            Preconditions.checkArgument(saved.getModel() != null
                    && saved.getModel().getCalendar() != null);
            final Calendar calendar = Calendar
                    .make(saved.getModel().getCalendar());
            final String season = calendar.getCurrentSeason().map(
                    value -> i18n.get(Loc.getTerrainDescription(value)) + " ")
                    .orElse("");
            return season + calendar.getCurrentYear() + " — " + modified;
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException | JsonParseException invalid) {
            return i18n.get(GameMenu.recoveryCorrupt) + " — " + modified;
        }
    }

    @Override
    protected void onCancelDialog() {
        result = Optional.empty();
        super.onCancelDialog();
    }
}
