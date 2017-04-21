package org.microcol.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contain size definition common for all dialogs.
 */
public class AbstractDialog extends JDialog {

	private final Logger logger = LoggerFactory.getLogger(AbstractDialog.class);

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	protected static final int BORDER = 10;

	protected static final int BORDER_BIG = 20;

	public AbstractDialog() {

	}

	public AbstractDialog(final JFrame parentFrame) {
		super(parentFrame);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action actionListener = new AbstractAction() {

			/**
			 * Default serialVersionUID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				logger.debug("ESC was pressed in dialog.");
				setVisible(false);
			}
		};
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", actionListener);
		return rootPane;
	}

}
