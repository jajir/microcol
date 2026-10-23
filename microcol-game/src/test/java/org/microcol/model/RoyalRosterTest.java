package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.RoyalMemberPo;
import org.microcol.model.store.RoyalRosterPo;

import com.google.gson.Gson;

/**
 * Crown prewar roster: starting force, bell growth, ships, freeze and reload.
 */
class RoyalRosterTest {

    private static RoyalRoster roster(final Difficulty difficulty) {
        return new RoyalRoster(RoyalRoster.initial(difficulty), difficulty);
    }

    private static int ground(final RoyalRoster roster) {
        return roster.getTotal(Family.INFANTRY)
                + roster.getTotal(Family.CAVALRY)
                + roster.getTotal(Family.ARTILLERY);
    }

    private static String json(final RoyalRosterPo po) {
        return new Gson().toJson(po);
    }

    private static GameModelPo fixture(final boolean declared) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() != null) {
                player.setRoyalRoster(
                        RoyalRoster.initial(Difficulty.CONQUISTADOR));
            } else if (player.getName().equals("Dutch")) {
                RebellionFixtures.set(data.getModel(), player, declared);
            }
        }
        return data;
    }

    private static Model model(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @ParameterizedTest
    @CsvSource({ "DISCOVERER,4,2,667", "EXPLORER,5,3,556",
            "CONQUISTADOR,6,4,500", "GOVERNOR,7,5,435", "VICEROY,8,6,385" })
    void startingForceAndThresholdFollowDifficultyIndex(
            final Difficulty difficulty, final int infantry, final int cavalry,
            final int threshold) {
        final RoyalRoster crown = roster(difficulty);
        assertEquals(infantry, crown.getTotal(Family.INFANTRY));
        assertEquals(cavalry, crown.getTotal(Family.CAVALRY));
        assertEquals(2, crown.getTotal(Family.ARTILLERY));
        assertEquals(2, crown.getTotal(Family.MAN_OF_WAR));
        assertEquals(infantry + cavalry + 4, crown.getMembers().size());
        assertEquals(infantry + cavalry + 4,
                crown.getCount(Family.INFANTRY, State.RESERVE)
                        + crown.getCount(Family.CAVALRY, State.RESERVE)
                        + crown.getCount(Family.ARTILLERY, State.RESERVE)
                        + crown.getCount(Family.MAN_OF_WAR, State.RESERVE));
        assertEquals(threshold, crown.getBellThreshold());
        assertEquals(0, crown.getBellRemainder());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void bellsBelowExactAndMultipleThresholdKeepRemainder(
            final Difficulty difficulty) {
        final RoyalRoster crown = roster(difficulty);
        final int threshold = crown.getBellThreshold();
        final int start = ground(crown);
        crown.credit(threshold - 1);
        assertEquals(start, ground(crown));
        assertEquals(threshold - 1, crown.getBellRemainder());
        crown.credit(1);
        assertEquals(start + 1, ground(crown));
        assertEquals(0, crown.getBellRemainder());
        // Two complete units and one leftover bell in a single update.
        crown.credit(2 * threshold + 1);
        assertEquals(start + 3, ground(crown));
        assertEquals(1, crown.getBellRemainder());
    }

    @Test
    void royalInfantryAndCavalryReuseTheCrownChartBases() {
        assertEquals(5, Family.INFANTRY.getChartBase());
        assertEquals(6, Family.CAVALRY.getChartBase());
        assertThrows(IllegalStateException.class,
                () -> Family.ARTILLERY.getChartBase());
        assertThrows(IllegalStateException.class,
                () -> Family.MAN_OF_WAR.getChartBase());
    }

    @Test
    void groundUnitsCycleInfantryCavalryArtilleryFromTheStart() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.credit(4 * 500);
        final List<Family> grown = crown.getMembers().stream().filter(
                member -> member.getId() > 14 && member.getFamily().isGround())
                .map(RoyalRoster.Member::getFamily)
                .collect(java.util.stream.Collectors.toList());
        assertEquals(List.of(Family.INFANTRY, Family.CAVALRY, Family.ARTILLERY,
                Family.INFANTRY), grown);
    }

    @Test
    void shipBoundaryAddsOneManOfWarOnlyAboveSixTimesShips() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        // Twelve ground units and two ships sit exactly on the six-to-one
        // ratio.
        assertEquals(12, ground(crown));
        assertEquals(2, crown.getTotal(Family.MAN_OF_WAR));
        crown.credit(499);
        assertEquals(2, crown.getTotal(Family.MAN_OF_WAR));
        crown.credit(1);
        assertEquals(13, ground(crown));
        assertEquals(3, crown.getTotal(Family.MAN_OF_WAR));
        crown.credit(5 * 500);
        assertEquals(18, ground(crown));
        assertEquals(3, crown.getTotal(Family.MAN_OF_WAR));
        crown.credit(500);
        assertEquals(19, ground(crown));
        assertEquals(4, crown.getTotal(Family.MAN_OF_WAR));
    }

    @Test
    void declarationFreezesGrowthAndRemainderOnTheCrown() {
        final Model declared = model(fixture(true));
        final Player dutch = declared.getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
        final RoyalRoster crown = declared.getKingOf(dutch).orElseThrow()
                .getRoyalRoster();
        final String before = json(crown.save());
        dutch.checkCanCreditProducedBells(5000);
        dutch.creditProducedBells(5000);
        assertEquals(before, json(crown.save()));
        assertEquals(0, crown.getBellRemainder());
    }

    @Test
    void beforeDeclarationTheColonyBellsGrowTheSameCrown() {
        final Model colonial = model(fixture(false));
        final Player dutch = colonial.getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
        final RoyalRoster crown = dutch.getCrownRoster().orElseThrow();
        assertSame(crown,
                colonial.getKingOf(dutch).orElseThrow().getRoyalRoster());
        final int before = ground(crown);
        dutch.creditProducedBells(500);
        assertEquals(before + 1, ground(crown));
        assertEquals(0, crown.getBellRemainder());
    }

    @Test
    void shipAndDestroyAreOnceOnlyAndNeverResurrect() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.ship(1, 900);
        assertEquals(State.SHIPPED, crown.getMembers().get(0).getState());
        assertEquals(900,
                crown.getMembers().get(0).getUnitId().orElseThrow().intValue());
        assertThrows(IllegalStateException.class, () -> crown.ship(1, 901));
        crown.destroy(1);
        assertEquals(State.DESTROYED, crown.getMembers().get(0).getState());
        assertTrue(crown.getMembers().get(0).getUnitId().isEmpty());
        assertThrows(IllegalStateException.class, () -> crown.ship(1, 901));
        assertThrows(IllegalStateException.class, () -> crown.destroy(1));
        assertThrows(IllegalArgumentException.class, () -> crown.ship(99, 902));
        // Growth adds a new unit and leaves the destroyed one destroyed.
        crown.credit(500);
        assertEquals(State.DESTROYED, crown.getMembers().get(0).getState());
        assertEquals(7, crown.getTotal(Family.INFANTRY));
        assertEquals(1, crown.getCount(Family.INFANTRY, State.DESTROYED));
        assertEquals(6, crown.getCount(Family.INFANTRY, State.RESERVE));
    }

    @Test
    void saveAndReloadKeepIdentityStateAndRemainder() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.credit(700);
        crown.ship(7, 900);
        crown.destroy(2);
        final RoyalRosterPo saved = crown.save();
        final RoyalRoster reloaded = new RoyalRoster(saved,
                Difficulty.CONQUISTADOR);
        assertEquals(json(saved), json(reloaded.save()));
        assertEquals(200, reloaded.getBellRemainder());
        assertEquals(State.SHIPPED, reloaded.getMembers().get(6).getState());
        assertEquals(State.DESTROYED, reloaded.getMembers().get(1).getState());
        // Reloading never adds a unit at a threshold that was already passed.
        assertEquals(json(saved),
                json(new RoyalRoster(saved, Difficulty.CONQUISTADOR).save()));
    }

    @Test
    void malformedSavedRosterIsRejected() {
        final RoyalRosterPo duplicate = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        duplicate.getMembers().get(1).setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(duplicate, Difficulty.CONQUISTADOR));

        final RoyalRosterPo atThreshold = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        atThreshold.setRemainder(500);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(atThreshold, Difficulty.CONQUISTADOR));

        final RoyalRosterPo negative = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        negative.setGrowthCount(-1);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(negative, Difficulty.CONQUISTADOR));

        final RoyalRosterPo missingGround = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        missingGround.getMembers().remove(0);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(missingGround, Difficulty.CONQUISTADOR));

        final RoyalRosterPo uncovered = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        uncovered.setGrowthCount(1);
        final RoyalMemberPo extraInfantry = new RoyalMemberPo();
        extraInfantry.setId(15);
        extraInfantry.setFamily(Family.INFANTRY);
        extraInfantry.setState(State.RESERVE);
        uncovered.getMembers().add(extraInfantry);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(uncovered, Difficulty.CONQUISTADOR));

        final RoyalRosterPo unknownFamily = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        unknownFamily.getMembers().get(0).setFamily(null);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(unknownFamily, Difficulty.CONQUISTADOR));
    }

    private static RoyalMemberPo member(final int id, final Family family) {
        final RoyalMemberPo member = new RoyalMemberPo();
        member.setId(id);
        member.setFamily(family);
        member.setState(State.RESERVE);
        return member;
    }

    @Test
    void savedFamiliesMustMatchStartingForceGrowthCycleAndShips() {
        final RoyalRoster grown = roster(Difficulty.CONQUISTADOR);
        grown.credit(500);
        // Growth one adds an infantry (id 15) and the ship it requires (id 16).
        final RoyalRosterPo saved = grown.save();
        final Difficulty conquistador = Difficulty.CONQUISTADOR;
        assertDoesNotThrow(() -> new RoyalRoster(saved, conquistador));

        final RoyalRosterPo wrongFamily = grown.save();
        wrongFamily.getMembers().get(14).setFamily(Family.ARTILLERY);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(wrongFamily, conquistador));

        final RoyalRosterPo missingShip = grown.save();
        missingShip.getMembers().remove(15);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(missingShip, conquistador));

        final RoyalRosterPo extraShip = grown.save();
        extraShip.getMembers().add(member(17, Family.MAN_OF_WAR));
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(extraShip, conquistador));

        final RoyalRosterPo unshipped = RoyalRoster.initial(conquistador);
        unshipped.getMembers().remove(13);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(unshipped, conquistador));

        final RoyalRosterPo overShipped = RoyalRoster.initial(conquistador);
        overShipped.getMembers().add(member(15, Family.MAN_OF_WAR));
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(overShipped, conquistador));
    }

    @Test
    void growthWithoutAvailableIdentitiesChangesNothing() {
        final RoyalRosterPo saved = RoyalRoster
                .initial(Difficulty.CONQUISTADOR);
        // The last starting man-of-war keeps the largest identity.
        saved.getMembers().get(13).setId(Integer.MAX_VALUE - 1);
        final RoyalRoster crown = new RoyalRoster(saved,
                Difficulty.CONQUISTADOR);
        final String before = json(crown.save());
        // The infantry takes the last identity; the ship that the ratio then
        // requires has none left, so the whole update is refused.
        assertThrows(IllegalStateException.class, () -> crown.credit(500));
        assertThrows(IllegalStateException.class, () -> crown.checkCredit(500));
        assertEquals(before, json(crown.save()));
        assertEquals(0, crown.getBellRemainder());
        assertEquals(6, crown.getTotal(Family.INFANTRY));
    }

    @Test
    void crownWithoutRosterOrColonyWithRosterIsRejected() {
        final GameModelPo missing = fixture(false);
        missing.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() != null)
                .forEach(player -> player.setRoyalRoster(null));
        assertThrows(IllegalArgumentException.class, () -> model(missing));

        final GameModelPo colony = fixture(false);
        colony.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow()
                .setRoyalRoster(RoyalRoster.initial(Difficulty.CONQUISTADOR));
        assertThrows(IllegalArgumentException.class, () -> model(colony));
    }

    private static List<Family> families(
            final List<RoyalRoster.Member> members) {
        return members.stream().map(RoyalRoster.Member::getFamily)
                .collect(java.util.stream.Collectors.toList());
    }

    @Test
    void passengersCycleFamiliesAndStayWithinTheHold() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        // Infantry 1..6, cavalry 7..10, artillery 11..12, ships 13..14.
        final List<RoyalRoster.Member> first = crown.planPassengers(6, 13);
        assertEquals(List.of(1, 7, 11, 2, 8, 12),
                first.stream().map(RoyalRoster.Member::getId)
                        .collect(java.util.stream.Collectors.toList()));
        assertEquals(
                List.of(Family.INFANTRY, Family.CAVALRY, Family.ARTILLERY,
                        Family.INFANTRY, Family.CAVALRY, Family.ARTILLERY),
                families(first));
        // Planning changes nothing.
        assertEquals(12, ground(crown));
        assertEquals(State.RESERVE,
                crown.findMember(1).orElseThrow().getState());

        crown.ship(13, 900);
        int unitId = 901;
        for (final RoyalRoster.Member passenger : first) {
            crown.ship(passenger.getId(), unitId++);
        }
        // The cycle continues after the last artillery: infantry leads again.
        assertEquals(
                List.of(Family.INFANTRY, Family.CAVALRY, Family.INFANTRY,
                        Family.CAVALRY, Family.INFANTRY, Family.INFANTRY),
                families(crown.planPassengers(6, 14)));
        assertThrows(IllegalStateException.class,
                () -> crown.planPassengers(6, 13));
    }

    @Test
    void unitBindingNamesOneShippedMemberOnly() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.ship(13, 900);
        assertThrows(IllegalArgumentException.class, () -> crown.ship(14, 900));
        assertThrows(IllegalArgumentException.class, () -> crown.ship(14, 0));
        assertEquals(13, crown.findByUnitId(900).orElseThrow().getId());
        assertTrue(crown.findByUnitId(901).isEmpty());
    }

    @Test
    void boundUnitLossIsRecordedOnce() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.ship(1, 900);
        crown.destroyBoundUnit(900);
        assertEquals(State.DESTROYED,
                crown.findMember(1).orElseThrow().getState());
        final String before = json(crown.save());
        crown.destroyBoundUnit(900);
        crown.destroyBoundUnit(901);
        assertEquals(before, json(crown.save()));
        assertEquals(1, crown.getCount(Family.INFANTRY, State.DESTROYED));
    }

    @Test
    void capturedMemberIsNeitherShippedNorDestroyedAndNeverReturns() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.ship(1, 900);
        crown.captureBoundUnit(900);
        final RoyalRoster.Member member = crown.findMember(1).orElseThrow();
        assertEquals(State.CAPTURED, member.getState());
        assertTrue(member.getUnitId().isEmpty());
        assertEquals(0, crown.getCount(Family.INFANTRY, State.DESTROYED));
        assertEquals(0, crown.getCount(Family.INFANTRY, State.SHIPPED));
        final String before = json(crown.save());
        crown.captureBoundUnit(900);
        crown.destroyBoundUnit(900);
        assertEquals(before, json(crown.save()));
        assertThrows(IllegalStateException.class, () -> crown.ship(1, 901));
        assertThrows(IllegalStateException.class, () -> crown.destroy(1));
        assertEquals(before, json(
                new RoyalRoster(crown.save(), Difficulty.CONQUISTADOR).save()));
    }

    @Test
    void reserveIsDeliverableOnlyWhileSomeManOfWarRemains() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        assertTrue(crown.hasDeliverableReserve());
        crown.ship(13, 900);
        crown.destroy(14);
        assertTrue(crown.hasDeliverableReserve());
        crown.destroyBoundUnit(900);
        assertTrue(crown.hasReserveGround());
        assertFalse(crown.hasDeliverableReserve());
    }

    @Test
    void transportRetriesStayBelowTheLimitAndNeedAShippedManOfWar() {
        final RoyalRoster crown = roster(Difficulty.CONQUISTADOR);
        crown.ship(13, 900);
        crown.setTransportPlan(900, Optional.of(3), 2);
        final RoyalRoster.Member ship = crown.findMember(13).orElseThrow();
        assertEquals(3, ship.getTargetColonyId().orElseThrow().intValue());
        assertEquals(2, ship.getBlockedTurns());
        assertThrows(IllegalArgumentException.class,
                () -> crown.setTransportPlan(900, Optional.of(3),
                        RoyalRoster.BLOCKED_TURNS_BEFORE_RETARGET));
        assertThrows(IllegalArgumentException.class,
                () -> crown.setTransportPlan(900, Optional.empty(), 1));
        assertThrows(IllegalArgumentException.class,
                () -> crown.setTransportPlan(901, Optional.of(3), 0));
        crown.setTransportPlan(900, Optional.empty(), 0);
        assertTrue(ship.getTargetColonyId().isEmpty());
        crown.ship(1, 901);
        assertThrows(IllegalStateException.class,
                () -> crown.setTransportPlan(901, Optional.of(3), 0));
    }

    @Test
    void malformedBindingsAndTransportStateAreRejected() {
        final Difficulty conquistador = Difficulty.CONQUISTADOR;
        final RoyalRosterPo shippedWithoutUnit = RoyalRoster
                .initial(conquistador);
        shippedWithoutUnit.getMembers().get(0).setState(State.SHIPPED);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(shippedWithoutUnit, conquistador));

        final RoyalRosterPo reserveWithUnit = RoyalRoster.initial(conquistador);
        reserveWithUnit.getMembers().get(0).setUnitId(900);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(reserveWithUnit, conquistador));

        final RoyalRosterPo unitTwice = RoyalRoster.initial(conquistador);
        for (int i = 0; i < 2; i++) {
            unitTwice.getMembers().get(i).setState(State.SHIPPED);
            unitTwice.getMembers().get(i).setUnitId(900);
        }
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(unitTwice, conquistador));

        final RoyalRosterPo infantryTarget = RoyalRoster.initial(conquistador);
        infantryTarget.getMembers().get(0).setState(State.SHIPPED);
        infantryTarget.getMembers().get(0).setUnitId(900);
        infantryTarget.getMembers().get(0).setTargetColonyId(3);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(infantryTarget, conquistador));

        final RoyalRosterPo retried = RoyalRoster.initial(conquistador);
        retried.getMembers().get(12).setState(State.SHIPPED);
        retried.getMembers().get(12).setUnitId(900);
        retried.getMembers().get(12).setTargetColonyId(3);
        retried.getMembers().get(12)
                .setBlockedTurns(RoyalRoster.BLOCKED_TURNS_BEFORE_RETARGET);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(retried, conquistador));

        final RoyalRosterPo cycle = RoyalRoster.initial(conquistador);
        cycle.setDispatchCycle(3);
        assertThrows(IllegalArgumentException.class,
                () -> new RoyalRoster(cycle, conquistador));
    }
}
