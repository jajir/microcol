package org.akropia.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.List;
import org.akropia.gui.FileSelectingService;
import org.akropia.model.*;
import org.akropia.model.unit.Ship;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.page.TestContext;

final class ShipVoyageNoticeFixture {
    private ShipVoyageNoticeFixture() {
    }

    static void prepare(final TestContext context, final File source,
            final boolean automatic) {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(source);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Ship ship = (Ship) model.getUnitById(3);
        if (automatic) {
            final TradeRoute route = model.createTradeRoute(ship.getOwner(),
                    "Silent sea", TradeRouteKind.SEA, 1,
                    List.of(TradeRouteStop.europe(),
                            TradeRouteStop.ofColony(1)));
            model.assignTradeRoute(ship, route, 0);
        }
        ship.placeToLocation(Location.of(model.getMap().getMaxLocationX(), 14));
        ship.placeToHighSeas(true);
        while (((PlaceHighSea) ship.getPlace()).getRemainigTurns() > 1) {
            ship.advanceVoyage();
        }
        data.setModel(model.save());
        final File prepared = new File(System.getProperty("user.home"),
                "ship-voyage-notice.akropia");
        dao.saveToFile(prepared.toString(), data);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    static void prepareColonyNotices(final TestContext context,
            final File source) {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(source);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = model.getColonyById(0).orElseThrow();
        model.addTurnEvent(new org.akropia.model.turnevent.TurnEventGoTo(
                colony.getOwner().getName(),
                org.akropia.model.turnevent.TurnEventGoTo.Kind.ARRIVED, 3,
                UnitType.MERCHANTMAN, colony.getName())
                .destination(colony.getId(), false));
        model.addTurnEvent(new org.akropia.model.turnevent.TurnEventShipRepair(
                colony.getOwner().getName(),
                org.akropia.model.turnevent.TurnEventShipRepair.Kind.COMPLETED,
                3, UnitType.MERCHANTMAN, colony.getName(), false, 0, List.of())
                .atColony(colony.getId()));
        final var famine = new org.akropia.model.store.TurnEventPo();
        famine.setType("TurnEventFamineWillPlagueColony");
        famine.setPlayerName(colony.getOwner().getName());
        famine.setColonyId(colony.getId());
        famine.setColonyName(colony.getName());
        model.addTurnEvent(
                org.akropia.model.turnevent.TurnEventFamineWillPlagueColony
                        .tryLoad(famine));
        model.renameColony(colony.getOwner(), colony, "Renamed port");
        data.setModel(model.save());
        final File prepared = new File(System.getProperty("user.home"),
                "colony-notice-links.akropia");
        dao.saveToFile(prepared.toString(), data);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

}
