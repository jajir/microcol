package org.microcol.gui.preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.PersistingTool;

class DeveloperModeTest {
    @TempDir
    Path directory;

    @Test
    void defaultsOffAndPersistsAcrossNewServiceInstancesWithoutLosingOtherSettings() {
        final PersistingTool files = mock(PersistingTool.class);
        when(files.getRootSaveDirectory()).thenReturn(directory.toFile());
        final SettingService service = new SettingService(new SettingDao(),
                files);
        final DeveloperMode mode = new DeveloperMode(service);
        assertFalse(mode.isEnabled());
        service.update(setting -> setting.setVolume(23));
        mode.setEnabled(true);
        assertTrue(
                new DeveloperMode(new SettingService(new SettingDao(), files))
                        .isEnabled());
        final Setting saved = service.load();
        final Setting copy = saved.copy();
        assertEquals(saved, copy);
        assertEquals(saved.hashCode(), copy.hashCode());
        copy.setDeveloperMode(false);
        assertNotEquals(saved, copy);
        assertTrue(saved.isDeveloperMode());
        assertEquals(23, service.load().getVolume());
        mode.setEnabled(false);
        assertFalse(
                new DeveloperMode(new SettingService(new SettingDao(), files))
                        .isEnabled());
    }

    @Test
    void publishesOnlySuccessfulChangesAndDoesNotRewriteUnchangedState() {
        final SettingService service = mock(SettingService.class);
        when(service.load()).thenReturn(new Setting());
        final DeveloperMode mode = new DeveloperMode(service);
        final AtomicInteger changes = new AtomicInteger();
        mode.enabledProperty().addListener(
                (property, before, after) -> changes.incrementAndGet());
        mode.setEnabled(false);
        verify(service, never()).update(any());
        doThrow(new IllegalStateException("storage unavailable")).when(service)
                .update(any());
        assertThrows(IllegalStateException.class, () -> mode.setEnabled(true));
        assertFalse(mode.isEnabled());
        assertEquals(0, changes.get());
        doNothing().when(service).update(any());
        mode.setEnabled(true);
        assertTrue(mode.isEnabled());
        assertEquals(1, changes.get());
    }
}
