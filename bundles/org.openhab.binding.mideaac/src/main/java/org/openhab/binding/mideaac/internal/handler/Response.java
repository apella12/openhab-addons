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
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Response from a device.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class Response {
    byte[] data;
    // set @empty to match the return from an empty byte
    float empty = (float) -22.5;
    private Logger logger = LoggerFactory.getLogger(Response.class);

    private final int version;
    String responseType;
    byte bodyType;

    private int getVersion() {
        return version;
    }

    public Response(byte[] data, int version, String responseType, byte bodyType) {
        this.data = data;
        this.version = version;
        this.bodyType = bodyType;
        this.responseType = responseType;

        logger.debug("PowerState: {}", getPowerState());
        logger.trace("ImodeResume: {}", getImmodeResume());
        logger.trace("TimerMode: {}", getTimerMode());
        logger.trace("Prompt Tone: {}", getPromptTone());
        logger.trace("ApplianceError: {}", getApplianceError());
        logger.debug("TargetTemperature: {}", getTargetTemperature());
        logger.debug("OperationalMode: {}", getOperationalMode());
        logger.debug("FanSpeed: {}", getFanSpeed());
        logger.trace("OnTimer: {}", getOnTimer());
        logger.trace("OffTimer: {}", getOffTimer());
        logger.debug("SwingMode: {}", getSwingMode());
        logger.trace("CozySleep: {}", getCozySleep());
        logger.trace("Power Saving: {}", getSave());
        logger.trace("LowFrequencyFan: {}", getLowFrequencyFan());
        logger.trace("SuperFan: {}", getSuperFan());
        logger.trace("FeelOwn: {}", getFeelOwn());
        logger.trace("ChildSleepMode: {}", getChildSleepMode());
        logger.trace("ExchangeAir: {}", getExchangeAir());
        logger.trace("DryClean: {}", getDryClean());
        logger.trace("AuxHeat: {}", getAuxHeat());
        logger.trace("EcoMode: {}", getEcoMode());
        logger.trace("CleanUp: {}", getCleanUp());
        logger.trace("TempUnit: {}", getTempUnit());
        logger.debug("SleepFunction: {}", getSleepFunction());
        logger.debug("TurboMode: {}", getTurboMode());
        logger.trace("Fahrenheit: {}", getFahrenheit());
        logger.trace("CatchCold: {}", getCatchCold());
        logger.trace("NightLight: {}", getNightLight());
        logger.trace("PeakElec: {}", getPeakElec());
        logger.trace("NaturalFan: {}", getNaturalFan());
        logger.debug("IndoorTemperature: {}", getIndoorTemperature());
        logger.debug("OutdoorTemperature: {}", getOutdoorTemperature());
        logger.debug("Humidity: {}", getHumidity());

        // Log Response and Body Type for V3. V2 set at "" and 0x00
        if (version == 3) {
            logger.trace("Response and Body Type: {}, {}", responseType, bodyType);
            // https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea/devices/ac/message.py#L418
            if ("notify2".equals(responseType) && bodyType == -95) { // 0xA0 = -95
                logger.trace("Response Handler: XA0Message");
            } else if ("notify1".equals(responseType) && bodyType == -91) { // 0xA1 = -91
                logger.trace("Response Handler: XA1Message");
            } else if (("notify2".equals(responseType) || "set".equals(responseType) || "query".equals(responseType))
                    && (bodyType == 0xB0 || bodyType == 0xB1 || bodyType == 0xB5)) {
                logger.trace("Response Handler: XBXMessage");
            } else if (("set".equals(responseType) || "query".equals(responseType)) && bodyType == -64) { // 0xC0 = -64
                logger.trace("Response Handler: XCOMessage");
            } else if ("query".equals(responseType) && bodyType == 0xC1) {
                logger.trace("Response Handler: XC1Message");
            } else {
                logger.trace("Response Handler: _general_");
            }
        }
    }

    public boolean getPowerState() {
        return (data[0x01] & 0x1) > 0;
    }

    public boolean getImmodeResume() {
        return (data[0x01] & 0x4) > 0;
    }

    public boolean getTimerMode() {
        return (data[0x01] & 0x10) > 0;
    }

    public boolean getPromptTone() {
        return (data[0x01] & 0x40) > 0;
    }

    public boolean getApplianceError() {
        return (data[0x01] & 0x80) > 0;
    }

    public float getTargetTemperature() {
        return (data[0x02] & 0xf) + 16.0f + (((data[0x02] & 0x10) > 0) ? 0.5f : 0.0f);
    }

    public OperationalMode getOperationalMode() {
        return OperationalMode.fromId((data[0x02] & 0xe0) >> 5);
    }

    public FanSpeed getFanSpeed() {
        return FanSpeed.fromId(data[0x03] & 0x7f, getVersion());
    }

    public Timer getOnTimer() {
        return new Timer((data[0x04] & 0x80) > 0, ((data[0x04] & (byte) 0x7c) >> 2),
                (data[0x04] & 0x3) * 15 | ((data[0x06] & (byte) 0xf0) >> 4));
    }

    public Timer getOffTimer() {
        return new Timer((data[0x05] & 0x80) > 0, ((data[0x05] & (byte) 0x7c) >> 2),
                (data[0x05] & 0x3) * 15 | (data[0x06] & (byte) 0xf));
    }

    public SwingMode getSwingMode() {
        return SwingMode.fromId(data[0x07] & 0x3f, getVersion());
    }

    public int getCozySleep() {
        return data[0x08] & (byte) 0x03;
    }

    public boolean getSave() {
        return (data[0x08] & (byte) 0x08) != 0;
    }

    public boolean getLowFrequencyFan() {
        return (data[0x08] & (byte) 0x10) != 0;
    }

    public boolean getSuperFan() {
        return (data[0x08] & (byte) 0x20) != 0;
    }

    public boolean getFeelOwn() {
        return (data[0x08] & (byte) 0x80) != 0;
    }

    public boolean getChildSleepMode() {
        return (data[0x09] & (byte) 0x01) != 0;
    }

    public boolean getExchangeAir() {
        return (data[0x09] & (byte) 0x02) != 0;
    }

    public boolean getDryClean() {
        return (data[0x09] & (byte) 0x04) != 0;
    }

    public boolean getAuxHeat() {
        return (data[0x09] & (byte) 0x08) != 0;
    }

    public boolean getEcoMode() {
        return (data[0x09] & (byte) 0x10) != 0;
    }

    public boolean getCleanUp() {
        return (data[0x09] & (byte) 0x20) != 0;
    }

    public boolean getTempUnit() {
        return (data[0x09] & (byte) 0x80) != 0;
    }

    public boolean getSleepFunction() {
        return (data[0x0a] & (byte) 0x01) != 0;
    }

    public boolean getTurboMode() {
        return (data[0x0a] & (byte) 0x02) != 0;
    }

    public boolean getFahrenheit() {
        return (data[0x0a] & (byte) 0x04) != 0;
    }

    public boolean getCatchCold() {
        return (data[0x0a] & (byte) 0x08) != 0;
    }

    public boolean getNightLight() {
        return (data[0x0a] & (byte) 0x10) != 0;
    }

    public boolean getPeakElec() {
        return (data[0x0a] & (byte) 0x20) != 0;
    }

    public boolean getNaturalFan() {
        return (data[0x0a] & (byte) 0x40) != 0;
    }

    public Float getIndoorTemperature() {
        // My AC just uses byte[11] for 0.5 degrees Validated with NetHome App reading
        // Changed int to float to handle, left byte[15] as used by others
        double indoorTempInteger;
        double indoorTempDecimal;

        if (data[0] == (byte) 0xc0) {
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0) < -19) {
                return (float) -19;
            }
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0) > 50) {
                return (float) 50;
            } else {
                indoorTempInteger = (float) ((data[11] - 50f) / 2f);
            }

            indoorTempDecimal = (float) (((data[15] & 0x0F)) * 0.1f);

            if (data[11] > 49) {
                return (float) (indoorTempInteger + indoorTempDecimal);
            } else {
                return (float) (indoorTempInteger - indoorTempDecimal);
            }
        }
        // Not observed or tested
        if (data[0] == (byte) 0xa0 || data[0] == (byte) 0xa1) {
            if (data[0] == (byte) 0xa0) {
                if ((data[1] >> 2) - 4 == 0) {
                    indoorTempInteger = -1;
                } else {
                    indoorTempInteger = (data[1] >> 2) + 12;
                }

                if (((data[1] >> 1) & 0x01) == 1) {
                    indoorTempDecimal = 0.5f;
                } else {
                    indoorTempDecimal = 0;
                }
            }
            if (data[0] == (byte) 0xa1) {
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2.0f) < -19) {
                    return (float) -19;
                }
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2.0f) > 50) {
                    return (float) 50;
                } else {
                    indoorTempInteger = (float) (Byte.toUnsignedInt(data[13]) - 50) / 2;
                }
                indoorTempDecimal = (data[18] & 0x0f) * 0.1f;

                if (Byte.toUnsignedInt(data[13]) > 49) {
                    return (float) (indoorTempInteger + indoorTempDecimal);
                } else {
                    return (float) (indoorTempInteger - indoorTempDecimal);
                }
            }
        }
        return empty;
    }

    public Float getOutdoorTemperature() {
        // My AC just uses byte[12] for 0.5 degrees; Validated with NetHome App reading
        // Changed int to float to handle, left byte[15] as used by others
        // Assumed to be used for all response and body types
        if (data[12] != 0xFF) {
            double tempInteger = (float) ((data[12] - 50f) / 2f);
            double tempDecimal = ((data[15] & 0xf0) >> 4) * 0.1f;
            if (data[12] > 49) {
                return (float) (tempInteger + tempDecimal);
            } else {
                return (float) (tempInteger - tempDecimal);
            }
        }
        return empty;
    }

    // Need to validate what byte has humidity (if any)
    public int getHumidity() {
        return (data[19] & (byte) 0x7f);
    }
}
