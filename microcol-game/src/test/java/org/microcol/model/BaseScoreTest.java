package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * R-0195 base score: the weights of living people, distinct fathers, the
 * treasury floor and the displayed national support. Pure weight cases use
 * {@link BaseScore#of}; nation cases read the live model of the free-play map,
 * where France owns Montreal.
 */
class BaseScoreTest {

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String FRANCE = "France";

    // ---- Pure weights, fathers, treasury and support ----

    @ParameterizedTest
    @CsvSource({ "1,0,0,1", "0,1,0,2", "0,0,1,4", "1,2,1,9" })
    void eachCivilianIdentityWeighsOneTwoOrFourPoints(final int servants,
            final int colonists, final int experts, final long expected) {
        final BaseScore score = BaseScore.of(servants, colonists, experts, 0, 0,
                0);
        assertEquals(expected, score.getPopulationPoints());
        assertEquals(expected, score.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({ "0,0", "1,5", "25,125" })
    void eachDistinctFatherAddsFivePoints(final int fathers,
            final long expected) {
        final BaseScore score = BaseScore.of(0, 0, 0, fathers, 0, 0);
        assertEquals(fathers, score.getFathers());
        assertEquals(expected, score.getFatherPoints());
        assertEquals(expected, score.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({ "0,0", "999,0", "1000,1", "1999,1", "2000,2" })
    void treasuryIsTheFloorOfGoldPerThousand(final long gold,
            final long expected) {
        final BaseScore score = BaseScore.of(0, 0, 0, 0, gold, 0);
        assertEquals(gold, score.getGold());
        assertEquals(expected, score.getGoldPoints());
        assertEquals(expected, score.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({ "0,0", "50,50", "100,100" })
    void supportAddsItsWholePercentage(final int percent, final long expected) {
        final BaseScore score = BaseScore.of(0, 0, 0, 0, 0, percent);
        assertEquals(percent, score.getSupportPercent());
        assertEquals(expected, score.getSupportPoints());
        assertEquals(expected, score.getSubtotal());
    }

    @Test
    void subtotalIsTheExactSumOfTheNamedComponents() {
        final BaseScore score = BaseScore.of(1, 2, 1, 2, 1999, 50);
        assertEquals(9, score.getPopulationPoints());
        assertEquals(10, score.getFatherPoints());
        assertEquals(1, score.getGoldPoints());
        assertEquals(50, score.getSupportPoints());
        assertEquals(70, score.getSubtotal());
    }

    @Test
    void largeCountsStayExactInLongArithmetic() {
        final BaseScore score = BaseScore.of(Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0, 0);
        assertEquals(7L * Integer.MAX_VALUE, score.getSubtotal());
    }

    @Test
    void invalidComponentsAreRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> BaseScore.of(-1, 0, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> BaseScore.of(0, 0, 0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> BaseScore.of(0, 0, 0, 0, 0, 101));
        assertThrows(IllegalArgumentException.class,
                () -> BaseScore.of(0, 0, 0, 0, 0, -1));
    }

    // ---- Live nation of the free-play map ----

    private static GameModelPo data() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath(FREE_PLAY);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(FRANCE);
        return data;
    }

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player france(final Model model) {
        return model.getPlayerByName(FRANCE);
    }

    private static Colony montreal(final Model model) {
        return model.getColonyById(1).orElseThrow();
    }

    private static PlayerPo playerPo(final GameModelPo data,
            final String name) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    /** A valid activated Congress whose only member is the given father. */
    private static CongressPo congress(final Founder member) {
        final List<Founder> members = List.of(member);
        final CongressPo po = Congress.initial();
        po.setActivated(true);
        po.setGeneration(1);
        po.setBells(0);
        po.setSelected(null);
        po.setJonesDeliveryPending(false);
        po.setMembers(members);
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder founder : Founder.values()) {
                if (founder.getCategory() == category
                        && !members.contains(founder)) {
                    candidates.add(founder);
                    break;
                }
            }
        }
        po.setCandidates(candidates);
        return po;
    }

    @Test
    void aFreeColonistCountsTwoPointsOnceAndReadingDoesNotChangeTheNation() {
        final Model model = load(data());
        final Player france = france(model);
        final BaseScore before = france.getBaseScore();
        final int units = france.getAllUnits().size();
        assertEquals(before.getSubtotal(), france.getBaseScore().getSubtotal());
        assertEquals(units, france.getAllUnits().size());

        final Unit colonist = model.addUnitOutSideColony(montreal(model));
        final BaseScore after = france.getBaseScore();
        assertEquals(before.getColonists() + 1, after.getColonists());
        assertEquals(before.getPopulationPoints() + 2,
                after.getPopulationPoints());
        assertEquals(before.getSubtotal() + 2, after.getSubtotal());
        assertEquals(1, france.getAllUnits().stream()
                .filter(unit -> unit.getId() == colonist.getId()).count());
    }

    @Test
    void anExpertKeepsItsSpecialtyThroughVeteranAndContinentalRoles() {
        final GameModelPo data = data();
        RebellionFixtures.set(data.getModel(), playerPo(data, FRANCE), true);
        final Model model = load(data);
        final Player france = france(model);
        final UnitFreeColonist colonist = (UnitFreeColonist) model
                .addUnitOutSideColony(montreal(model));
        final BaseScore colonistScore = france.getBaseScore();

        colonist.completeEducation(UnitType.EXPERT_FARMER);
        final BaseScore expert = france.getBaseScore();
        assertEquals(colonistScore.getExperts() + 1, expert.getExperts());
        assertEquals(colonistScore.getColonists() - 1, expert.getColonists());
        assertEquals(colonistScore.getSubtotal() + 2, expert.getSubtotal());

        colonist.completeEducation(UnitType.VETERAN_SOLDIER);
        assertTrue(colonist.isVeteran());
        assertEquals(expert.getSubtotal(), france.getBaseScore().getSubtotal());
        assertEquals(expert.getExperts(), france.getBaseScore().getExperts());

        colonist.setHoldingGuns(true);
        colonist.assignMilitaryRole(MilitaryRole.CONTINENTAL);
        assertEquals(MilitaryRole.CONTINENTAL, colonist.getMilitaryRole());
        assertEquals(expert.getSubtotal(), france.getBaseScore().getSubtotal());
        assertEquals(expert.getExperts(), france.getBaseScore().getExperts());
        assertEquals(colonistScore.getPopulationPoints() + 2,
                france.getBaseScore().getPopulationPoints());
    }

    @Test
    void aNativeConvertCountsAsAColonistWithTwoPoints() {
        final Model model = load(data());
        final Player france = france(model);
        final UnitFreeColonist colonist = (UnitFreeColonist) model
                .addUnitOutSideColony(montreal(model));
        final BaseScore before = france.getBaseScore();

        colonist.becomeNativeConvert(NativeNation.values()[0]);
        assertTrue(colonist.isNativeConvert());
        final BaseScore after = france.getBaseScore();
        assertEquals(before.getColonists(), after.getColonists());
        assertEquals(before.getSubtotal(), after.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({ "INDENTURED_SERVANT", "PETTY_CRIMINAL" })
    void aServantOrCriminalCountsOnePointInsteadOfTwo(final String type) {
        final BaseScore colonist = france(load(data())).getBaseScore();

        final GameModelPo data = data();
        final UnitPo person = data.getModel().getUnits().stream()
                .filter(unit -> FRANCE.equals(unit.getOwnerId())
                        && unit.getType() == UnitType.COLONIST)
                .findFirst().orElseThrow();
        person.setType(UnitType.valueOf(type));
        final BaseScore servant = france(load(data)).getBaseScore();
        assertEquals(colonist.getServants() + 1, servant.getServants());
        assertEquals(colonist.getColonists() - 1, servant.getColonists());
        assertEquals(colonist.getSubtotal() - 1, servant.getSubtotal());
    }

    @Test
    void aRecruitedFatherAddsFivePointsOnceAndChangesNoOtherComponent() {
        final BaseScore without = france(load(data())).getBaseScore();

        // Adam Smith has no effect on rebel support, so only the father
        // component moves.
        final GameModelPo data = data();
        playerPo(data, FRANCE).setCongress(congress(Founder.ADAM_SMITH));
        final BaseScore with = france(load(data)).getBaseScore();
        assertEquals(0, without.getFathers());
        assertEquals(1, with.getFathers());
        assertEquals(5, with.getFatherPoints());
        assertEquals(without.getPopulationPoints(), with.getPopulationPoints());
        assertEquals(without.getGoldPoints(), with.getGoldPoints());
        assertEquals(without.getSupportPoints(), with.getSupportPoints());
        assertEquals(without.getSubtotal() + 5, with.getSubtotal());
    }

    @Test
    void bolivarAddsFivePointsAsAFatherAndTwentyPointsThroughDisplayedSupport() {
        final BaseScore without = france(load(data())).getBaseScore();

        // Bolivar adds 40 of 200 points per resident to every colony of his
        // nation, i.e. 20 displayed percent: a separate, correct effect of the
        // support component, not of the father component.
        final GameModelPo data = data();
        playerPo(data, FRANCE).setCongress(congress(Founder.SIMON_BOLIVAR));
        final BaseScore with = france(load(data)).getBaseScore();
        assertEquals(1, with.getFathers());
        assertEquals(5, with.getFatherPoints());
        assertEquals(without.getPopulationPoints(), with.getPopulationPoints());
        assertEquals(without.getGoldPoints(), with.getGoldPoints());
        assertEquals(without.getSupportPercent() + 20,
                with.getSupportPercent());
        assertEquals(without.getSupportPoints() + 20, with.getSupportPoints());
        assertEquals(without.getSubtotal() + 5 + 20, with.getSubtotal());
    }

    @ParameterizedTest
    @CsvSource({ "0,0", "999,0", "1000,1", "1999,1" })
    void theTreasuryComponentFollowsTheLiveGold(final int gold,
            final long expected) {
        final Model model = load(data());
        final Player france = france(model);
        france.setGold(gold);
        assertEquals(expected, france.getBaseScore().getGoldPoints());
    }

    @Test
    void theSupportComponentIsTheDisplayedWholePercentage() {
        final Player france = france(load(data()));
        final BaseScore score = france.getBaseScore();
        assertEquals(france.getNationalRebelSupport().getDisplayPercent(),
                score.getSupportPercent());
        assertEquals(score.getSupportPercent(), score.getSupportPoints());
    }
}
