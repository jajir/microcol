package org.akropia;

import javafx.application.Application.Parameters;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Map;

class DemoStudioArgumentsTest {
    @Test
    void selectsStudioWithSingleNamedArgument() {
        Parameters parameters = mock(Parameters.class);
        when(parameters.getNamed())
                .thenReturn(Map.of("demo-studio", "src/main/resources/demos"));
        assertEquals("src/main/resources/demos",
                AkropiaApplication.demoDirectory(parameters));
    }

    @Test
    void preservesNormalStartupAndLegacyArguments() {
        assertNull(AkropiaApplication.demoDirectory(null));
        Parameters parameters = mock(Parameters.class);
        when(parameters.getNamed()).thenReturn(Map.of());
        when(parameters.getRaw()).thenReturn(List.of());
        assertNull(AkropiaApplication.demoDirectory(parameters));
        when(parameters.getRaw()).thenReturn(List.of("--demo-studio"));
        assertThrows(IllegalArgumentException.class,
                () -> AkropiaApplication.demoDirectory(parameters));
        when(parameters.getNamed())
                .thenReturn(Map.of("demo-directory", "scenes"));
        assertEquals("scenes", AkropiaApplication.demoDirectory(parameters));
        when(parameters.getNamed()).thenReturn(Map.of("demo-studio", " "));
        assertThrows(IllegalArgumentException.class,
                () -> AkropiaApplication.demoDirectory(parameters));
    }
}
