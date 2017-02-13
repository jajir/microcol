package org.microcol.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;

/**
 * Allow to register key listener and fire key events.
 * 
 * @author Jan Jirout
 *
 */
public class KeyController {

	private final Logger logger = Logger.getLogger(KeyController.class);

	private final List<KeyListener> listeners = new ArrayList<KeyListener>();

	public void addKeyListener(final KeyListener listener) {
		Preconditions.checkNotNull(listener);
		listeners.add(listener);
	}

	public void fireKeyWasPressed(final KeyEvent keyEvent) {
		Preconditions.checkNotNull(keyEvent);
		logger.trace("firing event: " + keyEvent);
		listeners.forEach(listener -> {
			listener.keyPressed(keyEvent);
		});
	}
}
