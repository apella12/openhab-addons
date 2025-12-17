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

import java.time.LocalDateTime;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.CommandBase;

/**
 * The {@link CCControlCommand } has the command structure to the
 * Midea Commercial AC
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCQueryCommand extends CommandBase {

    public CCQueryCommand() {
        super();
        // CC header WITHOUT clock byte
        data = new byte[] { (byte) 0xAA, // start
                0x00, // placeholder length
                (byte) 0xCC, // CC device type
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // filler
                (byte) 0x03 // command type (query)
                // payload will be appended next
                // clock will be appended after payload
        };
    }

    protected void buildPayload() {
        byte[] payload = new byte[22];
        payload[0] = 0x01; // fixed query marker
        data = concat(data, payload);
    }

    public byte[] toBytes() {
        buildPayload();
        // Append clock byte AFTER payload
        byte clock = (byte) LocalDateTime.now().getSecond();
        data = appendByte(data, clock);
        updateLength(); // include clock in the length
        compose(); // CRC8 + checksum
        return data;
    }

    private void updateLength() {
        // Length = header + payload + clock, excluding CRC/checksum
        data[1] = (byte) ((data.length + 1) & 0xFF);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static byte[] appendByte(byte[] src, byte b) {
        byte[] out = Arrays.copyOf(src, src.length + 1);
        out[src.length] = b;
        return out;
    }
}
