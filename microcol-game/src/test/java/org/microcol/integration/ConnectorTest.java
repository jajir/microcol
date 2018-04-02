package org.microcol.integration;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.gui.image.Connector;

public class ConnectorTest {

    @Test
    public void test_rotateRight() throws Exception {
        assertEquals('6', Connector.of('3').rotateRight().get());
        assertEquals('4', Connector.of('1').rotateRight().get());
        assertEquals('1', Connector.of('a').rotateRight().get());
        assertEquals('5', Connector.of('2').rotateRight().get());
        assertEquals('2', Connector.of('b').rotateRight().get());
        assertEquals('3', Connector.of('0').rotateRight().get());
        assertEquals('a', Connector.of('7').rotateRight().get());
        assertEquals('b', Connector.of('8').rotateRight().get());
    }

}
