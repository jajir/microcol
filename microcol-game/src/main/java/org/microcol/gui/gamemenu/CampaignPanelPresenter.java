package org.microcol.gui.gamemenu;

import java.util.function.Consumer;

import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.model.campaign.CampaignNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
public class CampaignPanelPresenter {

	final Logger logger = LoggerFactory.getLogger(getClass());

	private final GameController gameController;

	private final MainPanelPresenter mainFramePresenter;

	public interface Display {

		void updateLanguage();

		Button getButtonBack();

		void setOnSelectedMission(final Consumer<String> onSelectedMission);

	}

	@Inject
	public CampaignPanelPresenter(final CampaignPanelPresenter.Display display,
			final ChangeLanguageController changeLanguageController, final MainPanelPresenter mainFramePresenter,
			final GameController gameController) {
		this.mainFramePresenter = Preconditions.checkNotNull(mainFramePresenter);
		this.gameController = Preconditions.checkNotNull(gameController);
		changeLanguageController.addListener(listener -> display.updateLanguage());
		display.getButtonBack().setOnAction(event -> mainFramePresenter.showGameMenu());
		display.setOnSelectedMission(this::onSelectedMission);
	}

	private void onSelectedMission(final String missionName) {
		logger.debug("Mission {} was selected to play.", missionName);
		gameController.startCampaignMission(CampaignNames.defaultCampaign, missionName);
		mainFramePresenter.showGamePanel();
	}

}
