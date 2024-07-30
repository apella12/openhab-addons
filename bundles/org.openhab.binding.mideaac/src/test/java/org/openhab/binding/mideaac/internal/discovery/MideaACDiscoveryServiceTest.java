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
package org.openhab.binding.mideaac.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.Utils;

/**
 * Discovery service Test for Midea AC.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class MideaACDiscoveryServiceTest {

    byte[] data = HexFormat.of().parseHex(
            "837000C8200F00005A5A0111B8007A80000000006B0925121D071814C0110800008A0000000000000000018000000000AF55C8897BEA338348DA7FC0B3EF1F1C889CD57C06462D83069558B66AF14A2D66353F52BAECA68AEB4C3948517F276F72D8A3AD4652EFA55466D58975AEB8D948842E20FBDCA6339558C848ECE09211F62B1D8BB9E5C25DBA7BF8E0CC4C77944BDFB3E16E33D88768CC4C3D0658937D0BB19369BF0317B24D3A4DE9E6A13106AFFBBE80328AEA7426CD6BA2AD8439F72B4EE2436CC634040CB976A92A53BCD5");
    byte[] reply = HexFormat.of().parseHex(
            "F600A8C02C19000030303030303050303030303030305131423838433239353634334243303030300B6E65745F61635F343342430000870002000000000000000000AC00ACAC00000000B88C295643BC150023082122000300000000000000000000000000000000000000000000000000000000000000000000");
    String mSmartId = "", mSmartVersion = "", mSmartip = "", mSmartPort = "", mSmartSN = "", mSmartSSID = "",
            mSmartType = "";

    @Test
    public void testId() {
        if (Utils.bytesToHex(Arrays.copyOfRange(data, 8, 10)).equals("5A5A")) {
            data = Arrays.copyOfRange(data, 8, data.length - 16);
        }
        byte[] id = Arrays.copyOfRange(data, 20, 26);
        for (int i = 0; i < id.length / 2; i++) {
            byte temp = id[i];
            id[i] = id[id.length - i - 1];
            id[id.length - i - 1] = temp;
        }
        BigInteger bigId = new BigInteger(id);
        mSmartId = bigId.toString();
        assertEquals("-129742371548736", mSmartId);
    }

    @Test
    public void testIPAddress() {
        mSmartip = Byte.toUnsignedInt(reply[3]) + "." + Byte.toUnsignedInt(reply[2]) + "."
                + Byte.toUnsignedInt(reply[1]) + "." + Byte.toUnsignedInt(reply[0]);
        assertEquals("192.168.0.246", mSmartip);
    }

    @Test
    public void testPort() {
        mSmartPort = String.valueOf(bytes2port(Arrays.copyOfRange(reply, 4, 8)));
        assertEquals("6444", mSmartPort);
    }

    @Test
    public void testSN() {
        mSmartSN = new String(reply, 8, 40 - 8, StandardCharsets.UTF_8);
        assertEquals("000000P0000000Q1B88C295643BC0000", mSmartSN);
    }

    @Test
    public void testSSID() {
        mSmartSSID = new String(reply, 41, reply[40], StandardCharsets.UTF_8);
        assertEquals("net_ac_43BC", mSmartSSID);
    }

    @Test
    public void testType() {
        mSmartSSID = new String(reply, 41, reply[40], StandardCharsets.UTF_8);
        mSmartType = mSmartSSID.split("_")[1];
        assertEquals("ac", mSmartType);
    }

    private int bytes2port(byte[] bytes) {
        int b = 0;
        int i = 0;
        while (b < 4) {
            int b1;
            if (b < bytes.length) {
                b1 = bytes[b] & 0xFF;
            } else {
                b1 = 0;
            }

            i |= b1 << b * 8;
            b += 1;
        }
        return i;
    }
}
