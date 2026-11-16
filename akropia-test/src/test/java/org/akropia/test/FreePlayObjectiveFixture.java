package org.akropia.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.akropia.gui.FileSelectingService;
import org.akropia.model.Congress;
import org.akropia.model.Founder;
import org.akropia.model.FreePlayGoal;
import org.akropia.model.UnitType;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.CongressPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.page.TestContext;

/** A legal 24-member Congress one completed national update before victory. */
final class FreePlayObjectiveFixture {
    static void domination(TestContext context, File source, String name) {
        GameModelPoDao dao = new GameModelPoDao();
        GameModelPo game = dao.loadFromFile(source);
        game.getModel().getCalendar().setScoredPlay(true);
        game.getModel().setFreePlayGoals(List.of(FreePlayGoal.DOMINATION));
        // Quebec is the last French colony; Montreal was captured earlier.
        game.getModel().getColonies().stream().filter(c -> c.getId() == 1)
                .findFirst().ifPresent(c -> {
                    c.setOwnerName("Dutch");
                    c.setLastColonialAffiliation("Dutch");
                });
        game.getModel().getUnits().stream().filter(u -> u.getId() == 5)
                .findFirst().orElseThrow().setOwnerId("Dutch");
        File prepared = new File(System.getProperty("user.home"), name);
        dao.saveToFile(prepared.getPath(), game);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    static void congress(TestContext context, File source, String name) {
        GameModelPoDao dao = new GameModelPoDao();
        GameModelPo game = dao.loadFromFile(source);
        game.getModel().getCalendar().setScoredPlay(true);
        game.getModel().setFreePlayGoals(List.of(FreePlayGoal.CONGRESS));
        PlayerPo human = game.getModel().getGameManager().getPlayers().stream()
                .filter(p -> "Dutch".equals(p.getName())).findFirst()
                .orElseThrow();
        CongressPo congress = Congress.initial();
        congress.setActivated(true);
        congress.setGeneration(25);
        List<Founder> members = new ArrayList<>(
                Arrays.asList(Founder.values()));
        members.remove(Founder.HERNANDO_DE_SOTO);
        congress.setMembers(members);
        congress.setCandidates(List.of(Founder.HERNANDO_DE_SOTO));
        congress.setSelected(Founder.HERNANDO_DE_SOTO);
        congress.setOwls(10000);
        human.setCongress(congress);
        human.getRecruitment().getCandidates()
                .forEach(candidate -> candidate.setType(UnitType.COLONIST));
        File prepared = new File(System.getProperty("user.home"), name);
        dao.saveToFile(prepared.getPath(), game);
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
