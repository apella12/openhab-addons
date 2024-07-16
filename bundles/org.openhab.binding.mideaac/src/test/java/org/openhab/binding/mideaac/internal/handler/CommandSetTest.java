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
 * Test Set Commands
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
    public void setTargetTemperatureTest() {
        float targetTemperature = 25.5f;
        CommandSet commandSet = new CommandSet();
        commandSet.setTargetTemperature(targetTemperature);
        assertEquals(targetTemperature, commandSet.getTargetTemperature());
    }

    @Test
    public void setTargetTemperatureTest2() {
        float targetTemperature = 17.0f;
        CommandSet commandSet = new CommandSet();
        commandSet.setTargetTemperature(targetTemperature);
        assertEquals(targetTemperature, commandSet.getTargetTemperature());
    }

    @Test
    public void setTargetTemperatureTest3() {
        float targetTemperature = 30.0f;
        CommandSet commandSet = new CommandSet();
        commandSet.setTargetTemperature(targetTemperature);
        assertEquals(targetTemperature, commandSet.getTargetTemperature());
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
