package org.microcol.gui.event;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.SelectNextUnitController;
import org.microcol.model.Model;

import com.google.inject.Inject;

public class SelectNextUnitListener {
	
	@Inject
	public SelectNextUnitListener(final SelectNextUnitController selectNextUnitController, final GameModelController gameModelController){
		selectNextUnitController.addListener(event -> {
			final Model model = gameModelController.getModel();
		});
	}

}
