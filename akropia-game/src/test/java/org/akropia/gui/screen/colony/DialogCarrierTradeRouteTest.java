package org.akropia.gui.screen.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.akropia.model.Model;
import org.akropia.model.TradeRoute;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteStop;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DialogCarrierTradeRouteTest {
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "1 | Route — A1",
            "3 | Route — A1, A2, A3", "4 | Route — A1, A2, A3, …" })
    void colonyPreviewHasAtMostThreeNames(final int count,
            final String expected) {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromFile(new File(
                        "../akropia-test/src/test/scenarios/F222-colony-markers.akropia"))
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
        final List<TradeRouteStop> stops = IntStream.range(0, count)
                .mapToObj(TradeRouteStop::ofColony)
                .collect(Collectors.toList());
        final TradeRoute route = model.createTradeRoute(
                model.getColonyById(0).orElseThrow().getOwner(), "Route",
                TradeRouteKind.LAND, 11, stops);
        assertEquals(expected,
                DialogCarrierTradeRoute.routeLabel(model, route));
    }
}
