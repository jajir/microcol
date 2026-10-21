package org.microcol.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

/**
 * Chooses the skill a native settlement offers to teach when its map is created
 * (R-0157). Skills whose goods the settlement's surroundings produce naturally
 * are preferred; when none matches, the whole accepted list is used. Only the
 * generator's random stream is drawn from, so a seed reproduces the choice, and
 * it is never redrawn when the map is viewed or loaded.
 */
public final class NativeTeachableSkills {

    private NativeTeachableSkills() {
    }

    /**
     * @param surroundings required squares around the settlement, its own
     *                     square included
     * @param random       required generator of the map being created
     * @return one accepted skill, uniformly among the preferred candidates
     */
    public static UnitType choose(final List<Terrain> surroundings,
            final Random random) {
        Preconditions.checkNotNull(surroundings);
        Preconditions.checkNotNull(random);
        final List<UnitType> productive = UnitType.NATIVE_TEACHABLE_SKILLS
                .stream().filter(skill -> produces(surroundings, skill))
                .collect(Collectors.toList());
        final List<UnitType> candidates = productive.isEmpty()
                ? UnitType.NATIVE_TEACHABLE_SKILLS
                : productive;
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * @return whether a square of the skill's source yields its goods
     *         naturally; a skill without a produced good never matches
     */
    private static boolean produces(final List<Terrain> surroundings,
            final UnitType skill) {
        final GoodsType goodsType = skill.getExpertInProducing();
        return goodsType != null && surroundings.stream()
                .anyMatch(terrain -> isSource(skill, terrain.getTerrainType())
                        && terrain.getNaturalOutput(goodsType) > 0);
    }

    /**
     * Farmers take food from land and fishermen from water, so both can share
     * corn without matching each other's source. Other skills take any square.
     */
    private static boolean isSource(final UnitType skill,
            final TerrainType terrainType) {
        if (skill == UnitType.EXPERT_FISHERMAN) {
            return terrainType.isSea();
        }
        if (skill == UnitType.EXPERT_FARMER) {
            return terrainType.isLand();
        }
        return true;
    }

}
