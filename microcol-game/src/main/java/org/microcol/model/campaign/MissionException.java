package org.microcol.model.campaign;

import org.microcol.gui.MicroColException;

/**
 * This exception is thrown when used perform move against mission instructions
 * and intentions.
 * <p>
 * For example it's thrown when user is instructed to find new land and user
 * instead of moving ship to left move to high seas and go to Europe.
 * </p>
 */
public final class MissionException extends MicroColException {

	private static final long serialVersionUID = -2975490267252949111L;

	public MissionException(final String message) {
		super(message);
	}
}
