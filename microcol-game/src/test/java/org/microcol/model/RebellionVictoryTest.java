package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.RoyalRoster.Member;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;

/**
 * Victory of a declared rebellion (R-0191). The scene is the declared campaign
 * of the royal tests: the Dutch rebels own New Amsterdam (5 residents), Delft
 * (1) and Haarlem (6), all members of the rebellion, and the Crown owns nothing
 * and has ten living ground units in reserve. Victory needs every surviving
 * member rebel-held and at most two living Crown ground units. The Crown's
 * units are reduced by the roster itself, and colonies change hands only by
 * real defense battles.
 */
class RebellionVictoryTest {

    private static final String KING = RoyalScenarioFixtures.KING;

    private static final String DUTCH = RoyalScenarioFixtures.REBELS;

    private static final int NEW_AMSTERDAM = 0;

    private static final int DELFT = 1;

    private static final int HAARLEM = 2;

    private static final Location DELFT_AT = Location.of(22, 12);

    /** Free land square beside Delft where its only resident steps out. */
    private static final Location DELFT_FIELD = Location.of(23, 12);

    /** The only resident of Delft. */
    private static final int DELFT_RESIDENT = 6;

    /** Identity of a royal unit that a shipped member names, not on the map. */
    private static final int CARRIED_UNIT = 9001;

    private static final String VICTORY = GameOverEvaluator.REASON_REBELLION_VICTORY;

    private static final String FRANCE = "France";

    private static final String FRENCH_KING = "France's King";

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    /** Sea east of Quebec, which makes the French member a coastal port. */
    private static final Location QUEBEC_SEA = Location.of(18, 10);

    private static Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static PlayerPo rebelPo(final ModelPo data) {
        return data.getGameManager().getPlayers().stream()
                .filter(player -> DUTCH.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private static List<Integer> ids(final int... values) {
        return Arrays.stream(values).boxed().collect(Collectors.toList());
    }

    /**
     * The declared campaign in which the Crown holds the given colonies and the
     * rebellion has exactly the given members.
     */
    private static GameModelPo scene(final int[] crownColonies,
            final int[] members) {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        RebellionFixtures.transfer(data.getModel(), KING, crownColonies);
        rebelPo(data.getModel()).setRebellionColonyIds(ids(members));
        return data;
    }

    private static PlayerKing crown(final Model model) {
        return (PlayerKing) model.getPlayerByName(KING);
    }

    private static Player rebel(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    /**
     * Destroy living Crown ground units in reserve until only the first
     * {@code keep} of them remain; the roster decides which are lost.
     */
    private static void keepLivingGround(final Model model, final int keep) {
        keepLivingGround(crown(model).getRoyalRoster(), keep);
    }

    private static void keepLivingGround(final RoyalRoster roster,
            final int keep) {
        final List<Integer> reserve = roster.getMembers().stream()
                .filter(member -> member.getFamily().isGround()
                        && member.getState() == RoyalRoster.State.RESERVE)
                .map(Member::getId).collect(Collectors.toList());
        for (int index = keep; index < reserve.size(); index++) {
            roster.destroy(reserve.get(index));
        }
    }

    /**
     * Take a colony by the real defense battle. The winning force stays on the
     * colony square as an armed garrison.
     */
    private static Unit capture(final Model model, final int colonyId,
            final String attacker) {
        final Colony colony = model.getColonyById(colonyId).orElseThrow();
        final Unit force = model.addUnitOutSideColony(colony);
        force.takeOver(model.getPlayerByName(attacker));
        ColonyBattles.capture(model, colony, force);
        return force;
    }

    private static List<GameFinishedEvent> listen(final Model model) {
        final List<GameFinishedEvent> seen = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                seen.add(event);
            }
        });
        return seen;
    }

    private static Optional<String> endReason(final Model model) {
        return model.getGameOverResult().map(GameOverResult::getGameOverReason);
    }

    // ---- ground boundary ----------------------------------------------------

