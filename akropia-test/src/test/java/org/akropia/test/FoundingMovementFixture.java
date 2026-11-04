package org.akropia.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.io.File;
import org.akropia.gui.FileSelectingService;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.page.TestContext;

final class FoundingMovementFixture {
    private FoundingMovementFixture() {
    }

    static void exhaust(final TestContext context, final File source) {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromFile(source);
        data.getModel().getUnits().forEach(unit -> unit.setAvailableMoves(0));
        final File prepared = new File(System.getProperty("user.home"),
                "founding-no-movement.akropia");
        dao.saveToFile(prepared.toString(), data);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
