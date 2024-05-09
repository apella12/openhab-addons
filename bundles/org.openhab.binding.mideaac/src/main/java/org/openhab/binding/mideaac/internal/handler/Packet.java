/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Packet class for Midea AC.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
public class Packet {
    private CommandBase command;
    private byte[] packet;
    private MideaACHandler mideaACHandler;
    // @SuppressWarnings("unused") //JO added and rimmed out
    // private String deviceId; //JO added and rimmed out
    // private static Logger logger = LoggerFactory.getLogger(Packet.class);

    @SuppressWarnings("deprecation")
    public Packet(CommandBase command, String deviceId, MideaACHandler mideaACHandler) {
        this.command = command;
        // this.deviceId = deviceId; //JO added and rimmed out
        this.mideaACHandler = mideaACHandler;

        packet = new byte[] {
                // 2 bytes - StaticHeader
                (byte) 0x5a, (byte) 0x5a,
                // 2 bytes - mMessageType
                (byte) 0x01, (byte) 0x11,
                // 2 bytes - PacketLenght
                (byte) 0x00, (byte) 0x00,
                // 2 bytes
                (byte) 0x20, (byte) 0x00,
                // 4 bytes - MessageId
                0x00, 0x00, 0x00, 0x00,
                // 8 bytes - Date&Time
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // 6 bytes - mDeviceID
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // 14 bytes
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        Date d = new Date();
        byte[] datetimeBytes = { (byte) (d.getYear() / 100), (byte) (d.getYear() % 100), (byte) d.getMonth(),
                (byte) d.getDate(), (byte) d.getHours(), (byte) d.getMinutes(), (byte) d.getSeconds(),
                (byte) d.getTime() }; // TOOD:
        // , 0x00
        // };(byte)
        // d.getTime()
        // set
        // milliseconds
        System.arraycopy(datetimeBytes, 0, packet, 12, 8);

        byte[] idBytes = new BigInteger(deviceId).toByteArray();
        ArrayUtils.reverse(idBytes);
        System.arraycopy(idBytes, 0, packet, 20, 6);
    }

    @SuppressWarnings("null")
    @Override
    public void finalize() {
        command.finalize();

        // Append the command data(48 bytes) to the packet
        byte[] cmdEncrypted = mideaACHandler.getSecurity().aes_encrypt(command.getBytes());
        packet = ArrayUtils.addAll(packet, Arrays.copyOf(cmdEncrypted, 48));

        // PacketLenght
        byte[] lenBytes = { (byte) (packet.length + 16), 0 };

        System.arraycopy(lenBytes, 0, packet, 4, 2);

        // Append a basic checksum data(16 bytes) to the packet
        packet = ArrayUtils.addAll(packet, mideaACHandler.getSecurity().encode32_data(packet));
    }

    public byte[] getBytes() {
        return packet;
    }
}
