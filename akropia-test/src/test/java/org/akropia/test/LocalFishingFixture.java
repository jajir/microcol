package org.akropia.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.akropia.gui.FileSelectingService;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.page.TestContext;

final class LocalFishingFixture {
    private LocalFishingFixture() {
    }

    static void exhaustFisherman(final TestContext context) {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(
                new File("src/test/scenarios/F234-local-fishing.akropia"));
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setMovementThirds(0);
        final File prepared = new File(System.getProperty("user.home"),
                "fishing-no-movement.akropia");
        dao.saveToFile(prepared.toString(), data);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
