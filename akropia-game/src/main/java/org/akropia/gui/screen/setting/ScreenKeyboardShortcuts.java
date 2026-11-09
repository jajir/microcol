package org.akropia.gui.screen.setting;

import java.util.ArrayList;
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
import javafx.event.EventHandler;
import javafx.stage.Window;
import org.akropia.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
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
    private int captureIndex;
    private KeyCode swallowRelease;
    private String message = "";
    private String focusAfterRefresh;

    @Inject
    ScreenKeyboardShortcuts(final I18n i18n,
            final ShortcutController controller) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.controller = Preconditions.checkNotNull(controller);
        title.setId("shortcutsTitle");
        description.setWrapText(true);
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
        information.setPadding(new Insets(16, 24, 8, 24));
        root.setTop(new VBox(header, information));
        root.setCenter(scroll);
        root.setId("keyboardShortcutsScreen");
        root.getStyleClass().add("in-game-page");
        root.getStylesheets().add(
                getClass().getResource("/gui/InGamePage.css").toExternalForm());
    }

    public void open(final Runnable returnToSettings) {
        this.returnToSettings = returnToSettings;
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
        final List<KeyBinding> list = new ArrayList<>(
                draft.getBindings(capturing));
        final int existing = list.indexOf(binding.get());
        if (existing >= 0 && existing != captureIndex) {
            message = i18n.get(ShortcutText.captureDuplicate,
                    i18n.get(capturing.getLabel()),
                    controller.displayText(binding.get()));
            focusAfterRefresh = captureControlId();
        } else {
            if (captureIndex < 0) {
                list.add(binding.get());
                captureIndex = list.size() - 1;
            } else {
                list.set(captureIndex, binding.get());
            }
            draft = draft.with(capturing, list);
            message = "";
            focusAfterRefresh = id(capturing) + "-binding-" + captureIndex;
        }
        capturing = null;
        refresh();
    }

    private String captureControlId() {
        return captureIndex < 0 ? id(capturing) + "-add"
                : id(capturing) + "-binding-" + captureIndex;
    }

    private void startCapture(final ShortcutAction action, final int index) {
        capturing = action;
        captureIndex = index;
        message = "";
        focusAfterRefresh = captureControlId();
        refresh();
    }

    private void refresh() {
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
            heading.setPadding(new Insets(8, 0, 0, 0));
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
        label.setMinWidth(200);
        label.setPrefWidth(200);
        final FlowPane bindings = new FlowPane(6, 4);
        bindings.setId(id + "-bindings");
        HBox.setHgrow(bindings, Priority.ALWAYS);
        final List<KeyBinding> list = draft.getBindings(action);
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            final String text = controller.displayText(list.get(i));
            final Button binding = ActionButtons.button(ButtonIcon.REPLACE,
                    capturing == action && captureIndex == i ? "…" : text);
            binding.setId(id + "-binding-" + i);
            binding.setUserData(text);
            binding.setAccessibleText(i18n.get(ShortcutText.change, text));
            binding.setOnAction(event -> startCapture(action, index));
            final Button remove = ActionButtons.button(ButtonIcon.REMOVE);
            remove.setId(id + "-remove-" + i);
            remove.setAccessibleText(i18n.get(ShortcutText.remove, text));
            remove.setOnAction(event -> {
                final List<KeyBinding> changed = new ArrayList<>(
                        draft.getBindings(action));
                changed.remove(index);
                draft = draft.with(action, changed);
                message = "";
                focusAfterRefresh = id + "-add";
                capturing = null;
                refresh();
            });
            bindings.getChildren().add(new HBox(2, binding, remove));
        }
        if (list.isEmpty()) {
            final Label unbound = new Label(i18n.get(ShortcutText.unbound));
            unbound.setId(id + "-unbound");
            bindings.getChildren().add(unbound);
        }
        final Button add = ActionButtons.button(ButtonIcon.ADD,
                capturing == action && captureIndex < 0 ? "…"
                        : i18n.get(ShortcutText.add));
        add.setId(id + "-add");
        add.setAccessibleText(i18n.get(ShortcutText.add) + " " + name);
        add.setOnAction(event -> startCapture(action, -1));
        bindings.getChildren().add(add);
        final HBox row = new HBox(10, label, bindings);
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
