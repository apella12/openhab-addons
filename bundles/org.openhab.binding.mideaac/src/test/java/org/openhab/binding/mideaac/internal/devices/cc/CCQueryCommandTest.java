/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mideaac.internal.devices.cc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link CCQueryCommandTest} tests the methods in the CCQueryCommand class
 * for correctness.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCQueryCommandTest {

    @Test
    public void testQueryCommandFrame() {
        CCQueryCommand cmd = new CCQueryCommand();
        byte[] frame = cmd.toBytes();

        String actualHex = toHex(frame);
        // System.out.println("Frame (hex): " + actualHex);

        // Expected frame from Python example (without variable clock/CRC differences)
        String expectedPrefix = "aa22cc0000000000000301000000000000000000000000000000000000000000";

        // Check prefix matches exactly
        assertTrue(actualHex.startsWith(expectedPrefix), "Frame prefix matches expected Python output");

        // Header checks
        assertEquals("aa", actualHex.substring(0, 2), "Start byte");
        assertEquals("cc", actualHex.substring(4, 6), "Device type");
        assertEquals("03", actualHex.substring(18, 20), "Command type (query)");

        // Length field excludes CRC/checksum
        int declaredLength = frame[1] & 0xFF;
        assertEquals(frame.length - 1, declaredLength, "Declared length excludes CRC/checksum, matches protocol spec");

        // Payload: 22 bytes, first byte = 0x01
        int payloadOffset = 10; // after header
        assertEquals(0x01, frame[payloadOffset], "First payload byte is 0x01");

        // Clock byte right after payload
        byte clock = frame[payloadOffset + 22];
        assertTrue(clock >= 0 && clock <= 59, "Clock byte is a valid second");

        // CRC/checksum present at the end
        assertEquals(2, frame.length - (payloadOffset + 22 + 1), "Last 2 bytes are CRC + checksum");
    }

    private static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
