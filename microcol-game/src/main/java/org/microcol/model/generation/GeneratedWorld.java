package org.microcol.model.generation;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.ReferenceExpedition;
import org.microcol.model.store.GeneratedWorldPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.Preconditions;

/**
 * Assembles a complete New World from player settings: generated terrain, the
 * four-nation expedition and the native nations. Every candidate is checked
 * before it is returned; a rejected candidate is replaced by the next attempt
 * of the same seed, so the outcome stays reproducible.
 */
public final class GeneratedWorld {

    /** Fixed size of the reference world. */
    public static final int WIDTH = 50;

    /** Fixed size of the reference world. */
    public static final int HEIGHT = 30;

    /** Candidates tried for one set of settings before giving up. */
    static final int MAX_ATTEMPTS = 24;

    /** Legal colony squares each European start must reach by sea. */
    static final int MIN_COLONY_SITES = 4;

    /** Separates the rumor random stream from terrain and natives. */
    private static final long RUMOR_STREAM = 0x1057C17AL;

    private GeneratedWorld() {
    }

    /**
     * @param settings   required generation settings
     * @param nation     required nation the player controls
     * @param difficulty required difficulty
     * @return complete unsaved model state of the new world
     * @throws IllegalStateException when no attempt produced a valid world
     */
    public static ModelPo create(final GeneratedWorldSettings settings,
            final EuropeanNation nation, final Difficulty difficulty) {
        Preconditions.checkNotNull(settings, "Settings are null");
        Preconditions.checkNotNull(nation, "Nation is null");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        RuntimeException last = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            final Random random = new Random(
                    settings.getSeed() * 1_000_003L + attempt);
            try {
                final WorldMapPo terrain = GeneratedWorldLayout
                        .generate(settings, random);
                final ModelPo model = ReferenceExpedition.create(terrain,
                        nation, difficulty);
                GeneratedNatives.place(model, random);
                GeneratedRumors.place(model,
                        new Random((settings.getSeed() * 1_000_003L + attempt)
                                ^ RUMOR_STREAM));
                requireLegalStarts(model);
                model.setGeneratedWorld(saved(settings));
                return model;
            } catch (IllegalArgumentException
                    | IllegalStateException rejected) {
                last = rejected;
            }
        }
        throw new IllegalStateException(
                "No valid generated world for these settings", last);
    }

    private static GeneratedWorldPo saved(
            final GeneratedWorldSettings settings) {
        final GeneratedWorldPo out = new GeneratedWorldPo();
        out.setSeed(settings.getSeed());
        out.setClimate(settings.getClimate());
        out.setLandShare(settings.getLandShare());
        return out;
    }

    /**
     * Every European ship must reach, by sailing from its high-sea start, at
     * least {@link #MIN_COLONY_SITES} land squares where its colonist may found
     * a colony. The test is the model's own {@link Model#canFoundColonyAt}, so
     * native settlements, foreign units and mountains are refused exactly as in
     * play. The candidate is loaded in full first, so an invalid world is never
     * returned.
     */
    private static void requireLegalStarts(final ModelPo candidate) {
        final Model model = Model.make(candidate,
                MissionImpl.getPredefinedGameOverEvaluators());
        final GeneratedGrid grid = new GeneratedGrid(
                candidate.getMap().getTerrainMap());
        for (final UnitPo ship : candidate.getUnits()) {
            if (!ship.getType().isShip()) {
                continue;
            }
            final Location start = ship.getPlaceMap().getLocation();
            Preconditions.checkArgument(
                    grid.terrainAt(start) == TerrainType.HIGH_SEA,
                    "Ship must start on high sea");
            final Unit founder = model
                    .getUnitById(founderOf(candidate, ship.getOwnerId()));
            final Set<Location> sites = legalSitesBySea(grid, model, founder,
                    start);
            Preconditions.checkArgument(sites.size() >= MIN_COLONY_SITES,
                    "Start at %s reaches too few legal colony sites", start);
        }
    }

    /**
     * @return identity of the first colonist of the owner, who is the
     *         prospective founder of its colony
     */
    private static int founderOf(final ModelPo candidate, final String owner) {
        return candidate.getUnits().stream()
                .filter(unit -> unit.getType() == UnitType.COLONIST
                        && owner.equals(unit.getOwnerId()))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "No founder for " + owner))
                .getId();
    }

    /**
     * @return land squares reached by sea from the start where the founder may
     *         found a colony under the model's rule
     */
    private static Set<Location> legalSitesBySea(final GeneratedGrid grid,
            final Model model, final Unit founder, final Location start) {
        final Set<Location> seen = new HashSet<>();
        final Set<Location> sites = new HashSet<>();
        final ArrayDeque<Location> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            for (final Location next : grid.neighbours(queue.remove())) {
                if (grid.isWater(next) && seen.add(next)) {
                    queue.add(next);
                } else if (grid.isLand(next)
                        && model.canFoundColonyAt(founder, next)) {
                    sites.add(next);
                }
            }
        }
        return sites;
    }

}
