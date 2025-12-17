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

/**
 * The {@link CCStringCommands handles the String Commands for commerial ACs
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCStringCommands {

    /**
     * Operational Modes for CC Device
     */
    public enum CCOperationalMode {
        FAN_ONLY(1),
        COOL(2),
        HEAT(3),
        AUTO(5),
        DRY(6);

        private final int value;

        private CCOperationalMode(int value) {
            this.value = value;
        }

        /**
         * Gets Operational Mode value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Provides Operational Mode Common name
         * 
         * @param id integer from byte response
         * @return type
         */
        public static CCOperationalMode fromId(int id) {
            for (CCOperationalMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return FAN_ONLY; // default to FAN for unknown values
        }
    }

    public enum CCFanSpeed {

        AUTO(8),
        SEVEN(7),
        SIX(6),
        FIVE(5),
        FOUR(4),
        THREE(3),
        TWO(2),
        ONE(1);

        private final int value;

        private CCFanSpeed(int value) {
            this.value = value;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @return type
         */
        public static CCFanSpeed fromId(int id) {
            for (CCFanSpeed type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return AUTO; // default to AUTO for unknown values
        }
    }

    public enum CCAuxHeatMode {
        AUTO(0),
        ON(1),
        OFF(2);

        private final int value;

        private CCAuxHeatMode(int value) {
            this.value = value;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @return type
         */
        public static CCAuxHeatMode fromId(int id) {
            for (CCAuxHeatMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return OFF; // default to OFF for unknown values
        }
    }

    public enum CCSwingAngle {
        CLOSE(0),
        POSITION_1(1),
        POSITION_2(2),
        POSITION_3(3),
        POSITION_4(4),
        POSITION_5(5),
        AUTO(6);

        private final int value;

        private CCSwingAngle(int value) {
            this.value = value;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @return type
         */
        public static CCSwingAngle fromId(int id) {
            for (CCSwingAngle type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return POSITION_3; // default for unknown values
        }
    }

    public enum CCPurifierMode {
        AUTO(0),
        ON(1),
        OFF(2);

        private final int value;

        private CCPurifierMode(int value) {
            this.value = value;
        }

        /**
         * Gets Fan Speed value
         * 
         * @return value
         */
        public int getId() {
            return value;
        }

        /**
         * Returns Fan Speed high, medium, low, etc
         * 
         * @param id integer from byte response
         * @return type
         */
        public static CCPurifierMode fromId(int id) {
            for (CCPurifierMode type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return OFF; // default to OFF for unknown values
        }
    }
}
