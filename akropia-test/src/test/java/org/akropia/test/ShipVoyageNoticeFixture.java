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
}
