/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CapabilityTest {

  @Test
    public void testIsSupported() {
        assertFalse(CapabilitySet.emptySet().isSupported(Capability.ALIENCHAIN));
        assertFalse(CapabilitySet.of("ALIENCHAIN").isSupported(Capability.LIGHT));
        assertTrue(CapabilitySet.of("ALIENCHAIN").isSupported(Capability.ALIENCHAIN));
        assertTrue(CapabilitySet.of(Capability.ALIENCHAIN).isSupported(Capability.ALIENCHAIN));
        assertEquals(CapabilitySet.of(Capability.ALIENCHAIN), CapabilitySet.of(Capability.ALIENCHAIN));
    }
}
