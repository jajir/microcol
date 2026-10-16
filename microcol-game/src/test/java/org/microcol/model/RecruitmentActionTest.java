package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.RecruitmentCandidatePo;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class RecruitmentActionTest {

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo scenario() {
        final GameModelPo data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() == null)
                .forEach(player -> player.setRecruitment(pool()));
        return data;
    }

    private RecruitmentPo pool() {
        final RecruitmentPo po = new RecruitmentPo();
        po.setCandidates(List.of(candidate(1, UnitType.COLONIST),
                candidate(2, UnitType.INDENTURED_SERVANT),
                candidate(3, UnitType.PETTY_CRIMINAL)));
        po.setNextCandidateId(4);
        po.setPaidPassages(0);
        po.setCrosses(0);
        po.setEarnedImmigrants(0);
        return po;
    }

    private RecruitmentCandidatePo candidate(final int id,
            final UnitType type) {
        final RecruitmentCandidatePo po = new RecruitmentCandidatePo();
        po.setId(id);
        po.setType(type);
        return po;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void exactFundsRecruitChosenCandidateAndPersistOneReplacement() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final RecruitmentOffer chosen = buyer.getRecruitmentOffers().get(1);
        buyer.setGold(chosen.getPrice());
        final int count = model.getAllUnits().size();
        final int expectedUnitId = model.getAllUnits().stream()
                .mapToInt(Unit::getId).max().orElseThrow() + 1;
        final List<RecruitmentOffer> before = buyer.getRecruitmentOffers();
        final Unit recruited = buyer.recruit(chosen);
        assertEquals(0, buyer.getGold());
        assertEquals(count + 1, model.getAllUnits().size());
        assertEquals(expectedUnitId, recruited.getId());
        assertSame(UnitType.INDENTURED_SERVANT, recruited.getType());
        assertTrue(recruited instanceof UnitFreeColonist);
        assertTrue(recruited.isAtEuropePier());
        assertSame(buyer, recruited.getOwner());
        final List<RecruitmentOffer> after = buyer.getRecruitmentOffers();
        assertEquals(before.get(0).getCandidateId(),
                after.get(0).getCandidateId());
        assertEquals(4, after.get(1).getCandidateId());
        assertEquals(before.get(2).getCandidateId(),
                after.get(2).getCandidateId());
        assertFalse(buyer.canRecruit(chosen));
        final String afterPurchase = snapshot(model);
        assertThrows(IllegalStateException.class, () -> buyer.recruit(chosen));
        assertEquals(afterPurchase, snapshot(model));

        ((UnitFreeColonist) recruited).setTools(80);
        data.setModel(model.save());
        final Path file = directory.resolve("recruitment.microcol");
        dao.saveToFile(file.toString(), data);
        final Model restored = load(dao.loadFromFile(file.toFile()));
        final Unit saved = restored.getUnitById(expectedUnitId);
        assertSame(UnitType.INDENTURED_SERVANT, saved.getType());
        assertEquals(80, ((UnitFreeColonist) saved).getTools());
        assertTrue(saved.isAtEuropePier());
        final Player reloaded = restored.getPlayerByName("Dutch");
        assertEquals(4,
                reloaded.getRecruitmentOffers().get(1).getCandidateId());
        assertEquals(1, reloaded.save().getRecruitment().getPaidPassages());
        assertEquals(0, reloaded.save().getRecruitment().getCrosses());
        assertEquals(0, reloaded.save().getRecruitment().getEarnedImmigrants());
    }

    @Test
    void insufficientFundsForeignAndReplacedPlayersCannotMutate() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final RecruitmentOffer offer = buyer.getRecruitmentOffers().get(0);
        buyer.setGold(offer.getPrice() - 1);
        final String before = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(NotEnoughtGoldException.class, () -> buyer.recruit(offer));
        assertEquals(before, snapshot(model));
        buyer.setGold(offer.getPrice());
        final Player foreign = model.getPlayerByName("Dutch's King");
        assertFalse(foreign.canRecruit(offer));
        final String beforeForeign = snapshot(model);
        assertThrows(IllegalStateException.class, () -> foreign.recruit(offer));
        assertEquals(beforeForeign, snapshot(model));
        final Model replacement = load(data);
        assertNotSame(buyer, replacement.getPlayerByName("Dutch"));
        assertFalse(replacement.getPlayerByName("Dutch").canRecruit(offer));
    }

    @Test
    void pendingEarnedCrossesHidePaidQuotesWithoutChangingState() {
        final GameModelPo data = scenario();
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment().setCrosses(100);
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, buyer::getRecruitmentOffers);
        assertEquals(before, snapshot(model));
    }

    @Test
    void universityPurchaseDoesNotDrawOrAdvancePassageCounters() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.MASTER_BLACKSMITH.getEuropePrice());
        final List<RecruitmentOffer> before = buyer.getRecruitmentOffers();
        buyer.buy(UnitType.MASTER_BLACKSMITH);
        final List<RecruitmentOffer> after = buyer.getRecruitmentOffers();
        for (int slot = 0; slot < 3; slot++) {
            assertEquals(before.get(slot).getCandidateId(),
                    after.get(slot).getCandidateId());
            assertSame(before.get(slot).getType(), after.get(slot).getType());
            assertEquals(before.get(slot).getPrice(),
                    after.get(slot).getPrice());
        }
        assertEquals(0, buyer.save().getRecruitment().getPaidPassages());
    }

    @Test
    void stoppedGameAndForeignTurnRejectOfferWithoutChangingAnyState() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        final RecruitmentOffer offer = buyer.getRecruitmentOffers().get(0);
        model.endTurn();
        final String afterTurn = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(IllegalStateException.class, () -> buyer.recruit(offer));
        assertEquals(afterTurn, snapshot(model));
        model.stop();
        final String afterStop = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(IllegalStateException.class, () -> buyer.recruit(offer));
        assertEquals(afterStop, snapshot(model));
    }

    @Test
    void exhaustedCandidateIdentityRejectsBeforeUnitOrTreasuryMutation() {
        final GameModelPo data = scenario();
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment()
                .setNextCandidateId(Integer.MAX_VALUE);
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final RecruitmentOffer offer = buyer.getRecruitmentOffers().get(0);
        buyer.setGold(offer.getPrice());
        final String before = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(IllegalStateException.class, () -> buyer.recruit(offer));
        assertEquals(before, snapshot(model));
    }

    @Test
    void overflowingPassageOrThresholdCountersRejectOnModelLoad() {
        final GameModelPo highPassages = scenario();
        highPassages.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment()
                .setPaidPassages(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> load(highPassages));

        final GameModelPo highThreshold = scenario();
        highThreshold.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment()
                .setEarnedImmigrants(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> load(highThreshold));
    }

    @Test
    void nextPassagePriceOverflowRejectsBeforeMaterialization() {
        final GameModelPo data = scenario();
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment().setPaidPassages(107374172);
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(Integer.MAX_VALUE);
        final RecruitmentOffer offer = buyer.getRecruitmentOffers().get(0);
        assertEquals(2147483640, offer.getPrice());
        final String before = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(IllegalStateException.class, () -> buyer.recruit(offer));
        assertEquals(before, snapshot(model));
    }

    @Test
    void extremeNegativeLoadedTreasuryCannotBypassAffordability() {
        final GameModelPo data = scenario();
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().setGold(Integer.MIN_VALUE);
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        final RecruitmentOffer offer = buyer.getRecruitmentOffers().get(0);
        final String before = snapshot(model);
        assertFalse(buyer.canRecruit(offer));
        assertThrows(NotEnoughtGoldException.class, () -> buyer.recruit(offer));
        assertThrows(NotEnoughtGoldException.class,
                () -> buyer.buy(UnitType.COLONIST));
        assertEquals(before, snapshot(model));
    }

    @Test
    void plainGsonCandidateCloneRetainsRegisteredTypeIdentity() {
        final RecruitmentCandidatePo candidate = candidate(7,
                UnitType.PETTY_CRIMINAL);
        final Gson gson = new Gson();
        final String json = gson.toJson(candidate);
        assertTrue(json.contains("\"PETTY_CRIMINAL\""));
        final RecruitmentCandidatePo restored = gson.fromJson(json,
                RecruitmentCandidatePo.class);
        assertEquals(7, restored.getId());
        assertSame(UnitType.PETTY_CRIMINAL, restored.getType());
    }
}
