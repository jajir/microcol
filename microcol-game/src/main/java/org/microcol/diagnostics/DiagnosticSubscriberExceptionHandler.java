package org.microcol.diagnostics;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Sends failures of event subscribers to the local diagnostic log instead of
 * Guava's default reporting through <code>java.util.logging</code>.
 */
public final class DiagnosticSubscriberExceptionHandler
        implements SubscriberExceptionHandler {

    private final DiagnosticLog diagnosticLog;

    public DiagnosticSubscriberExceptionHandler(
            final DiagnosticLog diagnosticLog) {
        this.diagnosticLog = Preconditions.checkNotNull(diagnosticLog);
    }

    @Override
    public void handleException(final Throwable exception,
            final SubscriberExceptionContext context) {
        diagnosticLog.record(DiagnosticEvent.EVENT_HANDLER_FAILED, exception);
    }

}
