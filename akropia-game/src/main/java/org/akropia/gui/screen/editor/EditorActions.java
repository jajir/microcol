package org.akropia.gui.screen.editor;

import java.io.File;
import java.util.List;

import org.akropia.gui.FileSelectingService;
import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.dialog.SaveConfirmation;
import org.akropia.gui.event.model.GameController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.NativeNation;
import org.akropia.model.UnitType;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.generation.ScenarioDraftException;
import org.akropia.model.generation.ScenarioProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Player actions of the developer map editor (R-0435): New, Open, Save, Play
 * and leaving, each protected by Save / Discard / Cancel when the draft has
 * unsaved changes, and the edits of tools and the tile panel. A canceled or
 * failed step keeps the current draft and explains the outcome.
 */
@Singleton
public class EditorActions {

    private static final String EXTENSION = ".akropia";

    private final Logger logger = LoggerFactory.getLogger(EditorActions.class);

    private final ModelService modelService;

    private final EditorMessages messages;

    private final ViewUtil viewUtil;

    private final I18n i18n;

    private final FileSelectingService chooser;

    private final GameModelPoDao dao;

    private final GameController gameController;

    private final PersistingService persistingService;

    private final EventBus eventBus;

    @Inject
    EditorActions(final ModelService modelService,
            final EditorMessages messages, final ViewUtil viewUtil,
            final I18n i18n, final FileSelectingService chooser,
            final GameModelPoDao dao, final GameController gameController,
            final PersistingService persistingService,
            final EventBus eventBus) {
        this.modelService = Preconditions.checkNotNull(modelService);
        this.messages = Preconditions.checkNotNull(messages);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.chooser = Preconditions.checkNotNull(chooser);
        this.dao = Preconditions.checkNotNull(dao);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.eventBus = Preconditions.checkNotNull(eventBus);
    }

    /**
     * Ask for dimensions and replace the draft with an ocean map.
     */
    void newMap() {
        if (!confirmLeave()) {
            return;
        }
        new NewMapDialog(viewUtil, i18n, messages, modelService.getMapSize())
                .choose().ifPresent(size -> {
                    modelService.replace(
                            ScenarioDraft.ocean(size.getX(), size.getY()),
                            null);
                    modelService.showMessage(messages.get(EditorText.created,
                            size.getX(), size.getY()));
                });
    }

    /**
     * Choose a scenario file and open it for editing.
     */
    void open() {
        if (!confirmLeave()) {
            return;
        }
        final File file = chooser.openScenario(directory(),
                i18n.get(EditorText.openTitle));
        if (file == null) {
            modelService.showMessage(messages.get(EditorText.openCanceled));
            return;
        }
        read(file);
    }

    /**
     * Open a scenario chosen elsewhere, such as the developer load route.
     */
    void openFile(final File file) {
        Preconditions.checkNotNull(file);
        if (confirmLeave()) {
            read(file);
        }
    }

    private void read(final File file) {
        final ScenarioDraft opened;
        final boolean archive;
        try {
            archive = dao.isPlayerSaveArchive(file);
            opened = ScenarioDraft.of(archive ? dao.loadPlayerGame(file)
                    : dao.loadScenarioFromFile(file));
        } catch (ScenarioDraftException rejected) {
            modelService.showMessage(messages.get(EditorText.openFailed,
                    file.getName(), messages.describe(rejected)));
            return;
        } catch (RuntimeException unreadable) {
            logger.info("Unable to open scenario {}", file, unreadable);
            modelService.showMessage(messages.get(EditorText.openFailed,
                    file.getName(), i18n.get(EditorText.reason_STRUCTURE)));
            return;
        }
        // Archive imports are independent scenarios, not replacement player
        // saves.
        modelService.replace(opened, archive ? null : file);
        modelService
                .showMessage(messages.get(EditorText.opened, file.getName()));
    }

    /**
     * Choose a file and write the draft to it. A failed write keeps the
     * previous file and the draft stays unsaved.
     *
     * @return whether the draft is now saved
     */
    boolean save() {
        final String name = modelService.getFile().map(File::getName)
                .orElse("scenario" + EXTENSION);
        final File chosen = chooser.saveScenario(directory(), name,
                i18n.get(EditorText.saveTitle));
        if (chosen == null) {
            modelService.showMessage(messages.get(EditorText.saveCanceled));
            return false;
        }
        final File file = chosen.getName().endsWith(EXTENSION) ? chosen
                : new File(chosen.getParentFile(),
                        chosen.getName() + EXTENSION);
        try {
            dao.saveScenarioToFile(file.getAbsolutePath(),
                    modelService.getDraft().toScenario());
        } catch (RuntimeException failed) {
            logger.warn("Unable to save scenario {}", file, failed);
            modelService.showMessage(
                    messages.get(EditorText.saveFailed, file.getName()));
            return false;
        }
        modelService.markSaved(file);
        modelService
                .showMessage(messages.get(EditorText.saved, file.getName()));
        return true;
    }

