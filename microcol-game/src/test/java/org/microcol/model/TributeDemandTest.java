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

/** Contact, quote, cooldown and persistence boundaries for tribute. */
class TributeDemandTest {
    private Model model() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
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

    private void arm(final Model model, final int id) {
        ((UnitFreeColonist) model.getUnitById(id)).setHoldingGuns(true);
    }

    @Test
    void eligibleRoleContactAndHiddenContactAreRequired() {
        final Model model = model();
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        assertTrue(model.getTributeVisitors(dutch, france).isEmpty());
        arm(model, 0);
        assertEquals(List.of(model.getUnitById(0)),
                model.getTributeVisitors(dutch, france));
        ((UnitFreeColonist) model.getUnitById(0)).setHoldingGuns(false);
        ((UnitFreeColonist) model.getUnitById(0)).setMounted(true);
        assertEquals(List.of(model.getUnitById(0)),
                model.getTributeVisitors(dutch, france));
        model.getUnitById(0).placeToLocation(Location.of(30, 10));
        assertTrue(model.getTributeVisitors(dutch, france).isEmpty());

        final GameModelPo source = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        source.getModel().getGameManager().setGameStarted(true);
        source.getModel().getGameManager().setCurrentPlayer("Dutch");
        source.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().setVisible(new VisibilityPo());
        final Model hidden = Model.make(source.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit hiddenVisitor = hidden.getUnitById(0);
        hiddenVisitor.placeToLocation(Location.of(18, 10));
        arm(hidden, 0);
        assertFalse(hiddenVisitor.getOwner().isVisible(Location.of(17, 10)));
        assertTrue(hidden.getTributeVisitors(hiddenVisitor.getOwner(),
                hidden.getPlayerByName("France")).isEmpty());
    }

    @Test
    void seasonedScoutCanVisitButWagonAndShipCannot() {
        final Model model = model();
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        model.getUnitById(0).placeToLocation(Location.of(30, 10));
        final Unit scout = model.addUnitInEurope(UnitType.SEASONED_SCOUT,
                dutch);
        scout.placeToLocation(Location.of(18, 10));
        dutch.revealMapForUnit(scout);
        assertEquals(List.of(scout), model.getTributeVisitors(dutch, france));
        final Unit wagon = model.addUnitInEurope(UnitType.WAGON, dutch);
        wagon.placeToLocation(Location.of(18, 10));
        assertEquals(List.of(scout), model.getTributeVisitors(dutch, france));
        assertThrows(IllegalStateException.class,
                () -> model.prepareTributeDemand(wagon, france));

        final GameModelPo seaData = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        seaData.getModel().getGameManager().setGameStarted(true);
        seaData.getModel().getGameManager().setCurrentPlayer("Dutch");
        final var terrain = seaData.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(18, 10), TerrainType.OCEAN);
        seaData.getModel().getMap().setTerrainType(terrain);
        final Model sea = Model.make(seaData.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player sailor = sea.getPlayerByName("Dutch");
        final Unit ship = sea.addUnitInEurope(UnitType.CARAVEL, sailor);
        ship.placeToLocation(Location.of(18, 10));
        sailor.revealMapForUnit(ship);
        assertTrue(sea.getTributeVisitors(sailor, sea.getPlayerByName("France"))
                .isEmpty());
    }

    @Test
    void equalDoubleStrengthReceivesCappedGoldOnceAndStartsCooldown() {
        final Model model = model();
        final Player actor = model.getPlayerByName("Dutch");
        final Player rival = model.getPlayerByName("France");
        arm(model, 0);
        arm(model, 1);
        rival.setGold(317);
        final TributeDemand demand = model
                .prepareTributeDemand(model.getUnitById(0), rival);
        assertTrue(demand.isAccepted());
        assertEquals(3, demand.getActorStrength());
        assertEquals(1.5, demand.getRivalStrength());
        assertEquals(200, demand.getAmount());
        assertSame(demand,
                model.prepareTributeDemand(model.getUnitById(0), rival));
        final int actorGold = actor.getGold();
        assertEquals(200, model.confirmTributeDemand(demand));
        assertEquals(actorGold + 200, actor.getGold());
        assertEquals(117, rival.getGold());
        assertEquals(10, model.getTributeTurnsRemaining(actor, rival));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(actor, rival));
        final String after = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.confirmTributeDemand(demand));
        assertEquals(after, saved(model));

