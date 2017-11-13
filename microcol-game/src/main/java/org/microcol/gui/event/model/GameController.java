package org.microcol.gui.event.model;

import java.io.File;

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
	
	public void writeModelToFile(final File targetFile) {
		modelDao.saveToFile(targetFile.getAbsolutePath(), gameModelController.getModel().save());
	}

	public void loadModelFromFile(final File sourceFile) {
		gameModelController.setAndStartModel(Model.make(modelDao.loadModel(sourceFile)));
	}
	
}
