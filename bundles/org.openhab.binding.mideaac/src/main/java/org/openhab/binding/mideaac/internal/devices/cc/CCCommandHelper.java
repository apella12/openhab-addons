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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.cc.CCCommandSet.ControlId;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The {@link CCCommandHelper} is a static class that is able to translate {@link Command} to {@link CCCommandSet}
 * for Midea Commercial AC devices.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCCommandHelper {

    public static CCControlCommand handlePower(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.POWER, on ? 1 : 0));
    }

    public static CCControlCommand handleTargetTemperature(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            double temp = ((DecimalType) command).doubleValue();
            return new CCControlCommand(Map.of(ControlId.TARGET_TEMPERATURE, temp));
        }
        throw new IllegalArgumentException("Unsupported command type for target temperature");
    }

    public static CCControlCommand handleCCOperationalMode(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            return new CCControlCommand(Map.of(ControlId.MODE, mode));
        }
        throw new IllegalArgumentException("Unsupported command type for mode");
    }

    public static CCControlCommand handleFanSpeed(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            int speed = ((DecimalType) command).intValue();
            return new CCControlCommand(Map.of(ControlId.FAN_SPEED, speed));
        }
        throw new IllegalArgumentException("Unsupported command type for fan speed");
    }

    public static CCControlCommand handleVerticalAngle(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            int angle = ((DecimalType) command).intValue();
            return new CCControlCommand(Map.of(ControlId.VERT_SWING_ANGLE, angle));
        }
        throw new IllegalArgumentException("Unsupported command type for vertical angle");
    }

    public static CCControlCommand handleHorizontalAngle(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            int angle = ((DecimalType) command).intValue();
            return new CCControlCommand(Map.of(ControlId.HORZ_SWING_ANGLE, angle));
        }
        throw new IllegalArgumentException("Unsupported command type for horizontal angle");
    }

    public static CCControlCommand handlePurifier(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.PURIFIER, on ? 1 : 0));
    }

    public static CCControlCommand handleAuxHeat(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.AUX_MODE, on ? 1 : 0));
    }

    public static CCControlCommand handleEco(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.ECO, on ? 1 : 0));
    }

    public static CCControlCommand handleSilent(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.SILENT, on ? 1 : 0));
    }

    public static CCControlCommand handleSleep(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.SLEEP, on ? 1 : 0));
    }

    public static CCControlCommand handleTargetHumidity(Command command, CCResponse lastResponse) {
        if (command instanceof DecimalType) {
            int humidity = ((DecimalType) command).intValue();
            return new CCControlCommand(Map.of(ControlId.TARGET_HUMIDITY, humidity));
        }
        throw new IllegalArgumentException("Unsupported command type for target humidity");
    }

    public static CCControlCommand handleDisplay(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.DISPLAY, on ? 1 : 0));
    }

    public static CCControlCommand handleBeep(Command command, CCResponse lastResponse) {
        boolean on = (command instanceof OnOffType) && command == OnOffType.ON;
        return new CCControlCommand(Map.of(ControlId.BEEP, on ? 1 : 0));
    }
}
