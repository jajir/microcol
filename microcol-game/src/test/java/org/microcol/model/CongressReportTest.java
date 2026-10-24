package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Congress report (R-0203): national support, founders and the Crown roster of
 * the viewer only. Reading changes nothing and a saved game reports the same.
 */
class CongressReportTest {
    private static final List<Founder> OFFER = List.of(Founder.JOHN_PAUL_JONES,
            Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN);

    private static CongressPo active(final int bells, final Founder selected) {
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setBells(bells);
        po.setCandidates(OFFER);
        po.setSelected(selected);
        po.setGeneration(1);
        return po;
    }

    /**
     * The declared Dutch with a Conquistador Crown; the other colonial nations
     * keep no Congress. Only the Dutch Congress is given.
     */
    private static GameModelPo campaign(final CongressPo dutchCongress) {
        final GameModelPo data = RoyalScenarioFixtures
                .crown(Difficulty.CONQUISTADOR, true);
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() == null) {
                player.setCongress(
                        player.getName().equals("Dutch") ? dutchCongress
                                : Congress.initial());
            }
        }
        return data;
    }

    /**
     * The campaign with each Dutch colony's liberty set from its live
     * residents. Liberty is read from a separate preview, so no value is
     * guessed.
     */
    private static GameModelPo withLiberty(
            final ToLongFunction<Colony> liberty) {
        final Model preview = make(campaign(Congress.initial()));
        final Map<String, Long> points = new HashMap<>();
        for (final Colony colony : preview.getColonies(dutch(preview))) {
            points.put(colony.getName(), liberty.applyAsLong(colony));
        }
        final GameModelPo data = campaign(Congress.initial());
        for (final ColonyPo colony : data.getModel().getColonies()) {
            if (points.containsKey(colony.getName())) {
                colony.setLiberty(points.get(colony.getName()));
            }
        }
        return data;
    }

    /**
     * The bundled free-play map, where the Dutch have no colony yet and the
     * French hold two; both nations have their own King.
     */
    private static GameModelPo freePlay() {
        return new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
    }

    private static Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return named(model, "Dutch");
    }

    private static Player named(final Model model, final String name) {
        return model.getPlayers().stream()
                .filter(player -> name.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private static int totalMembers(final RoyalRoster crown) {
        return java.util.Arrays.stream(Family.values())
                .mapToInt(crown::getTotal).sum();
    }

    private static JsonElement json(final Model model) {
        return new Gson().toJsonTree(model.save());
    }

    @Test
    void zeroLibertyReadsZeroPercentWithTheNationsResidents() {
        final Model model = make(withLiberty(colony -> 0L));
        final Player owner = dutch(model);
        final CongressReport report = model.getCongressReport(owner);
        assertEquals(0, report.getNationalSupport().getDisplayPercent());
        assertFalse(report.getNationalSupport().isAtLeastPercent(50));
        assertTrue(report.getNationalSupport().getPopulation() > 0);
        assertEquals(owner.getNationalRebelSupport(),
                report.getNationalSupport());
    }

    @Test
    void justBelowHalfShowsFortyNineAndExactHalfMeetsTheThreshold() {
        // One point less than 100 per resident keeps the nation below half.
        final Model below = make(
                withLiberty(colony -> colony.getPopulation() == 0 ? 0L
                        : 100L * colony.getPopulation() - 1L));
        final CongressReport belowReport = below
                .getCongressReport(dutch(below));
        assertEquals(49, belowReport.getNationalSupport().getDisplayPercent());
        assertFalse(belowReport.getNationalSupport().isAtLeastPercent(50));
        assertEquals(dutch(below).getNationalRebelSupport(),
                belowReport.getNationalSupport());
        // Exactly 100 per resident is half of 200 per resident, shown as 50.
        final Model half = make(
                withLiberty(colony -> 100L * colony.getPopulation()));
        final CongressReport halfReport = half.getCongressReport(dutch(half));
        assertEquals(50, halfReport.getNationalSupport().getDisplayPercent());
        assertTrue(halfReport.getNationalSupport().isAtLeastPercent(50));
    }

    @Test
    void selectedFounderShowsBellsTowardTheTargetAndWaitsUnjoined() {
        final Model model = make(campaign(active(99, Founder.JOHN_PAUL_JONES)));
        final Player owner = dutch(model);
        final CongressReport report = model.getCongressReport(owner);
        assertEquals(99, report.getBells());
        // Conquistador factor 100 with no member: the first objective costs
        // 100.
        assertEquals(100, report.getTarget());
        assertEquals(owner.getCongressTarget(), report.getTarget());
        assertEquals(Optional.of(Founder.JOHN_PAUL_JONES),
                report.getSelectedFounder());
        assertEquals(List.of(), report.getMembers());
        assertFalse(report.isJonesDeliveryPending());
    }

    @Test
    void reportKeepsItsOwnBellsWhileALaterReportSeesNewBells() {
        final Model model = make(campaign(active(10, Founder.JOHN_PAUL_JONES)));
        final Player owner = dutch(model);
        final CongressReport before = model.getCongressReport(owner);
        // Five bells stay far below the target of 100, so no founder joins.
        owner.creditProducedBells(5);
        final CongressReport after = model.getCongressReport(owner);
        assertEquals(10, before.getBells());
        assertEquals(15, after.getBells());
        assertEquals(before.getTarget(), after.getTarget());
        assertEquals(List.of(), after.getMembers());
    }

    @Test
    void crownCountsSeparateReserveShippedAndDestroyedWithoutDoubleCounting() {
        final Model model = make(campaign(Congress.initial()));
        final Player owner = dutch(model);
        final RoyalRoster crown = owner.getCrownRoster().orElseThrow();
        // A Conquistador has difficulty index 2: 4+2 infantry, 2+2 cavalry, 2
        // artillery and 2 men-of-war, all in reserve (RoyalRosterTest).
        assertEquals(6, crown.getTotal(Family.INFANTRY));
        assertEquals(6, crown.getCount(Family.INFANTRY, State.RESERVE));
        assertEquals(4, crown.getTotal(Family.CAVALRY));
        final List<Integer> infantry = crown.getMembers().stream()
                .filter(member -> member.getFamily() == Family.INFANTRY)
                .map(RoyalRoster.Member::getId).collect(Collectors.toList());
        crown.ship(infantry.get(0), 9001);
        crown.destroy(infantry.get(1));
        final RoyalRoster seen = model.getCongressReport(owner).getCrownRoster()
                .orElseThrow();
        // One reserve member is shipped and one is lost: reserve falls by two,
        // shipped and destroyed rise by one, and the total does not change.
        assertEquals(6, seen.getTotal(Family.INFANTRY));
        assertEquals(4, seen.getCount(Family.INFANTRY, State.RESERVE));
        assertEquals(1, seen.getCount(Family.INFANTRY, State.SHIPPED));
        assertEquals(1, seen.getCount(Family.INFANTRY, State.DESTROYED));
        assertEquals(0, seen.getCount(Family.INFANTRY, State.CAPTURED));
        assertEquals(4, seen.getCount(Family.CAVALRY, State.RESERVE));
        // Growth at the Conquistador threshold of 500 adds one infantry, the
        // first unit of the growth cycle, and keeps the 7 bells beyond it. Its
        // 13 ground members exceed six per ship, so one man-of-war joins.
        assertEquals(500, crown.getBellThreshold());
        crown.credit(500 + 7);
        final RoyalRoster grown = model.getCongressReport(owner)
                .getCrownRoster().orElseThrow();
        assertEquals(7, grown.getTotal(Family.INFANTRY));
        assertEquals(5, grown.getCount(Family.INFANTRY, State.RESERVE));
        assertEquals(1, grown.getCount(Family.INFANTRY, State.SHIPPED));
        assertEquals(1, grown.getCount(Family.INFANTRY, State.DESTROYED));
        assertEquals(4, grown.getCount(Family.CAVALRY, State.RESERVE));
        assertEquals(3, grown.getTotal(Family.MAN_OF_WAR));
        assertEquals(7, grown.getBellRemainder());
        assertEquals(grown.getTotal(Family.INFANTRY),
                grown.getCount(Family.INFANTRY, State.RESERVE)
                        + grown.getCount(Family.INFANTRY, State.SHIPPED)
                        + grown.getCount(Family.INFANTRY, State.DESTROYED)
                        + grown.getCount(Family.INFANTRY, State.CAPTURED));
    }

    @Test
    void firstColonyNotYetFoundedReadsZeroSupportWithTheCrownOfTheNation() {
        final Model model = make(freePlay());
        final Player owner = dutch(model);
        assertTrue(model.getColonies(owner).isEmpty());
        final CongressReport report = model.getCongressReport(owner);
        assertEquals(RebelSupport.NONE, report.getNationalSupport());
        assertEquals(0, report.getNationalSupport().getDisplayPercent());
        assertEquals(0, report.getNationalSupport().getPopulation());
        assertEquals(owner.getCongressMembers(), report.getMembers());
        assertEquals(owner.getSelectedFounder(), report.getSelectedFounder());
        assertSame(owner.getCrownRoster().orElseThrow(),
                report.getCrownRoster().orElseThrow());
    }

    @Test
    void twoColonialViewersNeverSeeEachOthersSupportOrCrown() {
        final Model model = make(freePlay());
        final Player dutch = dutch(model);
        final Player france = named(model, "France");
        assertFalse(model.getColonies(france).isEmpty());
        final CongressReport dutchReport = model.getCongressReport(dutch);
        final CongressReport franceReport = model.getCongressReport(france);
        assertEquals(RebelSupport.NONE, dutchReport.getNationalSupport());
        assertTrue(franceReport.getNationalSupport().getPopulation() > 0);
        assertEquals(france.getNationalRebelSupport(),
                franceReport.getNationalSupport());
        final RoyalRoster dutchCrown = dutch.getCrownRoster().orElseThrow();
        final RoyalRoster franceCrown = france.getCrownRoster().orElseThrow();
        assertNotSame(dutchCrown, franceCrown);
        assertSame(dutchCrown, dutchReport.getCrownRoster().orElseThrow());
        assertSame(franceCrown, franceReport.getCrownRoster().orElseThrow());
        // Growing the French Crown changes its own report and no Dutch figure.
        final int dutchBefore = totalMembers(dutchCrown);
        final int franceBefore = totalMembers(franceCrown);
        // A bell threshold adds at least one ground member, possibly with a
        // ship.
        franceCrown.credit(franceCrown.getBellThreshold());
        assertTrue(totalMembers(franceCrown) > franceBefore);
        assertEquals(totalMembers(franceCrown), totalMembers(model
                .getCongressReport(france).getCrownRoster().orElseThrow()));
        assertEquals(dutchBefore, totalMembers(dutchCrown));
        assertEquals(dutchBefore, totalMembers(
                model.getCongressReport(dutch).getCrownRoster().orElseThrow()));
    }

    @Test
    void reportNeverShowsAnotherViewersRosterOrAcceptsForeignPlayers() {
        final Model model = make(campaign(Congress.initial()));
        final Player owner = dutch(model);
        final Player king = model.getPlayers().stream().filter(Player::isKing)
                .findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> model.getCongressReport(king));
        assertThrows(NullPointerException.class,
                () -> model.getCongressReport(null));
        assertSame(owner.getCrownRoster().orElseThrow(),
                model.getCongressReport(owner).getCrownRoster().orElseThrow());
        // A same-named Dutch player of another game is not this game's viewer.
        final Model other = make(campaign(Congress.initial()));
        assertThrows(IllegalArgumentException.class,
                () -> model.getCongressReport(dutch(other)));
    }

    @Test
    void repeatedReadChangesNothingAndReloadKeepsTheSameFigures(
            @TempDir final Path folder) {
        final Model model = make(campaign(active(99, Founder.JOHN_PAUL_JONES)));
        final Player owner = dutch(model);
        final JsonElement before = json(model);
        final CongressReport first = model.getCongressReport(owner);
        final CongressReport second = model.getCongressReport(owner);
        assertEquals(before, json(model));
        assertEquals(first.getNationalSupport(), second.getNationalSupport());
        assertEquals(first.getBells(), second.getBells());
        assertEquals(first.getTarget(), second.getTarget());
        assertEquals(first.getSelectedFounder(), second.getSelectedFounder());
        // Save the live state to disk and load it again: the same figures.
        final GameModelPo saved = campaign(Congress.initial());
        saved.setModel(model.save());
        final String file = folder.resolve("congress-report.microcol")
                .toString();
        new GameModelPoDao().saveToFile(file, saved);
        final Model reloaded = make(
                new GameModelPoDao().loadFromFile(new File(file)));
        final CongressReport again = reloaded
                .getCongressReport(dutch(reloaded));
        assertEquals(first.getNationalSupport(), again.getNationalSupport());
        assertEquals(first.getBells(), again.getBells());
        assertEquals(first.getTarget(), again.getTarget());
        assertEquals(first.getSelectedFounder(), again.getSelectedFounder());
        assertEquals(first.getMembers(), again.getMembers());
        assertEquals(first.getCrownRoster().orElseThrow().getBellThreshold(),
                again.getCrownRoster().orElseThrow().getBellThreshold());
        assertEquals(
                first.getCrownRoster().orElseThrow().getTotal(Family.INFANTRY),
                again.getCrownRoster().orElseThrow().getTotal(Family.INFANTRY));
    }
}
