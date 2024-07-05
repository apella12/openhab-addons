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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command changing a Midea AC.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class CommandSet extends CommandBase {

    public CommandSet() {
        data[0x01] = (byte) 0x23;
        data[0x09] = (byte) 0x02;
        // Set up Mode
        data[0x0a] = (byte) 0x40;

        byte[] extra = { 0x00, 0x00, 0x00 };
        byte[] newData = new byte[data.length + 3];
        System.arraycopy(data, 0, newData, 0, data.length);
        newData[data.length] = extra[0];
        newData[data.length + 1] = extra[1];
        newData[data.length + 2] = extra[2];
        data = newData;
    }

    public static CommandSet fromResponse(Response response) {
        CommandSet commandSet = new CommandSet();

        commandSet.setPowerState(response.getPowerState());
        commandSet.setTargetTemperature(response.getTargetTemperature());
        commandSet.setOperationalMode(response.getOperationalMode());
        commandSet.setFanSpeed(response.getFanSpeed());
        commandSet.setFahrenheit(response.getFahrenheit());
        commandSet.setTurboMode(response.getTurboMode());
        commandSet.setSwingMode(response.getSwingMode());
        commandSet.setScreenDisplay(response.getNightLight());
        commandSet.setEcoMode(response.getEcoMode());
        commandSet.setSleepMode(response.getSleepFunction());

        return commandSet;
    }

    public void setPromptTone(boolean feedbackEnabled) {
        if (!feedbackEnabled) {
            data[0x0b] &= ~(byte) 0x40; // Clear the audible bit
        } else {
            data[0x0b] |= (byte) 0x40;
        }
    }

    public void setPowerState(boolean state) {
        if (!state) {
            data[0x0b] &= ~0x01;// Clear the power bit
        } else {
            data[0x0b] |= 0x01;
        }
    }

    public void setOperationalMode(OperationalMode mode) {
        data[0x0c] &= ~(byte) 0xe0; // Clear the mode bit
        data[0x0c] |= ((byte) mode.getId() << 5) & (byte) 0xe0;
    }

    public void setTargetTemperature(float temperature) {
        // Clear the temperature bits.
        data[0x0c] &= ~0x0f;
        // Clear the temperature bits, except the 0.5 bit, which will be set properly in all cases
        data[0x0c] |= (int) (Math.round(temperature * 2) / 2) & 0xf;
        // set the +0.5 bit
        setTemperatureDot5((Math.round(temperature * 2)) % 2 != 0);
    }

    public void setFanSpeed(FanSpeed speed) {
        setFanSpeed(speed.getId());
    }

    public void setFanSpeed(int speed) {
        data[0x0d] = (byte) speed;
    }

    public void setEcoMode(boolean ecoModeEnabled) {
        if (!ecoModeEnabled) {
            data[0x13] &= ~0x10;// Clear the Eco bit
        } else {
            data[0x13] |= 0x10;
        }
    }

    public void setSwingMode(SwingMode mode) {
        data[0x11] &= ~0x3f; // Clear the mode bits
        data[0x11] |= mode.getId() & 0x3f;
    }

    public void setSleepMode(boolean sleepModeEnabled) {
        if (sleepModeEnabled) {
            data[0x14] |= 0x01;
        } else {
            data[0x14] &= (~0x01);
        }
    }

    public void setTurboMode(boolean turboModeEnabled) {
        if (turboModeEnabled) {
            data[0x14] |= 0x02;
        } else {
            data[0x14] &= (~0x02);
        }
    }

    public void setFahrenheit(boolean fahrenheitEnabled) {
        // set the display to Fahrenheit from Celsius
        if (fahrenheitEnabled) {
            data[0x14] |= 0x04;
        } else {
            data[0x14] &= (~0x04);
        }
    }

    public void setScreenDisplay(boolean screenDisplayEnabled) {
        // the LED lights on the AC. these display temperature and are often too bright during nights
        if (screenDisplayEnabled) {
            data[0x14] |= 0x10;
        } else {
            data[0x14] &= (~0x10);
        }
    }

    private void setTemperatureDot5(boolean temperatureDot5Enabled) {
        // add 0.5C to the temperature value. not intended to be called directly. target_temperature setter calls this
        // if needed
        if (temperatureDot5Enabled) {
            data[0x0c] |= 0x10;
        } else {
            data[0x0c] &= (~0x10);
        }
    }
}
