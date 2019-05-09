/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.alienchain.core.Amount.Unit.KILO_ALX;
import static org.alienchain.core.Amount.Unit.MEGA_ALX;
import static org.alienchain.core.Amount.Unit.MICRO_ALX;
import static org.alienchain.core.Amount.Unit.MILLI_ALX;
import static org.alienchain.core.Amount.Unit.NANO_ALX;
import static org.alienchain.core.Amount.Unit.ALX;
import static org.alienchain.core.Amount.ZERO;
import static org.alienchain.core.Amount.neg;
import static org.alienchain.core.Amount.sub;
import static org.alienchain.core.Amount.sum;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.alienchain.core.Amount.Unit;

public class AmountTest {

  @Test
    public void testUnits() {
        assertEquals(ZERO, ZERO);
        assertEquals(NANO_ALX.of(1), NANO_ALX.of(1));
        assertEquals(NANO_ALX.of(1000), MICRO_ALX.of(1));
        assertEquals(MICRO_ALX.of(1000), MILLI_ALX.of(1));
        assertEquals(MILLI_ALX.of(1000), ALX.of(1));
        assertEquals(ALX.of(1000), KILO_ALX.of(1));
        assertEquals(KILO_ALX.of(1000), MEGA_ALX.of(1));
    }

  @Test
    public void testFromDecimal() {
        assertEquals(ZERO, ALX.fromDecimal(BigDecimal.ZERO));
        assertEquals(ALX.of(10), ALX.fromDecimal(new BigDecimal("10.000")));
        assertEquals(ALX.of(1000), KILO_ALX.fromDecimal(BigDecimal.ONE));
        assertEquals(MILLI_ALX.of(1), ALX.fromDecimal(new BigDecimal("0.001")));
    }

  @Test(expected = NoSuchElementException.class)
    public void testOfSymbol() {
        assertEquals(NANO_ALX, Unit.ofSymbol("nSEM"));
        assertEquals(MICRO_ALX, Unit.ofSymbol("μSEM"));
        assertEquals(MILLI_ALX, Unit.ofSymbol("mSEM"));
        assertEquals(ALX, Unit.ofSymbol("ALX"));
        assertEquals(KILO_ALX, Unit.ofSymbol("kSEM"));
        assertEquals(MEGA_ALX, Unit.ofSymbol("MSEM"));

        Unit.ofSymbol("???");
    }

  @Test
    public void testToDecimal() {
        assertEquals(new BigDecimal("0"), ALX.toDecimal(ZERO, 0));
        assertEquals(new BigDecimal("0.000"), ALX.toDecimal(ZERO, 3));

        Amount oneSem = ALX.of(1);
        assertEquals(new BigDecimal("1.000"), ALX.toDecimal(oneSem, 3));
        assertEquals(new BigDecimal("1000.000"), MILLI_ALX.toDecimal(oneSem, 3));
        assertEquals(new BigDecimal("0.001000"), KILO_ALX.toDecimal(oneSem, 6));
    }

  @Test
    public void testCompareTo() {
        assertEquals(ZERO.compareTo(ZERO), 0);
        assertEquals(NANO_ALX.of(1000).compareTo(MICRO_ALX.of(1)), 0);

        assertEquals(NANO_ALX.of(10).compareTo(NANO_ALX.of(10)), 0);
        assertEquals(NANO_ALX.of(5).compareTo(NANO_ALX.of(10)), -1);
        assertEquals(NANO_ALX.of(10).compareTo(NANO_ALX.of(5)), 1);
    }

  @Test
    public void testHashCode() {
        assertEquals(ZERO.hashCode(), ALX.of(0).hashCode());
        assertEquals(ALX.of(999).hashCode(), ALX.of(999).hashCode());
        assertEquals(ALX.of(1000).hashCode(), KILO_ALX.of(1).hashCode());
        assertNotEquals(ALX.of(1).hashCode(), KILO_ALX.of(1).hashCode());
    }

  @Test
    public void testGtLtEtc() {
        assertTrue(ALX.of(19).gt0());
        assertTrue(ALX.of(-9).lt0());
        assertFalse(ZERO.gt0());
        assertFalse(ZERO.lt0());

        assertTrue(ZERO.gte0());
        assertTrue(ZERO.lte0());
        assertFalse(ALX.of(-9).gte0());
        assertFalse(ALX.of(99).lte0());

        assertTrue(ALX.of(999).gt(MILLI_ALX.of(999)));
        assertTrue(ALX.of(999).gte(MILLI_ALX.of(999)));
        assertFalse(ALX.of(999).lt(MILLI_ALX.of(999)));
        assertFalse(ALX.of(999).lte(MILLI_ALX.of(999)));
    }

  @Test
    public void testMath() {
        assertEquals(sum(ALX.of(1000), KILO_ALX.of(1)), KILO_ALX.of(2));
        assertEquals(sub(ALX.of(1000), KILO_ALX.of(1)), ZERO);
        assertEquals(neg(ALX.of(1000)), KILO_ALX.of(-1));
        assertEquals(neg(ZERO), ZERO);
    }
}
