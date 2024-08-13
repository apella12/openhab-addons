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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;

/**
 * The {@link CommandSetTest} compares example SET commands with the
 * expected results.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CommandSetTest {

    @Test
    public void setPowerStateTest() {
        boolean status = true;
        boolean status1 = true;
        CommandSet commandSet = new CommandSet();
        commandSet.setPowerState(status);
        assertEquals(status1, commandSet.getPowerState());
    }

    @Test
    public void testsetTargetTemperature() {
        CommandSet commandSet = new CommandSet();
        // Device is limited to 0.5 degree C increments. Check rounding too

        // Test case 1
        float targetTemperature1 = 25.4f;
        commandSet.setTargetTemperature(targetTemperature1);
        assertEquals(25.5f, commandSet.getTargetTemperature());

        // Test case 2
        float targetTemperature2 = 17.8f;
        commandSet.setTargetTemperature(targetTemperature2);
        assertEquals(18.0f, commandSet.getTargetTemperature());

        // Test case 3
        float targetTemperature3 = 21.26f;
        commandSet.setTargetTemperature(targetTemperature3);
        assertEquals(21.5f, commandSet.getTargetTemperature());

        // Test case 4
        float degreefahr = 72.0f;
        float targetTemperature4 = ((degreefahr + 40.0f) * (5.0f / 9.0f)) - 40.0f;
        commandSet.setTargetTemperature(targetTemperature4);
        assertEquals(22.0f, commandSet.getTargetTemperature());

        // Test case 5
        float degreefahr2 = 66.0f;
        float targetTemperature5 = ((degreefahr2 + 40.0f) * (5.0f / 9.0f)) - 40.0f;
        commandSet.setTargetTemperature(targetTemperature5);
        assertEquals(19.0f, commandSet.getTargetTemperature());
    }

    @Test
    public void testHandleSwingMode() {
        SwingMode mode = SwingMode.VERTICAL3;
        int mode1 = 60;
        CommandSet commandSet = new CommandSet();
        commandSet.setSwingMode(mode);
        assertEquals(mode1, commandSet.getSwingMode());
    }

    @Test
    public void testHandleFanSpeedCommand() {
        FanSpeed speed = FanSpeed.AUTO3;
        int speed1 = 102;
        CommandSet commandSet = new CommandSet();
        commandSet.setFanSpeed(speed);
        assertEquals(speed1, commandSet.getFanSpeed());
    }

    @Test
    public void testHandleOperationalMode() {
        OperationalMode mode = OperationalMode.COOL;
        int mode1 = 64;
        CommandSet commandSet = new CommandSet();
        commandSet.setOperationalMode(mode);
        assertEquals(mode1, commandSet.getOperationalMode());
    }
}
