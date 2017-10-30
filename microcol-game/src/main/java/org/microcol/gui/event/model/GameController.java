package org.microcol.gui.event.model;

import org.microcol.model.Model;
import org.microcol.model.store.ModelDao;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameController {

	private final GameModelController gameModelController;

	private final ModelDao modelDao;

	@Inject
	GameController(final GameModelController gameModelController, final ModelDao modelDao) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
		this.modelDao = Preconditions.checkNotNull(modelDao);
	}

	public void startScenario(final String fileName) {
		gameModelController.setAndStartModel(Model.make(modelDao.loadPredefinedModel(fileName)));
	}

	public void startNewDefaultGame(){
		gameModelController.startNewDefaultGame();
	}
	
}
