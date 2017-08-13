package org.microcol.gui.europe;

import javafx.scene.input.Dragboard;

//TODO use this class to extract objects and parameters from gragboard
public class DragObject {

	public static DragObject make(final Dragboard db){
		return new DragObject(db);
	}
	
	private DragObject(final Dragboard db){
		
	}
	
	
}
