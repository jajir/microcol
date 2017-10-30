package org.microcol.gui.colonizopedia;

import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Colonizopedia extends AbstractDialog {

	public Colonizopedia(final Text text, final ViewUtil viewUtil) {
		super(viewUtil);
		getDialog().setTitle(text.get("colonizopedia.title"));
		final VBox root = new VBox();

		final ButtonsBar buttonBar = new ButtonsBar(text);
		buttonBar.getButtonOk().setOnAction(e -> {
			getDialog().close();
		});
		
		final HBox main = new HBox();	
		
		root.getChildren().addAll( main, buttonBar);
		init(root);
		getDialog().showAndWait();
	}

}
