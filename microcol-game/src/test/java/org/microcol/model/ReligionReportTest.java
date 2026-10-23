package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.ReligionReport.Immigration;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;

/**
 * R-0199 religion report: each colony shows the cross output of its next
 * update, the national total is the sum of those outputs, and the accumulated
 * crosses, target and remainder come from the shared nation state. Penn's bonus
 * is read once inside each colony. The report reads only the viewer's own
 * nation, changes nothing and survives a save.
 */
class ReligionReportTest {

    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String DUTCH = "Dutch";

    /** The thrive Dutch nation with the given saved cross count. */
    private static GameModelPo withCrosses(final int crosses) {
        final GameModelPo data = thrive();
        dutchPo(data).getRecruitment().setCrosses(crosses);
        return data;
    }

    private static GameModelPo thrive() {
        return new GameModelPoDao().loadFromClassPath(THRIVE);
    }

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static PlayerPo dutchPo(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> DUTCH.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private static int crossesOf(final ReligionReport report,
            final Colony colony) {
        return report.getColonies().stream()
                .filter(output -> output.getColonyId() == colony.getId())
                .findFirst().orElseThrow().getCrosses();
    }

    @Test
    void aNationWithoutColoniesHasNoOutputAndTheWholeRemainder() {
        final Model model = load(
                new GameModelPoDao().loadFromClassPath(FREE_PLAY));
        final Player dutch = model.getPlayerByName(DUTCH);
        assertTrue(dutch.getColonies().isEmpty());

        final ReligionReport report = model.getReligionReport(dutch);
        assertTrue(report.getColonies().isEmpty());
        assertEquals(0, report.getNationalOutput());
        assertEquals(dutch.getImmigrationCrosses(), report.getAccumulated());
        assertEquals(dutch.getImmigrationTarget(), report.getTarget());
        assertEquals(report.getTarget() - report.getAccumulated(),
                report.getRemaining());
        assertEquals(Immigration.NONE, report.getImmigration());
        assertEquals(0, report.getChoiceOfferCount());
    }

    @Test
    void eachColonyIsListedOnceAndTheNationalTotalIsTheirSum() {
        final Model model = load(thrive());
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        final Colony third = model.getColonyById(2).orElseThrow();

        // Each of the three colonies carries an unstaffed chapel: one cross.
        final ReligionReport report = model.getReligionReport(first.getOwner());
        assertEquals(3, report.getColonies().size());
        assertEquals(1, crossesOf(report, first));
        assertEquals(1, crossesOf(report, second));
        assertEquals(1, crossesOf(report, third));
        assertEquals(3, report.getNationalOutput());
    }

    @Test
    void unattendedAndStaffedOutputFollowTheWorkerAndTheCreditIsTheSameSum() {
        final Model model = load(thrive());
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        final Colony third = model.getColonyById(2).orElseThrow();
        final Player owner = first.getOwner();
        for (final Colony colony : List.of(first, second, third)) {
            colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        }
        assertEquals(3, model.getReligionReport(owner).getNationalOutput());

        // A colonist in the chapel doubles its output at the next update.
        final Unit worker = model.addUnitOutSideColony(first);
        worker.placeToColonyStructureSlot(first
                .getConstructionByType(ConstructionType.CHAPEL).getSlotAt(0));
        final ReligionReport staffed = model.getReligionReport(owner);
        assertEquals(2, crossesOf(staffed, first));
        assertEquals(1, crossesOf(staffed, second));
        assertEquals(1, crossesOf(staffed, third));
        assertEquals(4, staffed.getNationalOutput());

        final int accumulated = owner.getImmigrationCrosses();
        first.startTurn();
        second.startTurn();
        third.startTurn();
        assertEquals(accumulated + staffed.getNationalOutput(),
                owner.getImmigrationCrosses());
    }

    @Test
    void pennBonusIsReadOncePerBuildingAndNotAgainInTheNationalTotal() {
        final GameModelPo data = thrive();
        final CongressPo congress = dutchPo(data).getCongress();
        final List<Founder> members = new ArrayList<>(congress.getMembers());
        members.add(Founder.WILLIAM_PENN);
        congress.setMembers(members);
        final List<Founder> candidates = new ArrayList<>(
                congress.getCandidates());
        candidates.set(candidates.indexOf(Founder.WILLIAM_PENN),
                Founder.JUAN_DE_SEPULVEDA);
        congress.setCandidates(candidates);
        final Model model = load(data);
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        final Colony third = model.getColonyById(2).orElseThrow();
        final Unit worker = model.addUnitOutSideColony(first);
        worker.placeToColonyStructureSlot(first
                .getConstructionByType(ConstructionType.CHAPEL).getSlotAt(0));

        final ReligionReport report = model.getReligionReport(first.getOwner());
        // Staffed chapel 2 becomes 3; each unstaffed chapel 1 stays 1. The
        // total is 5, not 5 multiplied by 3/2 once more.
        assertEquals(3, crossesOf(report, first));
        assertEquals(1, crossesOf(report, second));
        assertEquals(1, crossesOf(report, third));
        assertEquals(5, report.getNationalOutput());
    }

    @Test
    void thresholdBeforeAtAndAfterKeepsProgressTargetAndRemainderConsistent() {
        final int target = load(withCrosses(0)).getPlayerByName(DUTCH)
                .getImmigrationTarget();
        for (final int crosses : new int[] { target - 1, target, target + 1 }) {
            final Model model = load(withCrosses(crosses));
            final Player dutch = model.getPlayerByName(DUTCH);
            final ReligionReport report = model.getReligionReport(dutch);
            assertEquals(crosses, report.getAccumulated());
            assertEquals(target, report.getTarget());
            assertEquals(Math.max(0, target - crosses), report.getRemaining());
            assertEquals(crosses >= target ? Immigration.ARRIVES_NEXT_TURN
                    : Immigration.NONE, report.getImmigration());
            assertEquals(0, report.getChoiceOfferCount());
        }
    }

    @Test
    void aPendingBrewsterChoiceShowsItsOffersWithoutSpendingCrosses() {
        final GameModelPo data = withCrosses(230);
        final CongressPo congress = dutchPo(data).getCongress();
        congress.setMembers(List.of(Founder.WILLIAM_BREWSTER));
        congress.setSelected(null);
        dutchPo(data).getRecruitment().getCandidates().forEach(candidate -> {
            if (candidate.getType() == UnitType.INDENTURED_SERVANT
                    || candidate.getType() == UnitType.PETTY_CRIMINAL) {
                candidate.setType(UnitType.COLONIST);
            }
        });
        final Model model = load(data);
        final Player dutch = model.getPlayerByName(DUTCH);
        model.endTurn();
        model.endTurn();
        assertTrue(model.isAwaitingImmigrationChoice());

        final ReligionReport report = model.getReligionReport(dutch);
        assertEquals(Immigration.CHOICE_WAITING, report.getImmigration());
        assertEquals(3, report.getChoiceOfferCount());
        assertEquals(230, report.getAccumulated());
        assertEquals(0, report.getRemaining());
        // Reading again neither settles the offer nor spends the crosses.
        assertEquals(230, model.getReligionReport(dutch).getAccumulated());
        assertEquals(230, dutch.getImmigrationCrosses());
        assertTrue(model.isAwaitingImmigrationChoice());
    }

    @Test
    void aDeclaredNationKeepsItsReachedThresholdAsBlockedWithoutAnError() {
        final int target = load(withCrosses(0)).getPlayerByName(DUTCH)
                .getImmigrationTarget();
        final GameModelPo data = withCrosses(target);
        RebellionFixtures.set(data.getModel(), dutchPo(data), true);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName(DUTCH);
        assertFalse(dutch.canTradeInEurope());

        final ReligionReport report = model.getReligionReport(dutch);
        assertEquals(Immigration.BLOCKED_EUROPE, report.getImmigration());
        assertEquals(target, report.getAccumulated());
        assertEquals(0, report.getRemaining());
        assertEquals(0, report.getChoiceOfferCount());
    }

    @Test
    void readingTheReportChangesNoCrossesGoldOrColonyStock() {
        final Model model = load(withCrosses(99));
        final Player dutch = model.getPlayerByName(DUTCH);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final int gold = dutch.getGold();
        final int stock = colony.getWarehouse().getGoods(GoodsType.CROSS)
                .getAmount();
        for (int i = 0; i < 3; i++) {
            model.getReligionReport(dutch);
        }
        assertEquals(99, dutch.getImmigrationCrosses());
        assertEquals(gold, dutch.getGold());
        assertEquals(stock,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertFalse(dutch.hasPendingImmigrant());
        assertEquals(0, dutch.save().getRecruitment().getEarnedImmigrants());
    }

    @Test
    void anotherGamesSameNamedNationAndACrownAreRejected() {
        final Model model = load(thrive());
        final Model other = load(thrive());
        final Player sameName = other.getPlayerByName(DUTCH);
        // Player equality is by name, so identity is the only check.
        assertEquals(model.getPlayerByName(DUTCH), sameName);
        assertThrows(IllegalArgumentException.class,
                () -> model.getReligionReport(sameName));

        final Model freePlay = load(
                new GameModelPoDao().loadFromClassPath(FREE_PLAY));
        final Player king = freePlay.getPlayers().stream()
                .filter(Player::isKing).findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> freePlay.getReligionReport(king));
    }

    @Test
    void aSavedAndReloadedNationShowsTheSameFigures() {
        final Model model = load(withCrosses(99));
        final ReligionReport before = model
                .getReligionReport(model.getPlayerByName(DUTCH));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ReligionReport after = restored
                .getReligionReport(restored.getPlayerByName(DUTCH));
        assertEquals(before.getNationalOutput(), after.getNationalOutput());
        assertEquals(before.getAccumulated(), after.getAccumulated());
        assertEquals(before.getTarget(), after.getTarget());
        assertEquals(before.getRemaining(), after.getRemaining());
        assertEquals(before.getImmigration(), after.getImmigration());
        assertEquals(before.getColonies().size(), after.getColonies().size());
    }
}
