package org.akropia.gui.screen.menu.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.Gson;
import org.akropia.model.*;
import org.akropia.model.unit.UnitActionType;
import org.akropia.model.campaign.po.GameModelPoDao;

/** Authored commands; all gameplay changes still pass through the model. */
public final class DemoScene {
    private transient java.nio.file.Path directory;
    private transient org.akropia.model.campaign.po.GameModelPo initialData;
    public String initialState;
    public Map<String, Integer> actors;
    public int[] camera;
    public List<Step> steps;

    public static final class Step {
        public String action;
        public String actor;
        public int[] to;
        public int[] from;
        public String name;
        public int durationMs;
    }

    public static DemoScene load() {
        String directory = System.getProperty("akropia.demo.directory");
        if (directory != null)
            return load(java.nio.file.Path.of(directory));
        return loadBundled();
    }

    public static DemoScene load(java.nio.file.Path directory) {
        try (Reader reader = java.nio.file.Files
                .newBufferedReader(directory.resolve("scene.json"))) {
            DemoScene scene = new Gson().fromJson(reader, DemoScene.class);
            scene.directory = directory.toAbsolutePath().normalize();
            scene.validate();
            return scene;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read demo directory",
                    error);
        }
    }

    private static DemoScene loadBundled() {
        try (InputStream stream = DemoScene.class
                .getResourceAsStream("/demos/first-colony/scene.json")) {
            DemoScene scene = new Gson().fromJson(
                    new InputStreamReader(Objects.requireNonNull(stream),
                            StandardCharsets.UTF_8),
                    DemoScene.class);
            scene.validate();
            return scene;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read demo", e);
        }
    }

    public void validate() {
        if ((initialState == null || initialState.isBlank()
                || initialState.contains("/") || initialState.contains("\\")
                || !(initialState.endsWith(".json")
                        || initialState.endsWith(".akropia")))
                || camera == null || camera.length != 2 || actors == null
                || steps == null || steps.isEmpty() || steps.size() > 500)
            throw new IllegalArgumentException("Invalid demo definition");
        for (Step step : steps) {
            if (step == null || step.action == null
                    || !Set.of("wait", "move", "disembark", "found", "animate",
                            "plow", "endTurn").contains(step.action)
                    || step.durationMs < 0 || step.durationMs > 60000)
                throw new IllegalArgumentException("Invalid demo step");
            if (Set.of("move", "disembark", "found", "animate", "plow")
                    .contains(step.action) && !actors.containsKey(step.actor))
                throw new IllegalArgumentException("Unknown actor");
            if (Set.of("move", "disembark", "animate").contains(step.action)
                    && (step.to == null || step.to.length != 2))
                throw new IllegalArgumentException("Missing destination");
        }
    }

    public Model newModel() {
        GameModelPoDao dao = new GameModelPoDao();
        if (initialData == null)
            initialData = directory == null
                    ? dao.loadFromClassPath(
                            "/demos/first-colony/" + initialState)
                    : initialState.endsWith(".akropia")
                            ? dao.loadPlayerGame(
                                    directory.resolve(initialState).toFile())
                            : dao.loadScenarioFromFile(
                                    directory.resolve(initialState).toFile());
        Model model = Model.make(initialData.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        for (Integer id : actors.values()) {
            if (id == null)
                throw new IllegalArgumentException("Missing actor identity");
            model.getUnitById(id);
        }
        if (!model.getMap().isValid(Location.of(camera[0], camera[1])))
            throw new IllegalArgumentException("Camera outside map");
        for (Step step : steps) {
            if ("animate".equals(step.action) && (step.from == null
                    || step.from.length != 2 || !model.getMap()
                            .isValid(Location.of(step.from[0], step.from[1]))))
                throw new IllegalArgumentException("Invalid animation origin");
            if (step.to != null && !model.getMap().isValid(destination(step)))
                throw new IllegalArgumentException("Destination outside map");
        }
        model.startGame();
        return model;
    }

    public Unit actor(Model model, Step step) {
        return model.getUnitById(actors.get(step.actor));
    }

    /** Presentation starts at the carrier while a passenger is still aboard. */
    public Location movementOrigin(Model model, Step step) {
        Unit unit = actor(model, step);
        if ("animate".equals(step.action)) {
            if (!unit.getLocation().equals(destination(step)))
                throw new IllegalArgumentException(
                        "Animation destination differs from unit position");
            return Location.of(step.from[0], step.from[1]);
        }
        return "disembark".equals(step.action)
                ? unit.getPlaceCargoSlot().getOwnerUnit().getLocation()
                : unit.getLocation();
    }

    public Location destination(Step step) {
        return Location.of(step.to[0], step.to[1]);
    }

    public void execute(Model model, Step step) {
        switch (step.action) {
        case "wait":
            break;
        case "animate":
            movementOrigin(model, step);
            break;
        case "move":
            model.moveUnit(actor(model, step),
                    org.akropia.model.Path.of(List.of(destination(step))));
            break;
        case "disembark":
            actor(model, step).disembarkToLocation(destination(step));
            break;
        case "found":
            model.buildColony(actor(model, step).getOwner(), actor(model, step),
                    step.name);
            break;
        case "plow":
            actor(model, step).setActionType(UnitActionType.plowField);
            break;
        case "endTurn":
            model.endTurn();
            break;
        default:
            throw new IllegalArgumentException("Unknown action");
        }
    }
}
