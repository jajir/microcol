package org.microcol.gui.event;

/**
 * Listener for change language event.
 */
public interface ChangeLanguageListener {

	/**
	 * When language is changed this method is called.
	 * 
	 * @param event
	 *            required event object containing new language
	 */
	void onChangeLanguage(ChangeLanguageEvent event);

}
