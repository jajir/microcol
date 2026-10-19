package org.microcol.diagnostics;

/**
 * Fixed events that may enter the local diagnostic log. Callers pick one of
 * these labels; free-form text is never recorded.
 */
public enum DiagnosticEvent {
    STARTED, STARTUP_FAILED, EVENT_HANDLER_FAILED, LOAD_FAILED, SAVE_FAILED,
    EXIT_SAVE_FAILED, RECOVERY_SAVE_FAILED, TURN_PREPARATION_REJECTED
}
