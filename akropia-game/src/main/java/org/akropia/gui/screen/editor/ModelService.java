package org.akropia.gui.screen.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.model.Location;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.WorldMap;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.generation.ScenarioDraft;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * Holds the edited scenario draft, its file, the last saved or opened state,
 * the selected square and the latest feedback. The draft is separate from any
 * running game. Edits go through the model's {@link ScenarioDraft}.
 */
@Singleton
class ModelService {

    /** Size of the ocean draft shown before anything is created or opened. */
    static final int DEFAULT_WIDTH = 40;

    static final int DEFAULT_HEIGHT = 30;

    private final VisibleAreaService visibleAreaService;

    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper("");

    private final List<Runnable> listeners = new ArrayList<>();

    private ScenarioDraft draft;

    private GameModelPo baseline;

    private File file;

    private boolean dirty;

    private Location selected;

    private Map<Location, TerrainType> terrainCache;
    private Set<Location> forestCache;
    private Set<Location> fieldCache;
    private Map<Location, RiverSize> riverCache;
    private WorldMap worldMapCache;
    private Map<Location, ResourceOverlay> resourceCache;
    private Set<Location> colonyCache;

    @Inject
    ModelService(final @Named("editor") VisibleAreaService visibleAreaService) {
        this.visibleAreaService = Preconditions
                .checkNotNull(visibleAreaService);
        replace(ScenarioDraft.ocean(DEFAULT_WIDTH, DEFAULT_HEIGHT), null);
    }

    /**
     * Show another draft: a new map or an opened file. It counts as saved.
     *
     * @param next required draft
     * @param from optional file it was opened from
     */
    void replace(final ScenarioDraft next, final File from) {
        draft = Preconditions.checkNotNull(next);
        file = from;
        baseline = draft.toScenario();
        dirty = false;
        selected = null;
        visibleAreaService.setMapSize(getMapSize());
        changed();
    }

    /**
     * The draft was written to the file and is now its saved state.
     */
    void markSaved(final File to) {
        file = Preconditions.checkNotNull(to);
        baseline = draft.toScenario();
        dirty = false;
        changed();
    }

    /**
     * Discard unsaved changes, returning to the last saved or opened state.
     */
    void revert() {
        final File from = file;
        final Location keep = selected;
        replace(ScenarioDraft.of(baseline), from);
        selected = keep != null && draft.contains(keep) ? keep : null;
    }

    /**
     * Run an edit of the draft. A rejected edit throws before anything changes
     * and the draft stays clean or dirty as it was.
     */
    void edit(final Runnable change) {
        change.run();
        dirty = true;
        changed();
    }

    void addListener(final Runnable listener) {
        listeners.add(Preconditions.checkNotNull(listener));
    }

    private void changed() {
        terrainCache = draft.getTerrainMap();
        forestCache = draft.getForests();
        fieldCache = draft.getFields();
        riverCache = draft.getRivers();
        worldMapCache = draft.getWorldMap();
        resourceCache = draft.getResources();
        colonyCache = draft.getColonyLocations();
        listeners.forEach(Runnable::run);
    }

    ScenarioDraft getDraft() {
        return draft;
    }

    Optional<File> getFile() {
        return Optional.ofNullable(file);
    }

    boolean isDirty() {
        return dirty;
    }

    Optional<Location> getSelected() {
        return Optional.ofNullable(selected);
    }

    void select(final Location location) {
        selected = location != null && draft.contains(location) ? location
                : null;
        listeners.forEach(Runnable::run);
    }

    ReadOnlyStringProperty messageProperty() {
        return message.getReadOnlyProperty();
    }

    void showMessage(final String text) {
        message.set(Preconditions.checkNotNull(text));
    }

    Location getMapSize() {
        return Location.of(draft.getWidth(), draft.getHeight());
    }

    TerrainType getTerrainTypeAt(final Location location) {
        return terrainCache.getOrDefault(location, TerrainType.OCEAN);
    }

    boolean hasTrees(final Location location) {
        return forestCache.contains(location);
    }

    boolean hasField(final Location location) {
        return fieldCache.contains(location);
    }

    RiverSize getRiverAt(final Location location) {
        return riverCache.getOrDefault(location, RiverSize.NONE);
    }

    WorldMap getWorldMap() {
        return worldMapCache;
    }

    ResourceOverlay getResourceAt(final Location location) {
        return resourceCache.getOrDefault(location, ResourceOverlay.NONE);
    }

    boolean hasColony(final Location location) {
        return colonyCache.contains(location);
    }

}
