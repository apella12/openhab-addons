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
package org.openhab.binding.mideaac.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Testing of the MideaAC Configuration
 *
 * @author Robert - Initial contribution
 */
@NonNullByDefault
public class MideaACConfigurationTest {

    MideaACConfiguration mideaACConfiguration = new MideaACConfiguration();

    @Test
    public void testValidConfigs() {
        String ip = "192.168.0.1";
        String port = "6444";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertTrue(mideaACConfiguration.isValid());
        assertFalse(mideaACConfiguration.isDiscoveryNeeded());
    }

    @Test
    public void testnonValidConfigs() {
        String ip = "192.168.0.1";
        String port = "";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertFalse(mideaACConfiguration.isValid());
        assertTrue(mideaACConfiguration.isDiscoveryNeeded());
    }

    @Test
    public void testBadIpConfigs() {
        String ip = "192.1680.1";
        String port = "6444";
        String deviceId = "1234567890";
        mideaACConfiguration.setIpAddress(ip);
        mideaACConfiguration.setIpPort(port);
        mideaACConfiguration.setDeviceId(deviceId);
        String ipTest = "";
        String portTest = "";
        String idTest = "";
        ipTest = mideaACConfiguration.getIpAddress();
        portTest = mideaACConfiguration.getIpPort();
        idTest = mideaACConfiguration.getDeviceId();
        assertEquals(ip, ipTest);
        assertEquals(port, portTest);
        assertEquals(deviceId, idTest);
        assertTrue(mideaACConfiguration.isValid());
        assertTrue(mideaACConfiguration.isDiscoveryNeeded());
    }
}
