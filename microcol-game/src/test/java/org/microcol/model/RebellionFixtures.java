package org.microcol.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;

/**
 * Authors a declared rebellion in a saved test game (R-0192). A declaration
 * fixes the colonies the nation owns at that moment, so the saved membership is
 * exactly those colonies; loading never rebuilds it from ownership.
 */
public final class RebellionFixtures {

    private RebellionFixtures() {
    }

    /**
     * Keep the free-play Dutch rebellion viable without giving it a repair
     * yard. Quebec remains French for trade and battery tests; Montreal and its
     * worker become Dutch, with an unused adjacent square made coastal.
     */
    static void holdFreePlayMontreal(final ModelPo model) {
        final var montreal = model.getColonies().stream()
                .filter(colony -> colony.getId() == 1).findFirst()
                .orElseThrow();
        montreal.setOwnerName("Dutch");
        montreal.setLastColonialAffiliation("Dutch");
        model.getUnits().stream().filter(unit -> unit.getId() == 5).findFirst()
                .orElseThrow().setOwnerId("Dutch");
        final Location coast = Location.of(17, 22);
        final var terrain = model.getMap().getTerrainMap();
        terrain.put(coast, TerrainType.OCEAN);
        model.getMap().setTerrainType(terrain);
        final var trees = model.getMap().getTreeSet();
        trees.remove(coast);
        model.getMap().setTrees(trees);
        final var fields = model.getMap().getFieldSet();
        fields.remove(coast);
        model.getMap().setFields(fields);
    }

    /**
     * Declare the nation of a saved player and author its members.
     *
     * @param model  required saved game that contains the colonies
     * @param player required saved player of the declaring nation
     */
    public static void declare(final ModelPo model, final PlayerPo player) {
        player.setDeclaredIndependence(true);
        // A declaration is dated by the saved calendar, as the game does
        // (R-0196).
        player.setDeclarationYear(
                Calendar.make(model.getCalendar()).getCurrentYear());
        player.setRebellionColonyIds(ownedColonyIds(model, player.getName()));
    }

    /**
     * Declare or withdraw the nation of a saved player, as a test toggles it.
     *
     * @param model    required saved game that contains the colonies
     * @param player   required saved player
     * @param declared whether the nation declared independence
     */
    public static void set(final ModelPo model, final PlayerPo player,
            final boolean declared) {
        if (declared) {
            declare(model, player);
        } else {
            undeclare(player);
        }
    }

    /**
     * Withdraw a saved declaration; a nation that never declared has no
     * members.
     *
     * @param player required saved player
     */
    public static void undeclare(final PlayerPo player) {
        player.setDeclaredIndependence(false);
        player.setDeclarationYear(null);
        player.setRebellionColonyIds(null);
    }

    /**
     * Give saved colonies and all their residents to another owner, such as a
     * Crown that took them in an earlier war or that has always held them. The
     * rebellion membership is not touched: a captured colony stays a member and
     * an always-royal one never was.
     *
     * @param model     required saved game
     * @param newOwner  required name of the player that holds the colonies
     * @param colonyIds identities of the saved colonies to give
     */
    public static void transfer(final ModelPo model, final String newOwner,
            final int... colonyIds) {
        final Set<Integer> ids = new HashSet<>();
        for (final int id : colonyIds) {
            ids.add(id);
        }
        for (final ColonyPo colony : model.getColonies()) {
            if (!ids.contains(colony.getId())) {
                continue;
            }
            colony.setOwnerName(newOwner);
            colony.getColonyFields().forEach(
                    field -> reown(model, field.getWorkerId(), newOwner));
            colony.getConstructions()
                    .forEach(construction -> construction.getSlots()
                            .forEach(slot -> reown(model, slot.getWorkerId(),
                                    newOwner)));
        }
    }

    private static void reown(final ModelPo model, final Integer unitId,
            final String owner) {
        if (unitId == null) {
            return;
        }
        model.getUnits().stream().filter(unit -> unitId.equals(unit.getId()))
                .forEach(unit -> unit.setOwnerId(owner));
    }

    /**
     * @return identities of the colonies the nation owns in the saved game
     */
    public static List<Integer> ownedColonyIds(final ModelPo model,
            final String owner) {
        return model.getColonies().stream()
                .filter(colony -> owner.equals(colony.getOwnerName()))
                .map(colony -> colony.getId()).collect(Collectors.toList());
    }
}
