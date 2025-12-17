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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.ac.ACCommandSet;

/**
 * This {@link ACCommandSet} class handles the allowed changes originating from
 * the items linked to the Midea device channels. Not all devices
 * support all commands.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCCommandSet {
    public enum ControlId {
        POWER(0x0000),
        TARGET_TEMPERATURE(0x0003),
        TEMPERATURE_UNIT(0x000C),
        TARGET_HUMIDITY(0x000F),
        MODE(0x0012),
        FAN_SPEED(0x0015),
        VERT_SWING_ANGLE(0x001C),
        HORZ_SWING_ANGLE(0x001E),
        WIND_SENSE(0x0020), // Untested
        ECO(0x0028),
        SILENT(0x002A),
        SLEEP(0x002C),
        SELF_CLEAN(0x002E), // Untested
        PURIFIER(0x003A),
        BEEP(0x003F),
        DISPLAY(0x0040),
        AUX_MODE(0x0043); // Untested

        private final int value;

        ControlId(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /** Decode raw control data into a convenient form. */
        public Object decode(byte[] data) {
            if (this == TARGET_TEMPERATURE) {
                return (data[0] / 2.0) - 40;
            } else {
                return data[0];
            }
        }

        /** Encode controls into raw form. */
        public byte[] encode(Object arg) {
            if (this == TARGET_TEMPERATURE) {
                int val = (int) (((Double) arg * 2) + 80);
                return new byte[] { (byte) val };
            } else {
                if (arg instanceof Integer) {
                    return new byte[] { ((Integer) arg).byteValue() };
                } else if (arg instanceof Boolean) {
                    return new byte[] { (byte) (((Boolean) arg) ? 1 : 0) };
                }
                throw new IllegalArgumentException("Unsupported type for encode");
            }
        }
    }
}
