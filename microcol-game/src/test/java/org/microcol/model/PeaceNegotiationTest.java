package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/** Real wartime contact, saved terms, and one-time peace payment. */
class PeaceNegotiationTest {
    private Model model(final boolean franklin) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        data.getModel().getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.WAR);
        if (franklin) {
            final var congress = data.getModel().getGameManager().getPlayers()
                    .stream().filter(p -> p.getName().equals("Dutch"))
                    .findFirst().orElseThrow().getCongress();
            congress.setActivated(true);
            congress.setMembers(List.of(Founder.BENJAMIN_FRANKLIN));
            congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                    Founder.THOMAS_PAINE, Founder.ADAM_SMITH,
                    Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
            congress.setGeneration(2);
        }
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit visitor = model.getUnitById(0);
        visitor.placeToLocation(Location.of(18, 10));
        visitor.getOwner().revealMapForUnit(visitor);
        return model;
    }

    private String saved(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void onlyVisibleAdjacentColonistsCanVisitAnAiRivalAtWar() {
        final Model model = model(false);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        assertEquals(List.of(model.getUnitById(0)),
                model.getPeaceVisitors(dutch, france));
        assertTrue(model
                .getPeaceVisitors(dutch, model.getPlayerByName("Dutch's King"))
                .isEmpty());
        final Unit visitor = model.getUnitById(0);
        visitor.placeToLocation(Location.of(30, 10));
        assertTrue(model.getPeaceVisitors(dutch, france).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> model.preparePeaceOffer(visitor, france));
        visitor.placeToLocation(Location.of(18, 10));
        dutch.revealMapForUnit(visitor);
        assertEquals(List.of(visitor), model.getPeaceVisitors(dutch, france));
        model.acceptPeace(dutch, france);
        assertTrue(model.getPeaceVisitors(dutch, france).isEmpty());
    }

    @Test
    void unseenAdjacentUnitAndColonyDoNotOfferContact() {
        final GameModelPo hiddenData = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        final ModelPo hiddenState = hiddenData.getModel();
        hiddenState.getGameManager().setGameStarted(true);
        hiddenState.getGameManager().setCurrentPlayer("Dutch");
        hiddenState.getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.WAR);
        hiddenState.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().setVisible(new VisibilityPo());
        final Model hidden = Model.make(hiddenState,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = hidden.getPlayerByName("Dutch");
        final Player france = hidden.getPlayerByName("France");
        final Unit visitor = hidden.getUnitById(0);
        visitor.placeToLocation(Location.of(18, 10));
        assertFalse(dutch.isVisible(Location.of(17, 10)));
        assertTrue(hidden.getPeaceVisitors(dutch, france).isEmpty());
        final String before = saved(hidden);
        assertThrows(IllegalStateException.class,
                () -> hidden.preparePeaceOffer(visitor, france));
        assertEquals(before, saved(hidden));
        dutch.revealMapForUnit(visitor);
        assertTrue(dutch.isVisible(Location.of(17, 10)));
        assertEquals(List.of(visitor), hidden.getPeaceVisitors(dutch, france));
    }

    @Test
    void stoppedAndOtherPlayerTurnRejectPendingOfferWithoutPayment() {
        final Model wrongTurnSource = model(false);
        final ModelPo wrongTurnState = wrongTurnSource.save();
        wrongTurnState.getGameManager().setCurrentPlayer("France");
        final Model wrongTurn = Model.make(wrongTurnState,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = wrongTurn.getPlayerByName("Dutch");
        final Player france = wrongTurn.getPlayerByName("France");
        assertTrue(wrongTurn.getPeaceVisitors(dutch, france).isEmpty());
        final String wrongTurnBefore = saved(wrongTurn);
        assertThrows(IllegalStateException.class, () -> wrongTurn
                .preparePeaceOffer(wrongTurn.getUnitById(0), france));
        assertEquals(wrongTurnBefore, saved(wrongTurn));

        final Model stopped = model(false);
        final PeaceOffer pending = stopped.preparePeaceOffer(
                stopped.getUnitById(0), stopped.getPlayerByName("France"));
        stopped.stop();
        assertTrue(stopped.getPeaceVisitors(stopped.getPlayerByName("Dutch"),
                stopped.getPlayerByName("France")).isEmpty());
        final String stoppedBefore = saved(stopped);
        assertThrows(IllegalStateException.class,
                () -> stopped.acceptPeaceOffer(pending));
        assertEquals(stoppedBefore, saved(stopped));
    }

    @Test
    void acceptedTreatyClosesOrdinaryAiAttackQuery() {
        final Model model = model(false);
        final ModelPo wartime = model.save();
        wartime.getGameManager().setCurrentPlayer("France");
        final Model aiAtWar = Model.make(wartime,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player warFrance = aiAtWar.getPlayerByName("France");
        final Player warDutch = aiAtWar.getPlayerByName("Dutch");
        assertTrue(aiAtWar.areHostile(warFrance, warDutch));
        assertTrue(aiAtWar.getUnitById(6)
                .isPossibleToAttackAt(Location.of(18, 10)));

        final PeaceOffer offer = model.preparePeaceOffer(model.getUnitById(0),
                model.getPlayerByName("France"));
        model.acceptPeaceOffer(offer);
        final ModelPo peacetime = model.save();
        peacetime.getGameManager().setCurrentPlayer("France");
        final Model aiAtPeace = Model.make(peacetime,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player peaceFrance = aiAtPeace.getPlayerByName("France");
        final Player peaceDutch = aiAtPeace.getPlayerByName("Dutch");
        assertFalse(aiAtPeace.areHostile(peaceFrance, peaceDutch));
        assertFalse(aiAtPeace.getUnitById(6)
                .isPossibleToAttackAt(Location.of(18, 10)));
    }

    @Test
    void strengthQuoteAndFranklinDiscountAreFixedBeforeAcceptance() {
        for (final boolean franklin : new boolean[] { false, true }) {
            final Model model = model(franklin);
            ((UnitFreeColonist) model.getUnitById(0)).setHoldingGuns(true);
            ((UnitFreeColonist) model.getUnitById(6)).setHoldingGuns(true);
            ((UnitFreeColonist) model.getUnitById(4)).setHoldingGuns(true);
            final Player actor = model.getPlayerByName("Dutch");
            final Player rival = model.getPlayerByName("France");
            final PeaceOffer offer = model
                    .preparePeaceOffer(model.getUnitById(0), rival);
            assertEquals(1.5, offer.getActorStrength());
            assertEquals(3, offer.getRivalStrength());
            assertEquals(franklin ? 200 : 400, offer.getPrice());
            assertEquals(actor.getGold() - offer.getPrice(),
                    offer.getActorGoldAfter());
            assertEquals(rival.getGold() + offer.getPrice(),
                    offer.getRivalGoldAfter());
            assertSame(offer,
                    model.preparePeaceOffer(model.getUnitById(0), rival));
            final int actorGold = actor.getGold();
            final int rivalGold = rival.getGold();
            model.acceptPeaceOffer(offer);
            assertEquals(actorGold - offer.getPrice(), actor.getGold());
            assertEquals(rivalGold + offer.getPrice(), rival.getGold());
            assertEquals(EuropeanRelationState.PEACE,
                    model.getEuropeanRelation(actor, rival));
            assertEquals(EuropeanRelationState.PEACE,
                    model.getEuropeanRelation(rival, actor));
            assertTrue(model.getPendingPeaceOffer(actor, rival).isEmpty());
            final String accepted = saved(model);
            assertThrows(IllegalStateException.class,
                    () -> model.acceptPeaceOffer(offer));
            assertEquals(accepted, saved(model));
        }
    }

    @Test
    void cancellationAndLostContactLeaveWarAndGoldUntouched() {
        final Model model = model(false);
        final Player actor = model.getPlayerByName("Dutch");
        final Player rival = model.getPlayerByName("France");
        final int gold = actor.getGold();
        final PeaceOffer offer = model.preparePeaceOffer(model.getUnitById(0),
                rival);
        model.getUnitById(0).placeToLocation(Location.of(30, 10));
        final String stale = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeaceOffer(offer));
        assertEquals(stale, saved(model));
        model.cancelPeaceOffer(offer);
        assertEquals(gold, actor.getGold());
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(actor, rival));
        assertTrue(model.getPendingPeaceOffer(actor, rival).isEmpty());
    }

    @Test
    void pendingTermsSurviveDiskStateWithoutChargingUntilConfirmed() {
        final Model model = model(false);
        final Player rival = model.getPlayerByName("France");
        final PeaceOffer pending = model.preparePeaceOffer(model.getUnitById(0),
                rival);
        final String saved = saved(model);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(saved, saved(restored));
        final Player restoredActor = restored.getPlayerByName("Dutch");
        final Player restoredRival = restored.getPlayerByName("France");
        final PeaceOffer offer = restored
                .getPendingPeaceOffer(restoredActor, restoredRival)
                .orElseThrow();
        assertEquals(pending.getPrice(), offer.getPrice());
        assertEquals(pending.getVisitorId(), offer.getVisitorId());
        restored.acceptPeaceOffer(offer);
        final Model again = Model.make(restored.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(EuropeanRelationState.PEACE,
                again.getEuropeanRelation(again.getPlayerByName("Dutch"),
                        again.getPlayerByName("France")));
        assertTrue(again.getPendingPeaceOffer(again.getPlayerByName("Dutch"),
                again.getPlayerByName("France")).isEmpty());
    }

    @Test
    void insufficientGoldAndChangedStrengthOrTreasuryRejectBeforePayment() {
        final Model model = model(false);
        final Player actor = model.getPlayerByName("Dutch");
        final Player rival = model.getPlayerByName("France");
        actor.setGold(199);
        final PeaceOffer poor = model.preparePeaceOffer(model.getUnitById(0),
                rival);
        assertEquals(300, poor.getPrice());
        final String before = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeaceOffer(poor));
        assertEquals(before, saved(model));
        model.cancelPeaceOffer(poor);

        actor.setGold(500);
        final PeaceOffer quote = model.preparePeaceOffer(model.getUnitById(0),
                rival);
        ((UnitFreeColonist) model.getUnitById(0)).setHoldingGuns(true);
        final String changedStrength = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeaceOffer(quote));
        assertEquals(changedStrength, saved(model));
        ((UnitFreeColonist) model.getUnitById(0)).setHoldingGuns(false);
        rival.setGold(rival.getGold() + 1);
        final String changedGold = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeaceOffer(quote));
        assertEquals(changedGold, saved(model));
        model.acceptPeace(actor, rival);
        assertTrue(model.getPendingPeaceOffer(actor, rival).isEmpty());
        final String peace = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptPeaceOffer(quote));
        assertEquals(peace, saved(model));
    }

    @Test
    void adjacentWagonAndCoastalShipAreNotPeaceVisitors() {
        final Model land = model(false);
        final Player dutch = land.getPlayerByName("Dutch");
        final Player france = land.getPlayerByName("France");
        land.getUnitById(0).placeToLocation(Location.of(30, 10));
        final Unit wagon = land.addUnitInEurope(UnitType.WAGON, dutch);
        wagon.placeToLocation(Location.of(18, 10));
        dutch.revealMapForUnit(wagon);
        assertTrue(land.getPeaceVisitors(dutch, france).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> land.preparePeaceOffer(wagon, france));

        final GameModelPo seaData = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        seaData.getModel().getGameManager().setGameStarted(true);
        seaData.getModel().getGameManager().setCurrentPlayer("Dutch");
        seaData.getModel().getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.WAR);
        final var terrain = seaData.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(18, 10), TerrainType.OCEAN);
        seaData.getModel().getMap().setTerrainType(terrain);
        final Model sea = Model.make(seaData.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player sailor = sea.getPlayerByName("Dutch");
        final Player coastOwner = sea.getPlayerByName("France");
        final Unit ship = sea.addUnitInEurope(UnitType.CARAVEL, sailor);
        ship.placeToLocation(Location.of(18, 10));
        sailor.revealMapForUnit(ship);
        assertTrue(sea.getPeaceVisitors(sailor, coastOwner).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> sea.preparePeaceOffer(ship, coastOwner));
    }

    @Test
    void rivalTreasuryOverflowRejectsWithoutChangingWarOrEitherBalance() {
        final Model model = model(false);
        final Player actor = model.getPlayerByName("Dutch");
        final Player rival = model.getPlayerByName("France");
        rival.setGold(Integer.MAX_VALUE);
        final PeaceOffer offer = model.preparePeaceOffer(model.getUnitById(0),
                rival);
        final String before = saved(model);
        assertThrows(ArithmeticException.class,
                () -> model.acceptPeaceOffer(offer));
        assertEquals(before, saved(model));
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(actor, rival));
    }

    @Test
    void malformedPendingOfferDoesNotLoadAsAValidRelation() {
        final Model model = model(false);
        model.preparePeaceOffer(model.getUnitById(0),
                model.getPlayerByName("France"));
        final ModelPo missing = model.save();
        missing.getEuropeanRelations().get(0).getPeaceOffer().setPrice(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(missing,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final ModelPo wrongPair = model.save();
        wrongPair.getEuropeanRelations().get(0).getPeaceOffer()
                .setRival("Unknown");
        assertThrows(IllegalArgumentException.class, () -> Model.make(wrongPair,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
