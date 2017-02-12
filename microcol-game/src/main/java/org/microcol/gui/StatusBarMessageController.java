package org.microcol.gui;

/**
 * Register status bar message listeners. Allows to fire event that status bar message was changed.
 * 
 */
public interface StatusBarMessageController {

  /**
   * Add status message listener.
   * 
   * @param listener
   *          required listener instance
   */
  void addStatusMessageListener(StatusBarMessageListener listener);

  /**
   * Allows set status message for main window.
   * 
   * @param statusMessage
   *          required message text in main window.
   */
  void fireStatusMessageWasChangedEvent(String statusMessage);
}