    @Test
    void threeLivingGroundUnitsDoNotWinAndTwoDo() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        final List<GameFinishedEvent> events = listen(model);
        keepLivingGround(model, 3);

        model.checkRebellionLoss();
        assertFalse(model.isGameFinished(),
                "Three living ground units prevent the victory");
        assertTrue(rebel(model).getRebellionVictoryOrder().isEmpty());

        keepLivingGround(model, 2);
        model.checkRebellionLoss();
        assertEquals(Optional.of(VICTORY), endReason(model));
        assertEquals(Optional.of(1), rebel(model).getRebellionVictoryOrder());
        assertEquals(1, events.size());
    }

    @Test
    void theVictoryIsJudgedOnlyAtACompletedBoundary() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 2);
        // The roster changed without any completed action: nothing is judged.
        assertFalse(model.isGameFinished());
        assertTrue(rebel(model).getRebellionVictoryOrder().isEmpty());
        model.checkRebellionLoss();
        assertTrue(model.isGameFinished());
    }

    @Test
    void shipsAloneDoNotPreventTheVictory() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 0);
        assertEquals(2, crown(model).getRoyalRoster().getCount(
                RoyalRoster.Family.MAN_OF_WAR, RoyalRoster.State.RESERVE),
                "Both men-of-war are still in reserve");

        model.checkRebellionLoss();
        assertEquals(Optional.of(VICTORY), endReason(model));
    }

    // ---- hidden reserve, carried and landed members -------------------------

    @Test
    void aCarriedGroundUnitCountsUntilItIsDestroyed() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 3);
        final RoyalRoster roster = crown(model).getRoyalRoster();
        // Two reserve units and one carried by a man-of-war: three in all.
        final int carried = roster.getMembers().stream()
                .filter(member -> member.getFamily().isGround()
                        && member.getState() == RoyalRoster.State.RESERVE)
                .findFirst().orElseThrow().getId();
        roster.ship(carried, CARRIED_UNIT);

        model.checkRebellionLoss();
        assertFalse(model.isGameFinished(),
                "The carried unit still counts toward the three");

        roster.destroyBoundUnit(CARRIED_UNIT);
        model.checkRebellionLoss();
        assertEquals(Optional.of(VICTORY), endReason(model));
    }

    @Test
    void aCapturedCarriedUnitLeavesTheCrownsForce() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 3);
        final RoyalRoster roster = crown(model).getRoyalRoster();
        final int carried = roster.getMembers().stream()
                .filter(member -> member.getFamily().isGround()
                        && member.getState() == RoyalRoster.State.RESERVE)
                .findFirst().orElseThrow().getId();
        roster.ship(carried, CARRIED_UNIT);

        // Another owner takes the unit: captured members are not counted.
        roster.captureBoundUnit(CARRIED_UNIT);
        assertEquals(RoyalRoster.State.CAPTURED,
                roster.findMember(carried).orElseThrow().getState());
        model.checkRebellionLoss();
        assertEquals(Optional.of(VICTORY), endReason(model));
    }

    // ---- colony membership
    // ---------------------------------------------------

    @Test
    void aMemberHeldByTheCrownPreventsVictoryUntilTheRebelsRecoverIt() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 2);
        final List<GameFinishedEvent> events = listen(model);

        // The Crown takes Haarlem by a real battle; Haarlem stays a member.
        final Unit garrison = capture(model, HAARLEM, KING);
        assertFalse(model.isGameFinished(),
                "A Crown-held member prevents the victory");
        assertTrue(rebel(model).getRebellionVictoryOrder().isEmpty());

        // The rebels retake it in a real battle and the rebellion is won.
        model.destroyUnit(garrison);
        capture(model, HAARLEM, DUTCH);
        assertEquals(Optional.of(VICTORY), endReason(model));
        assertEquals(1, events.size());
    }

    @Test
    void aDestroyedMemberLeavesTheSetSoTheSurvivorsWin() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 2);
        final List<GameFinishedEvent> events = listen(model);
        assertEquals(Optional.empty(), endReason(model));

        // Delft is rebel-held; its only resident steps out and it is destroyed.
        // New Amsterdam and Haarlem remain and are rebel-held.
        model.getUnitById(DELFT_RESIDENT).placeToMap(DELFT_FIELD);

        assertTrue(model.getColonyById(DELFT).isEmpty());
        assertEquals(Optional.of(VICTORY), endReason(model));
        assertEquals(1, events.size());
    }

    // ---- loss and victory in the same action --------------------------------

    @Test
    void aLossWinsOverAVictoryJudgedAtTheSameBoundary() {
        // Delft is the only member, rebel-held and inland; the royal colonies
        // are not members. The coastal condition ends the rebellion as a loss,
        // while the victory counts would hold too.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT });
        RoyalScenarioFixtures.wallAround(data, DELFT_AT);
        final Model model = load(data.getModel());
        keepLivingGround(model, 2);

        model.checkRebellionLoss();

        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN),
                endReason(model));
        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN),
                rebel(model).getRebellionLossReason());
        assertTrue(rebel(model).getRebellionVictoryOrder().isEmpty(),
                "A lost rebellion is never also won");
    }

    // ---- computer rebels
    // -----------------------------------------------------

    @Test
    void aComputerRebelWinsWithoutEndingTheGame() {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        rebelPo(data.getModel()).setComputer(true);
        final Model model = load(data.getModel());
        keepLivingGround(model, 2);
        final List<GameFinishedEvent> events = listen(model);

        model.checkRebellionLoss();

        assertEquals(Optional.of(1), rebel(model).getRebellionVictoryOrder());
        assertFalse(model.isGameFinished(),
                "A computer victory never ends the human's game");
        assertTrue(model.getGameOverResult().isEmpty());
        assertTrue(events.isEmpty());
        assertTrue(model.isGameRunning(), "The game goes on as usual");

        // The outcome is recorded once and is not judged again.
        model.checkRebellionLoss();
        assertEquals(Optional.of(1), rebel(model).getRebellionVictoryOrder());
        assertTrue(events.isEmpty());
    }

    // ---- two declared nations in completion order ---------------------------

    private static PlayerPo playerPo(final ModelPo data, final String name) {
        return data.getGameManager().getPlayers().stream()
                .filter(player -> name.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private static PlayerKing king(final Model model, final String name) {
        return (PlayerKing) model.getPlayerByName(name);
    }

    /**
     * The bundled free-play map with two declared colonial nations, each with
     * its own King holding a fresh Conquistador force. The French member is
     * Quebec, made a port by the sea beside it; the Dutch member is Montreal.
     */
    private static ModelPo twoDeclaredNations() {
        final ModelPo model = new GameModelPoDao().loadFromClassPath(FREE_PLAY)
                .getModel();
        model.setDifficulty(Difficulty.CONQUISTADOR);
        model.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        model.getCalendar().setScoredPlay(true);
        model.getCalendar().setNumberOfPlayedTurns(0);
        final var terrain = model.getMap().getTerrainMap();
        terrain.put(QUEBEC_SEA, TerrainType.OCEAN);
        model.getMap().setTerrainType(terrain);
        final var fields = model.getMap().getFieldSet();
        fields.remove(QUEBEC_SEA);
        model.getMap().setFields(fields);
        final var trees = model.getMap().getTreeSet();
        trees.remove(QUEBEC_SEA);
        model.getMap().setTrees(trees);
        RebellionFixtures.holdFreePlayMontreal(model);
        playerPo(model, KING)
                .setRoyalRoster(RoyalRoster.initial(Difficulty.CONQUISTADOR));
        playerPo(model, FRENCH_KING)
                .setRoyalRoster(RoyalRoster.initial(Difficulty.CONQUISTADOR));
        RebellionFixtures.declare(model, playerPo(model, FRANCE));
        RebellionFixtures.declare(model, playerPo(model, DUTCH));
        // The shipped map is not started, but a rebellion is judged only in a
        // started game and saving needs a current player. France is a computer
        // nation and first in the turn order, so a started game stands on its
        // turn, as it would after the real start. The scene is judged only
        // when a test completes an action, never by loading or starting it.
        model.getGameManager().setGameStarted(true);
        model.getGameManager().setCurrentPlayer(FRANCE);
        return model;
    }

    /**
     * The started scene with its preconditions checked: each nation has its own
     * single member, its own Crown, and neither side has won with the full
     * force still in the Crowns' reserve.
     */
    private static Model twoNationsScene() {
        final Model model = load(twoDeclaredNations());
        final Player french = model.getPlayerByName(FRANCE);
        final Player dutch = rebel(model);
        assertEquals(Set.of(0), french.getRebellionColonyIds());
        assertEquals(Set.of(1), dutch.getRebellionColonyIds());
        assertSame(french, model.getColonyById(0).orElseThrow().getOwner());
        assertSame(dutch, model.getColonyById(1).orElseThrow().getOwner());
        assertEquals(Optional.of(king(model, FRENCH_KING)),
                model.getKingOf(french));
        assertEquals(Optional.of(crown(model)), model.getKingOf(dutch));
        assertTrue(french.isComputer() && dutch.isHuman());
        assertEquals(french, model.getCurrentPlayer());
        assertTrue(french.getRebellionVictoryOrder().isEmpty());
        assertTrue(dutch.getRebellionVictoryOrder().isEmpty());
        assertFalse(model.isGameFinished());
        return model;
    }

    /**
     * Complete the French rebellion first, then the Dutch one, each by the
     * roster's own losses and the judgment of a completed action.
     */
    private static Model bothRebellionsWon(final Model model) {
        keepLivingGround(king(model, FRENCH_KING).getRoyalRoster(), 2);
        keepLivingGround(crown(model).getRoyalRoster(), 3);
        model.checkRebellionLoss();
        keepLivingGround(crown(model).getRoyalRoster(), 2);
        model.checkRebellionLoss();
        return model;
    }

    @Test
    void theFirstCompletionTakesOrderOneAndTheOtherNationTakesOrderTwo() {
        final Model model = twoNationsScene();
        final Player french = model.getPlayerByName(FRANCE);
        final Player dutch = rebel(model);
        final List<GameFinishedEvent> events = listen(model);
        keepLivingGround(king(model, FRENCH_KING).getRoyalRoster(), 2);
        keepLivingGround(crown(model).getRoyalRoster(), 3);
        assertTrue(french.getRebellionVictoryOrder().isEmpty(),
                "Nothing is judged until an action completes");

        // The French win first with order one. The game goes on: a computer
        // victory never ends the human's game.
        model.checkRebellionLoss();
        assertEquals(Optional.of(1), french.getRebellionVictoryOrder());
        assertTrue(dutch.getRebellionVictoryOrder().isEmpty(),
                "Three living Crown ground units hold the Dutch");
        assertFalse(model.isGameFinished());
        assertTrue(events.isEmpty());

        // The Dutch win later and take the next free order; their game ends.
        keepLivingGround(crown(model).getRoyalRoster(), 2);
        model.checkRebellionLoss();
        assertEquals(Optional.of(2), dutch.getRebellionVictoryOrder());
        assertEquals(Optional.of(1), french.getRebellionVictoryOrder(),
                "The first order is kept");
        assertEquals(Optional.of(VICTORY), endReason(model));
        assertEquals(1, events.size());

        // The scene fixes these facts: the Dutch declared in 1490, the calendar
        // year of the saved scene, so the early points are the 290 years
        // before 1780. They own four colonists (three in the galleon, one in
        // Montreal, two points each) and 2,108 gold (two points), and no
        // father. The displayed support is the model's own, owned by
        // BaseScoreTest.
        assertEquals(Optional.of(1490), dutch.getDeclarationYear());
        final BaseScore base = dutch.getBaseScore();
        assertEquals(4, base.getColonists());
        assertEquals(8, base.getPopulationPoints());
        assertEquals(2, base.getGoldPoints());
        assertEquals(0, base.getFathers());

        // Second order: 50 percent bonus at factor 100. The total is the
        // floor of (clamped base x 150 + 290 x 100) / 100; this restates the
        // formula's algebra over the model's own support, it is not an
        // independent oracle for the base.
        final ResultScore won = dutch.getResultScore();
        assertEquals(10 + base.getSupportPoints(), won.getClampedBase());
        assertEquals(50, won.getOrderBonusPercent());
        assertEquals(290, won.getEarlyPoints());
        assertEquals(100, won.getDifficultyPercent());
        assertEquals((won.getClampedBase() * 150 + 290 * 100) / 100,
                won.getTotal());
    }

    @Test
    void savedOrdersAreKeptAndNeverAssignedAgainOnContinue() {
        final Model model = bothRebellionsWon(twoNationsScene());
        final long total = rebel(model).getResultScore().getTotal();
        final String identity = model.getFrozenResult().orElseThrow()
                .getIdentity();

        // The saved game names both orders, and the one human end.
        final ModelPo saved = model.save();
        assertEquals(Integer.valueOf(1),
                playerPo(saved, FRANCE).getRebellionVictoryOrder());
        assertEquals(Integer.valueOf(2),
                playerPo(saved, DUTCH).getRebellionVictoryOrder());
        assertTrue(saved.getGameManager().isGameFinished());
        assertEquals(VICTORY, saved.getGameManager().getGameOverReason());

        final Model restored = load(saved);
        final List<GameFinishedEvent> again = listen(restored);
        assertEquals(Optional.of(1),
                restored.getPlayerByName(FRANCE).getRebellionVictoryOrder());
        assertEquals(Optional.of(2),
                restored.getPlayerByName(DUTCH).getRebellionVictoryOrder());
        assertEquals(Optional.of(VICTORY), endReason(restored));
        assertEquals(total,
                restored.getPlayerByName(DUTCH).getResultScore().getTotal());
        // The one frozen result is the same final result, of order two.
        final GameResult frozen = restored.getFrozenResult().orElseThrow();
        assertEquals(identity, frozen.getIdentity());
        assertEquals(GameResult.Status.FINAL, frozen.getStatus());
        assertEquals(Optional.of(2), frozen.getVictoryOrder());
        assertEquals(total, frozen.getScore().getTotal());

        // A later judgment, or Continue, neither reassigns nor repeats the end.
        restored.checkRebellionLoss();
        assertThrows(IllegalStateException.class, restored::startGame);
        assertTrue(again.isEmpty(), "The victory is not announced again");
        assertEquals(Optional.of(1),
                restored.getPlayerByName(FRANCE).getRebellionVictoryOrder());
        assertEquals(Optional.of(2),
                restored.getPlayerByName(DUTCH).getRebellionVictoryOrder());
    }

    // ---- undeclared nations and terminal persistence -----------------------

    @Test
    void anUndeclaredNationNeverWinsAMissingRebellion() {
        final Model model = load(RoyalScenarioFixtures
                .crown(Difficulty.CONQUISTADOR, false).getModel());
        keepLivingGround(model, 0);

        model.checkRebellionLoss();

        assertFalse(model.isGameFinished());
        assertTrue(rebel(model).getRebellionVictoryOrder().isEmpty());
    }

    @Test
    void aWonRebellionIsAnnouncedOnceAndReloadsFinishedWithoutAnotherEvent() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        keepLivingGround(model, 2);
        final List<GameFinishedEvent> events = listen(model);
        model.checkRebellionLoss();
        assertEquals(1, events.size());

        // A later judgment, or the same action again, never changes the result.
        model.checkRebellionLoss();
        assertEquals(1, events.size());

        final ModelPo saved = model.save();
        assertTrue(saved.getGameManager().isGameFinished());
        assertEquals(VICTORY, saved.getGameManager().getGameOverReason());
        assertEquals(Integer.valueOf(1),
                rebelPo(saved).getRebellionVictoryOrder());

        final Model restored = load(saved);
        final List<GameFinishedEvent> again = listen(restored);
        assertTrue(restored.isGameFinished());
        assertEquals(Optional.of(VICTORY), endReason(restored));
        assertEquals(Optional.of(1),
                restored.getPlayerByName(DUTCH).getRebellionVictoryOrder());
        // The first victory takes the full order bonus and the early points of
        // the saved declaration year; reading the result changes nothing and
        // the reloaded result is the same (R-0196).
        final String beforeRead = new com.google.gson.Gson()
                .toJson(rebel(model).save());
        final ResultScore won = rebel(model).getResultScore();
        assertEquals(100, won.getOrderBonusPercent());
        assertEquals(1780 - rebel(model).getDeclarationYear().orElseThrow(),
                won.getEarlyPoints());
        assertEquals(won.getTotal(),
                restored.getPlayerByName(DUTCH).getResultScore().getTotal());
        assertEquals(beforeRead,
                new com.google.gson.Gson().toJson(rebel(model).save()));
        assertThrows(IllegalStateException.class, restored::startGame);
        restored.checkRebellionLoss();
        assertTrue(again.isEmpty(), "The victory is not repeated");
    }

    @Test
    void loadRejectsAnIncoherentVictoryState() {
        // A won human rebellion without the finished game that it ended.
        final ModelPo open = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        rebelPo(open).setRebellionVictoryOrder(1);
        assertThrows(RuntimeException.class, () -> load(open));

        // A victory order that is not a completion order.
        final ModelPo zero = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        rebelPo(zero).setRebellionVictoryOrder(0);
        assertThrows(RuntimeException.class, () -> load(zero));

        // A nation that never declared cannot have won.
        final ModelPo undeclared = RoyalScenarioFixtures
                .crown(Difficulty.CONQUISTADOR, false).getModel();
        rebelPo(undeclared).setRebellionVictoryOrder(1);
        assertThrows(RuntimeException.class, () -> load(undeclared));
    }

    @Test
    void loadRejectsVictoryOrdersThatAreNotTheCompletionOrder() {
        // The first victory of the game is order one; a saved two alone is not
        // a completion order and would make the next count collide.
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        rebelPo(data.getModel()).setComputer(true);
        rebelPo(data.getModel()).setRebellionVictoryOrder(2);
        assertThrows(RuntimeException.class, () -> load(data.getModel()));
    }

    @Test
    void loadRejectsAFinishedRebellionEndThatNoHumanRebelRecorded() {
        // A finished victory with no human victory on record.
        final ModelPo victory = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        victory.getGameManager().setGameFinished(true);
        victory.getGameManager().setGameOverReason(VICTORY);
        assertThrows(RuntimeException.class, () -> load(victory));

        // A finished loss with no human loss on record.
        final ModelPo loss = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        loss.getGameManager().setGameFinished(true);
        loss.getGameManager().setGameOverReason(
                GameOverEvaluator.REASON_REBELLION_NO_COLONIES);
        assertThrows(RuntimeException.class, () -> load(loss));
    }

    @Test
    void aWonRebellionStartsNoNewHelp() {
        // Control: a declared computer rebel that has not won starts its help.
        final GameModelPo open = RoyalScenarioFixtures.declaredCrown();
        rebelPo(open.getModel()).setComputer(true);
        final Model control = load(open.getModel());
        rebel(control).startTurn();
        assertTrue(rebel(control).getIntervention().isPresent());

        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        rebelPo(data.getModel()).setComputer(true);
        final Model model = load(data.getModel());
        keepLivingGround(model, 2);
        model.checkRebellionLoss();
        assertEquals(Optional.of(1), rebel(model).getRebellionVictoryOrder());

        // Its next owner update delivers and starts nothing new.
        rebel(model).startTurn();
        assertTrue(rebel(model).getIntervention().isEmpty(),
                "A won rebellion starts no help");
    }
}
