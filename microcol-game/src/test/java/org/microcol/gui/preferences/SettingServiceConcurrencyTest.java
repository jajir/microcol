package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.PersistingTool;

class SettingServiceConcurrencyTest {

    @TempDir
    Path directory;

    private final ExecutorService threads = Executors.newFixedThreadPool(2);

    @AfterEach
    void stopThreads() throws InterruptedException {
        threads.shutdownNow();
        assertTrue(threads.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void readingWaitsUntilTheTruncatedFileHasBeenWritten() throws Exception {
        final SettingDao dao = spy(new SettingDao());
        final SettingService service = service(dao);
        final Setting changed = service.load();
        changed.setVolume(23);
        final CountDownLatch truncated = new CountDownLatch(1);
        final CountDownLatch finishWrite = new CountDownLatch(1);
        final AtomicBoolean firstWrite = new AtomicBoolean(true);
        doAnswer(invocation -> {
            if (firstWrite.getAndSet(false)) {
                final File file = invocation.getArgument(1);
                Files.write(file.toPath(), new byte[0]);
                truncated.countDown();
                assertTrue(finishWrite.await(5, TimeUnit.SECONDS));
            }
            return invocation.callRealMethod();
        }).when(dao).saveToFile(any(Setting.class), any(File.class));

        final Future<?> writer = threads.submit(() -> service.save(changed));
        try {
            assertTrue(truncated.await(5, TimeUnit.SECONDS));
            final CountDownLatch reading = new CountDownLatch(1);
            final Future<Setting> reader = threads.submit(() -> {
                reading.countDown();
                return service.load();
            });
            assertTrue(reading.await(5, TimeUnit.SECONDS));

            // Painting must wait, not replace a temporarily empty file with
            // defaults.
            assertThrows(TimeoutException.class,
                    () -> reader.get(200, TimeUnit.MILLISECONDS));
            finishWrite.countDown();
            writer.get(5, TimeUnit.SECONDS);
            assertEquals(23, reader.get(5, TimeUnit.SECONDS).getVolume());
            assertEquals(23, service.load().getVolume());
        } finally {
            finishWrite.countDown();
        }
    }

    @Test
    void concurrentUpdatesPreserveBothChangedFields() throws Exception {
        final SettingService service = service(new SettingDao());
        service.load();
        final CountDownLatch firstLoaded = new CountDownLatch(1);
        final CountDownLatch finishFirstUpdate = new CountDownLatch(1);
        final Future<?> first = threads.submit(() -> service.update(setting -> {
            setting.setVolume(23);
            firstLoaded.countDown();
            try {
                assertTrue(finishFirstUpdate.await(5, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AssertionError(ex);
            }
        }));
        try {
            assertTrue(firstLoaded.await(5, TimeUnit.SECONDS));
            final CountDownLatch secondStarted = new CountDownLatch(1);
            final Future<?> second = threads.submit(() -> {
                secondStarted.countDown();
                service.update(setting -> setting.setShowGrid(true));
            });
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS));

            // The second update must read the first update's completed state.
            assertThrows(TimeoutException.class,
                    () -> second.get(200, TimeUnit.MILLISECONDS));
            finishFirstUpdate.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
            final Setting saved = service.load();
            assertEquals(23, saved.getVolume());
            assertTrue(saved.isShowGrid());
        } finally {
            finishFirstUpdate.countDown();
        }
    }

    private SettingService service(final SettingDao dao) {
        final PersistingTool files = mock(PersistingTool.class);
        when(files.getRootSaveDirectory()).thenReturn(directory.toFile());
        return new SettingService(dao, files);
    }
}
