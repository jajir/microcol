package org.akropia.gui.screen.setting;

import java.util.List;
import java.util.Optional;

import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.shortcut.KeyBinding;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.shortcut.ShortcutConfiguration;
import org.akropia.gui.shortcut.ShortcutContext;
import org.akropia.gui.shortcut.ShortcutController;
import org.akropia.gui.shortcut.ShortcutText;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.GameScreenHeader;
import org.akropia.gui.util.ActionButtons;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import org.akropia.gui.screen.game.components.StatusBar;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent.Source;
import javafx.event.EventHandler;
import javafx.stage.Window;
import org.akropia.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.common.eventbus.EventBus;
import org.akropia.gui.screen.game.components.StatusBarMessageEvent;
import com.google.inject.Singleton;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Full-page editor of draft keyboard shortcuts. Capturing a combination
 * consumes every key until it ends, so no old command runs; Escape cancels the
 * capture. Save and back activates the draft only after it was saved; Cancel
 * and Escape discard it.
 */
@Singleton
public final class ScreenKeyboardShortcuts implements JavaFxComponent {

    public static final String STATUS_ID = "shortcutStatus";
    public static final String RESET_ID = "shortcutsReset";
    public static final String CANCEL_ID = "shortcutsCancel";
    public static final String SAVE_ID = "shortcutsSave";
    public static final String PLATFORM_ID = "shortcutsPlatform";

