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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.devices.cc.CCCommandSet.ControlId;

/**
 * The {@link CCControlCommandTest} tests the methods in the CCControlCommand class
 * for correctness.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCControlCommandTest {
    @Test
    public void testLengthWithSingleControl() {
        // Power OFF (value = 0)
        CCControlCommand ccCommand = new CCControlCommand(Map.of(ControlId.POWER, 0));
        byte[] frame = ccCommand.toBytes();

        // Length byte should be 0x11 (17 decimal)
        assertEquals(0x11, frame[1] & 0xFF, "Length byte mismatch for single control");
    }

    @Test
    public void testLengthWithTwoControls() {
        // Power ON + Fan Speed 4
        CCControlCommand ccCommand = new CCControlCommand(Map.of(ControlId.POWER, 1, ControlId.FAN_SPEED, 4));
        byte[] frame = ccCommand.toBytes();

        // Length byte should be 0x16 (22 decimal)
        assertEquals(0x16, frame[1] & 0xFF, "Length byte mismatch for two controls");
    }
}
