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

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.CommandBase;
import org.openhab.binding.mideaac.internal.devices.cc.CCCommandSet.ControlId;

/**
 * The {@link CCControlCommand } has the command structure to the
 * Midea Commercial AC
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCControlCommand extends CommandBase {
    private final Map<ControlId, Object> controls;

    public CCControlCommand(Map<ControlId, Object> controls) {
        super();
        this.controls = controls;

        // CC header WITHOUT clock byte
        data = new byte[] { (byte) 0xAA, // start
                0x00, // placeholder length
                (byte) 0xCC, // CC device type
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // filler
                (byte) 0x02 // command type (setting)
                // payload will be appended next
                // clock will be appended after payload
        };
    }

    protected void buildPayload() {
        ByteBuffer buffer = ByteBuffer.allocate(256);

        for (Map.Entry<ControlId, Object> e : controls.entrySet()) {
            ControlId cid = e.getKey();
            Object val = Objects.requireNonNull(e.getValue(), "Null value for " + cid);

            buffer.putShort((short) cid.getValue());
            byte[] encoded = cid.encode(val);
            buffer.put((byte) encoded.length);
            buffer.put(encoded);
            buffer.put((byte) 0xFF);
        }

        byte[] payload = new byte[buffer.position()];
        buffer.flip();
        buffer.get(payload);

        // Append payload
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
