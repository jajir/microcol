package org.microcol.gui;

/**
 * Lister inform about status message change.
 * 
 */
public interface StatusBarMessageListener {

	/**
	 * When status message is changed this method is invoked.
	 * 
	 * @param statusMessage
	 *            required status message
	 */
	void onStatusMessageChange(String statusMessage);

}