    private static final ShortcutText[] CONTEXT_LABELS = {
            ShortcutText.contextGlobal, ShortcutText.contextSession,
            ShortcutText.contextMainMenu, ShortcutText.contextMap,
            ShortcutText.contextEurope, ShortcutText.contextColony,
            ShortcutText.contextEditor };

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ScreenKeyboardShortcuts.class);

    private final StatusBar statusBar;
    private final EventBus eventBus;

    private final BorderPane root = new BorderPane();
    private final EventHandler<KeyEvent> keyFilter = this::onKey;
    private Window window;
    private Runnable returnToSettings;
    private final I18n i18n;
    private final ShortcutController controller;
    private final Label title = new Label();
    private final Label description = new Label();
    private final Label platform = new Label();
    private final VBox rows = new VBox(4);
    private final Label status = new Label();
    private final Button reset = ActionButtons.button(ButtonIcon.REPLACE);
    private final Button cancel = ActionButtons.button(ButtonIcon.CANCEL);
    private final Button save = ActionButtons.button(ButtonIcon.SAVE);

    private ShortcutConfiguration draft;
    private ShortcutAction capturing;
    private final Timeline captureBlink = new Timeline();
    private KeyCode swallowRelease;
    private String message = "";
    private String focusAfterRefresh;

    @Inject
    ScreenKeyboardShortcuts(final I18n i18n,
            final ShortcutController controller, final StatusBar statusBar,
            final EventBus eventBus) {
        this.statusBar = statusBar;
        this.eventBus = Preconditions.checkNotNull(eventBus);
        statusBar.setShowEventsFromSource(Source.GAME);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.controller = Preconditions.checkNotNull(controller);
        title.setId("shortcutsTitle");
        description.setId("shortcutsDescription");
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);
        description.setMaxWidth(Double.MAX_VALUE);
        platform.setId(PLATFORM_ID);
        status.setId(STATUS_ID);
        status.setWrapText(true);
        status.setMaxWidth(Double.MAX_VALUE);
        status.setMinHeight(Region.USE_PREF_SIZE);
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setId("shortcutsRows");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(0);
        scroll.getStyleClass().add("screen-scroll");
        rows.setPadding(new Insets(24));
        reset.setId(RESET_ID);
        cancel.setId(CANCEL_ID);
        save.setId(SAVE_ID);
        reset.setOnAction(event -> {
            draft = controller.getDefaults();
            capturing = null;
            message = "";
            refresh();
        });
        cancel.setOnAction(event -> close());
        save.setOnAction(event -> onSave());
        final HBox commitButtons = new HBox(10, cancel, save);
        final HBox buttons = new HBox(28, reset, commitButtons);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        save.setStyle("-fx-font-weight: bold;");
        final GameScreenHeader header = new GameScreenHeader();
        header.setTitle(title);
        header.setActions(buttons);
        final VBox information = new VBox(10, description, platform, status);
        information.setMinHeight(Region.USE_PREF_SIZE);
        information.setPadding(new Insets(16, 24, 8, 24));
        scroll.setContent(new VBox(information, rows));
        root.setTop(header);
        root.setBottom(statusBar.getContent());
        root.setCenter(scroll);
        root.setId("keyboardShortcutsScreen");
        root.getStyleClass().add("in-game-page");
        root.getStylesheets().add(
                getClass().getResource("/gui/InGamePage.css").toExternalForm());
    }

    public void open(final Runnable returnToSettings) {
        this.returnToSettings = returnToSettings;
        statusBar.updateLanguage(i18n);
        draft = controller.getActive();
        capturing = null;
        swallowRelease = null;
        message = "";
        focusAfterRefresh = null;
        refresh();
        window = root.getScene().getWindow();
        window.addEventFilter(KeyEvent.ANY, keyFilter);
        root.requestFocus();
    }

    private void close() {
        captureBlink.stop();
        window.removeEventFilter(KeyEvent.ANY, keyFilter);
        capturing = null;
        returnToSettings.run();
    }

    @Override
    public Region getContent() {
        return root;
    }

    private void onSave() {
        if (!draft.findConflicts().isEmpty()) {
            refresh();
            return;
        }
        try {
            controller.save(draft);
            close();
        } catch (RuntimeException failed) {
            // The previous configuration stays active and the draft stays
            // here.
            LOGGER.warn("Keyboard shortcuts were not saved", failed);
            message = i18n.get(ShortcutText.saveFailed);
            refresh();
        }
    }

    private void onKey(final KeyEvent event) {
        if (swallowRelease != null
                && event.getEventType() == KeyEvent.KEY_RELEASED
                && event.getCode() == swallowRelease) {
            // The key that ended a capture never reaches a shortcut.
            swallowRelease = null;
            event.consume();
            return;
        }
        if (capturing == null) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED
                    && event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                close();
            }
            return;
        }
        event.consume();
        if (event.getEventType() != KeyEvent.KEY_PRESSED) {
            return;
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            swallowRelease = KeyCode.ESCAPE;
            focusAfterRefresh = captureControlId();
            capturing = null;
            message = i18n.get(ShortcutText.captureCanceled);
            refresh();
            return;
        }
        final Optional<KeyBinding> binding = KeyBinding.fromEvent(event);
        if (binding.isEmpty()) {
            // A modifier alone waits for the rest of the combination.
            return;
        }
        swallowRelease = event.getCode();
        draft = draft.with(capturing, List.of(binding.get()));
        message = "";
        focusAfterRefresh = captureControlId();
        capturing = null;
        refresh();
    }

    private String captureControlId() {
        return id(capturing) + "-binding-0";
    }

    private void startCapture(final ShortcutAction action) {
        capturing = action;
        message = "";
        focusAfterRefresh = captureControlId();
        refresh();
    }

    private void refresh() {
        captureBlink.stop();
        captureBlink.getKeyFrames().clear();
        if (draft == null) {
            return;
        }
        title.setText(i18n.get(ShortcutText.title));
        description.setText(i18n.get(ShortcutText.description));
        platform.setText(i18n.get(ShortcutText.platform,
                i18n.get(controller.getPlatform().getLabel())));
        reset.setText(i18n.get(ShortcutText.reset));
        cancel.setText(i18n.get(ShortcutText.cancel));
        save.setText(i18n.get(ShortcutText.save));
        rows.getChildren().clear();
        for (final ShortcutContext context : ShortcutContext.values()) {
            final Label heading = new Label(
                    i18n.get(CONTEXT_LABELS[context.ordinal()]));
            heading.getStyleClass().add("shortcut-group");
            heading.setId("shortcutGroup-" + context.name());
            rows.getChildren().add(heading);
            for (final ShortcutAction action : ShortcutAction.values()) {
                if (action.getContext() == context) {
                    rows.getChildren().add(row(action));
                }
            }
        }
        final List<ShortcutConfiguration.Conflict> conflicts = draft
                .findConflicts();
        if (capturing != null) {
            status.setText(i18n.get(ShortcutText.capture,
                    i18n.get(capturing.getLabel())));
        } else if (!conflicts.isEmpty()) {
            final ShortcutConfiguration.Conflict conflict = conflicts.get(0);
            status.setText(i18n.get(ShortcutText.conflict,
                    controller.displayText(conflict.getBinding()),
                    i18n.get(conflict.getFirst().getLabel()),
                    i18n.get(conflict.getSecond().getLabel())));
        } else {
            status.setText(
                    message.isEmpty() ? i18n.get(ShortcutText.conflictFree)
                            : message);
        }
        save.setDisable(!conflicts.isEmpty() || capturing != null);
        if (focusAfterRefresh != null) {
            final Node focus = rows.lookup("#" + focusAfterRefresh);
            focusAfterRefresh = null;
            if (focus != null) {
                focus.requestFocus();
            }
        }
    }

    private Node row(final ShortcutAction action) {
        final String id = id(action);
        final String name = i18n.get(action.getLabel());
        final Label label = new Label(name);
        label.setId(id + "-label");
        label.setWrapText(true);
        label.setMinWidth(260);
        label.setPrefWidth(260);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setTextAlignment(TextAlignment.RIGHT);
        final List<KeyBinding> list = draft.getBindings(action);
        final String text = list.isEmpty() ? i18n.get(ShortcutText.unbound)
                : controller.displayText(list.get(0));
        final HBox keys = new HBox(8);
        keys.setAlignment(Pos.CENTER_LEFT);
        keys.setMouseTransparent(true);
        final List<String> parts = capturing == action
                ? List.of(i18n.get(ShortcutText.captureKeys))
                : list.isEmpty() ? List.of(text)
                        : controller.displayKeys(list.get(0));
        for (final String part : parts) {
            if (!keys.getChildren().isEmpty()) {
                keys.getChildren().add(new Label("+"));
            }
            final Label key = new Label(part);
            key.getStyleClass().add("shortcut-keycap");
            if (list.isEmpty() && capturing != action) {
                key.setId(id + "-unbound");
            }
            keys.getChildren().add(key);
        }
        final Button binding = ActionButtons.shortcut(keys);
        binding.getStyleClass().add("shortcut-key");
        binding.setId(id + "-binding-0");
        binding.setUserData(list.isEmpty() ? null : text);
        binding.setAccessibleText(
                name + ": " + i18n.get(ShortcutText.change, text));
        binding.setOnAction(event -> startCapture(action));
        if (capturing == action) {
            binding.getStyleClass().add("shortcut-capturing");
            final PseudoClass pulse = PseudoClass.getPseudoClass("capture-on");
            binding.pseudoClassStateChanged(pulse, true);
            captureBlink
                    .getKeyFrames().setAll(
                            new KeyFrame(Duration.millis(600),
                                    event -> binding.pseudoClassStateChanged(
                                            pulse, false)),
                            new KeyFrame(Duration.millis(1200), event -> binding
                                    .pseudoClassStateChanged(pulse, true)));
            captureBlink.setCycleCount(Timeline.INDEFINITE);
            captureBlink.play();
        }
        final Button remove = ActionButtons.clearShortcut();
        remove.setId(id + "-remove-0");
        remove.setAccessibleText(
                name + ": " + i18n.get(ShortcutText.remove, text));
        remove.setDisable(list.isEmpty());
        remove.setOnAction(event -> {
            draft = draft.with(action, List.of());
            message = "";
            focusAfterRefresh = id + "-binding-0";
            capturing = null;
            refresh();
        });
        final HBox bindings = new HBox(12, binding, remove);
        if (draft.findConflicts().stream()
                .anyMatch(conflict -> conflict.getFirst() == action
                        || conflict.getSecond() == action)) {
            final Label warning = new Label();
            warning.setGraphic(ButtonIcon.WARNING.createView());
            warning.setId(id + "-conflict");
            warning.setAccessibleText(i18n.get(ShortcutText.conflictHint));
            warning.setOnMouseEntered(
                    event -> eventBus.post(new StatusBarMessageEvent(
                            i18n.get(ShortcutText.conflictHint), Source.GAME)));
            warning.setOnMouseExited(event -> eventBus
                    .post(new StatusBarMessageEvent(Source.GAME)));
            bindings.getChildren().add(warning);
        }
        bindings.setId(id + "-bindings");
        bindings.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(bindings, Priority.ALWAYS);
        final HBox row = new HBox(24, label, bindings);
        row.getStyleClass().add("shortcut-row");
        row.setId(id);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * @param action required action
     * @return node id of the action's row; its controls add suffixes
     */
    public static String id(final ShortcutAction action) {
        return "shortcut-" + action.getKey().replace('.', '-');
    }

}
