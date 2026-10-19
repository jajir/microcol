package org.microcol.diagnostics;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

class DiagnosticSubscriberExceptionHandlerTest {

    @TempDir
    Path directory;

    @Test
    void failedSubscriberIsRecordedWithoutItsMessage() throws Exception {
        final DiagnosticLog log = new DiagnosticLog(directory, "test-build");
        final EventBus eventBus = new EventBus(
                new DiagnosticSubscriberExceptionHandler(log));
        eventBus.register(new FailingSubscriber());

        eventBus.post("player text Jan");

        final String record = Files.readString(log.getActiveFile());
        assertTrue(record.contains("event=EVENT_HANDLER_FAILED"));
        assertTrue(record.contains("cause=java.lang.IllegalStateException"));
        assertFalse(record.contains("Jan"));
    }

    public static final class FailingSubscriber {

        @Subscribe
        public void onEvent(final String event) {
            throw new IllegalStateException("subscriber failed for " + event);
        }

    }

}
