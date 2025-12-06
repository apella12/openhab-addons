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
package org.openhab.binding.mideaac.internal.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.commands.CommandBase.A1FanSpeed;
import org.openhab.binding.mideaac.internal.commands.CommandBase.A1OperationalMode;

/**
 * The {@link A1CommandSetTest} tests the methods in the A1CommandSet class
 * for correctness.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1CommandSetTest {

    /**
     * Dehumidifier Swing Mode test
     * 
     */
    @Test
    public void testHandleA1SwingMode() {
        boolean mode1 = true;
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.setA1SwingMode(mode1);
        assertEquals(mode1, commandSet.getA1SwingMode());
    }

    /**
     * Dehumidifier Operational mode test
     */
    @Test
    public void testHandleA1OperationalMode() {
        A1OperationalMode mode = A1OperationalMode.AUTO;
        int mode1 = 3;
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.setA1OperationalMode(mode);
        assertEquals(mode1, commandSet.getA1OperationalMode());
    }

    /**
     * Dehumidifier Fan Speed test
     */
    @Test
    public void testHandleA1FanSpeed() {
        A1FanSpeed speed = A1FanSpeed.HIGH;
        int speed1 = 80;
        A1CommandSet commandSet = new A1CommandSet();
        commandSet.setA1FanSpeed(speed);
        assertEquals(speed1, commandSet.getA1FanSpeed());
    }
}