    /**
     * Start a new game from the draft when it is playable; otherwise stay and
     * list every problem, selecting the first implicated square.
     */
    void play() {
        final List<ScenarioProblem> problems = modelService.getDraft()
                .getPlayabilityProblems();
        if (!problems.isEmpty()) {
            showPlayProblems(problems);
            return;
        }
        // The game starts from the draft as authored, even when its changes
        // are then discarded from the editor.
        final GameModelPo scenario = modelService.getDraft().toScenario();
        if (!confirmLeave() || !persistingService.confirmLeave()) {
            return;
        }
        try {
            gameController.startAuthoredScenario(scenario);
        } catch (ScenarioDraftException rejected) {
            showPlayProblems(rejected.getProblems());
            return;
        } catch (RuntimeException failed) {
            logger.warn("Unable to start the authored scenario", failed);
            modelService.showMessage(messages.get(EditorText.playFailed,
                    String.valueOf(failed.getMessage())));
            return;
        }
        discardUnsaved();
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

    private void showPlayProblems(final List<ScenarioProblem> problems) {
        problems.stream().map(ScenarioProblem::getLocation)
                .flatMap(java.util.Optional::stream).findFirst()
                .ifPresent(modelService::select);
        modelService.showMessage(messages.get(EditorText.playRejected,
                problems.stream().map(messages::describe)
                        .collect(java.util.stream.Collectors.joining(" "))));
    }

    /**
     * Return to the main menu.
     */
    void leave() {
        if (confirmLeave()) {
            discardUnsaved();
            eventBus.post(new ShowScreenEvent(Screen.MENU));
        }
    }

    /**
     * A left editor keeps no unsaved changes; the next visit shows the saved or
     * opened state.
     */
    private void discardUnsaved() {
        if (modelService.isDirty()) {
            modelService.revert();
        }
    }

    /**
     * The leave rule of New, Open, Play, leaving and closing the application: a
     * saved draft is left at once, otherwise Save / Discard / Cancel. A
     * discarded draft stays until the action replaces or leaves it.
     *
     * @return whether the pending action may continue
     */
    public boolean confirmLeave() {
        if (!modelService.isDirty()) {
            return true;
        }
        final int choice = new SaveConfirmation(viewUtil, i18n)
                .choose(i18n.get(EditorText.unsaved), true);
        return choice == 2 || choice == 1 && save();
    }

    private File directory() {
        return modelService.getFile().map(File::getAbsoluteFile)
                .map(File::getParentFile)
                .orElseGet(() -> new File(System.getProperty("user.home")));
    }

    /**
     * Run an edit at a square; a refusal is explained and changes nothing.
     */
    void change(final Location location, final Runnable edit) {
        if (location != null) {
            modelService.select(location);
        }
        try {
            modelService.edit(edit);
        } catch (ScenarioDraftException rejected) {
            modelService.showMessage(messages.get(EditorText.rejected,
                    messages.describe(rejected)));
            return;
        }
        modelService.showMessage(location == null ? ""
                : messages.get(EditorText.changed, location.getX(),
                        location.getY()));
    }

    void setNation(final EuropeanNation nation) {
        if (nation != modelService.getDraft().getNation()) {
            change(null, () -> modelService.getDraft().setNation(nation));
            // A refused change shows the kept nation again.
            modelService.select(modelService.getSelected().orElse(null));
        }
    }

    void updateSettlement(final Location location, final NativeNation nation,
            final boolean capital, final UnitType skill) {
        change(location, () -> modelService.getDraft()
                .updateSettlement(location, nation, capital, skill));
    }

    void removeUnit(final Location location, final int unitId) {
        change(location, () -> modelService.getDraft().removeUnit(unitId));
    }

    void addPassenger(final Location location, final int shipId,
            final UnitType type) {
        change(location,
                () -> modelService.getDraft().addPassenger(shipId, type));
    }

}