        final ModelPo ninth = model.save();
        ninth.getCalendar().setNumberOfPlayedTurns(9);
        final Model tooEarly = Model.make(ninth,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(1,
                tooEarly.getTributeTurnsRemaining(
                        tooEarly.getPlayerByName("Dutch"),
                        tooEarly.getPlayerByName("France")));
        final String ninthState = saved(tooEarly);
        assertThrows(IllegalStateException.class,
                () -> tooEarly.prepareTributeDemand(tooEarly.getUnitById(0),
                        tooEarly.getPlayerByName("France")));
        assertEquals(ninthState, saved(tooEarly));
        final ModelPo tenth = model.save();
        tenth.getCalendar().setNumberOfPlayedTurns(10);
        final Model eligible = Model.make(tenth,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(0,
                eligible.getTributeTurnsRemaining(
                        eligible.getPlayerByName("Dutch"),
                        eligible.getPlayerByName("France")));
        final Player eligibleActor = eligible.getPlayerByName("Dutch");
        final Player eligibleRival = eligible.getPlayerByName("France");
        final int totalGold = eligibleActor.getGold() + eligibleRival.getGold();
        final TributeDemand next = eligible
                .prepareTributeDemand(eligible.getUnitById(0), eligibleRival);
        assertTrue(next.isAccepted());
        assertEquals(117, next.getAmount());
        assertEquals(117, eligible.confirmTributeDemand(next));
        assertEquals(0, eligibleRival.getGold());
        assertEquals(totalGold,
                eligibleActor.getGold() + eligibleRival.getGold());
        assertEquals(10, eligible.getTributeTurnsRemaining(eligibleActor,
                eligibleRival));
    }

    @Test
    void lowerStrengthRefusesWithoutWarAndZeroTreasuryStartsCooldown() {
        final Model refused = model();
        arm(refused, 0);
        final Player rival = refused.getPlayerByName("France");
        final TributeDemand no = refused
                .prepareTributeDemand(refused.getUnitById(0), rival);
        assertFalse(no.isAccepted());
        assertEquals(0, no.getAmount());
        final String before = saved(refused);
        assertEquals(0, refused.confirmTributeDemand(no));
        assertNotEquals(before, saved(refused));
        assertEquals(EuropeanRelationState.PEACE, refused
                .getEuropeanRelation(refused.getPlayerByName("Dutch"), rival));
        assertEquals(10, refused.getTributeTurnsRemaining(
                refused.getPlayerByName("Dutch"), rival));

        final Model empty = model();
        arm(empty, 0);
        arm(empty, 1);
        final Player poorRival = empty.getPlayerByName("France");
        poorRival.setGold(0);
        final TributeDemand noMoney = empty
                .prepareTributeDemand(empty.getUnitById(0), poorRival);
        assertTrue(noMoney.isAccepted());
        assertEquals(0, noMoney.getAmount());
        assertEquals(0, empty.confirmTributeDemand(noMoney));
        assertEquals(10, empty.getTributeTurnsRemaining(
                empty.getPlayerByName("Dutch"), poorRival));
    }

    @Test
    void cancelAndStaleProposalDoNotPayOrStartCooldown() {
        final Model model = model();
        arm(model, 0);
        arm(model, 1);
        final Player rival = model.getPlayerByName("France");
        final TributeDemand canceled = model
                .prepareTributeDemand(model.getUnitById(0), rival);
        final int gold = model.getPlayerByName("Dutch").getGold();
        model.cancelTributeDemand(canceled);
        assertEquals(gold, model.getPlayerByName("Dutch").getGold());
        assertEquals(0, model.getTributeTurnsRemaining(
                model.getPlayerByName("Dutch"), rival));
        final TributeDemand stale = model
                .prepareTributeDemand(model.getUnitById(0), rival);
        rival.setGold(rival.getGold() - 1);
        final String before = saved(model);
        assertThrows(IllegalStateException.class,
                () -> model.confirmTributeDemand(stale));
        assertEquals(before, saved(model));
        model.cancelTributeDemand(stale);
        assertEquals(0, model.getTributeTurnsRemaining(
                model.getPlayerByName("Dutch"), rival));
    }

    @Test
    void changedStrengthAndVisitorRoleRejectWithoutAnyFurtherMutation() {
        final Model strength = model();
        arm(strength, 0);
        arm(strength, 1);
        final Player rival = strength.getPlayerByName("France");
        final TributeDemand quote = strength
                .prepareTributeDemand(strength.getUnitById(0), rival);
        ((UnitFreeColonist) strength.getUnitById(1)).setHoldingGuns(false);
        final String changedStrength = saved(strength);
        assertThrows(IllegalStateException.class,
                () -> strength.confirmTributeDemand(quote));
        assertEquals(changedStrength, saved(strength));
        assertEquals(0, strength.getTributeTurnsRemaining(
                strength.getPlayerByName("Dutch"), rival));

        final Model role = model();
        arm(role, 0);
        arm(role, 1);
        final Player roleRival = role.getPlayerByName("France");
        final TributeDemand roleQuote = role
                .prepareTributeDemand(role.getUnitById(0), roleRival);
        ((UnitFreeColonist) role.getUnitById(0)).setHoldingGuns(false);
        final String changedRole = saved(role);
        assertThrows(IllegalStateException.class,
                () -> role.confirmTributeDemand(roleQuote));
        assertEquals(changedRole, saved(role));
        assertEquals(0, role.getTributeTurnsRemaining(
                role.getPlayerByName("Dutch"), roleRival));
    }

    @Test
    void changedRelationContactAndRoundLeavePendingDemandUnpaid() {
        final Model contact = model();
        arm(contact, 0);
        arm(contact, 1);
        final Player rival = contact.getPlayerByName("France");
        final TributeDemand demand = contact
                .prepareTributeDemand(contact.getUnitById(0), rival);
        contact.getUnitById(0).placeToLocation(Location.of(30, 10));
        final String lostContact = saved(contact);
        assertThrows(IllegalStateException.class,
                () -> contact.confirmTributeDemand(demand));
        assertEquals(lostContact, saved(contact));

        final Model relation = model();
        arm(relation, 0);
        arm(relation, 1);
        final Player dutch = relation.getPlayerByName("Dutch");
        final Player french = relation.getPlayerByName("France");
        final TributeDemand quotedAtPeace = relation
                .prepareTributeDemand(relation.getUnitById(0), french);
        relation.declareHostilities(dutch, french);
        final String changedRelation = saved(relation);
        assertThrows(IllegalStateException.class,
                () -> relation.confirmTributeDemand(quotedAtPeace));
        assertEquals(changedRelation, saved(relation));
        relation.cancelTributeDemand(quotedAtPeace);
        assertEquals(0, relation.getTributeTurnsRemaining(dutch, french));

        final Model fresh = model();
        arm(fresh, 0);
        arm(fresh, 1);
        fresh.prepareTributeDemand(fresh.getUnitById(0),
                fresh.getPlayerByName("France"));
        final ModelPo laterPo = fresh.save();
        laterPo.getCalendar().setNumberOfPlayedTurns(1);
        final Model later = Model.make(laterPo,
                MissionImpl.getPredefinedGameOverEvaluators());
        final TributeDemand old = later
                .getPendingTributeDemand(later.getPlayerByName("Dutch"),
                        later.getPlayerByName("France"))
                .orElseThrow();
        final String laterBefore = saved(later);
        assertThrows(IllegalStateException.class,
                () -> later.confirmTributeDemand(old));
        assertEquals(laterBefore, saved(later));
    }

    @Test
    void pendingDemandRoundTripsWithoutChargingAndRejectsMalformedHistory() {
        final Model model = model();
        arm(model, 0);
        arm(model, 1);
        final Player rival = model.getPlayerByName("France");
        final TributeDemand proposal = model
                .prepareTributeDemand(model.getUnitById(0), rival);
        final String before = saved(model);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(before, saved(restored));
        final Player actor2 = restored.getPlayerByName("Dutch");
        final Player rival2 = restored.getPlayerByName("France");
        final TributeDemand resumed = restored
                .getPendingTributeDemand(actor2, rival2).orElseThrow();
        assertEquals(proposal.getAmount(), resumed.getAmount());
        assertEquals(proposal.getQuotedRound(), resumed.getQuotedRound());
        assertEquals(200, restored.confirmTributeDemand(resumed));
        final Model after = Model.make(restored.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(after.getPendingTributeDemand(after.getPlayerByName("Dutch"),
                after.getPlayerByName("France")).isEmpty());
        assertEquals(10,
                after.getTributeTurnsRemaining(after.getPlayerByName("Dutch"),
                        after.getPlayerByName("France")));

        final ModelPo malformed = model.save();
        malformed.getEuropeanRelations().get(0).getTributeDemand()
                .setQuotedRound(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(malformed,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final ModelPo future = after.save();
        future.getEuropeanRelations().get(0).setLastTributeRound(100);
        assertThrows(IllegalArgumentException.class, () -> Model.make(future,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void actorTreasuryOverflowRejectsBeforeSavingProposal() {
        final Model model = model();
        arm(model, 0);
        arm(model, 1);
        model.getPlayerByName("Dutch").setGold(Integer.MAX_VALUE);
        final Player rival = model.getPlayerByName("France");
        final String before = saved(model);
        assertThrows(IllegalArgumentException.class,
                () -> model.prepareTributeDemand(model.getUnitById(0), rival));
        assertEquals(before, saved(model));
        assertTrue(model
                .getPendingTributeDemand(model.getPlayerByName("Dutch"), rival)
                .isEmpty());
    }
}
