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
public class Response {
    byte[] data;
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

        if (version == 3) {
            logger.trace("Response and Body Type: {}, {}", responseType, bodyType);
            // https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea/devices/ac/message.py#L418
            if (responseType.equals("notify2") && bodyType == -95) { // 0xA0 = -95
                logger.trace("Response Handler: XA0Message");
            } else if (responseType.equals("notify1") && bodyType == -91) { // 0xA1 = -91
                logger.trace("Response Handler: XA1Message");
            } else if ((responseType.equals("notify2") || responseType.equals("set") || responseType.equals("query"))
                    && (bodyType == 0xB0 || bodyType == 0xB1 || bodyType == 0xB5)) {
                logger.trace("Response Handler: XBXMessage");
            } else if ((responseType.equals("set") || responseType.equals("query")) && bodyType == -64) { // 0xC0 = -64
                                                                                                          // ??
                logger.trace("Response Handler: XCOMessage");
            } else if (responseType.equals("query") && bodyType == 0xC1) {
                logger.trace("Response Handler: XC1Message");
            } else {
                logger.trace("Response Handler: _general_");
            }
            {

                logger.trace("PowerState: {}", getPowerState());
                logger.trace("ImodeResume: {}", getImmodeResume());
                logger.trace("TimerMode: {}", getTimerMode());
                logger.trace("ApplianceError: {}", getApplianceError());
                logger.trace("TargetTemperature: {}", getTargetTemperature());
                logger.trace("OperationalMode: {}", getOperationalMode());
                logger.trace("FanSpeed: {}", getFanSpeed());
                logger.trace("OnTimer: {}", getOnTimer());
                logger.trace("OffTimer: {}", getOffTimer());
                logger.trace("SwingMode: {}", getSwingMode());
                logger.trace("CozySleep: {}", getCozySleep());
                logger.trace("Save: {}", getSave());
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
                logger.trace("SleepFunction: {}", getSleepFunction());
                logger.trace("TurboMode: {}", getTurboMode());
                logger.trace("Fahrenheit: {}", getFahrenheit());
                logger.trace("CatchCold: {}", getCatchCold());
                logger.trace("NightLight: {}", getNightLight());
                logger.trace("PeakElec: {}", getPeakElec());
                logger.trace("NaturalFan: {}", getNaturalFan());
                logger.trace("IndoorTemperature: {}", getIndoorTemperature());
                logger.trace("OutdoorTemperature: {}", getOutdoorTemperature());
                logger.trace("Humidity: {}", getHumidity());
            }

        } else {
            logger.trace("PowerState: {}", getPowerState());
            logger.trace("ImodeResume: {}", getImmodeResume());
            logger.trace("TimerMode: {}", getTimerMode());
            logger.trace("ApplianceError: {}", getApplianceError());
            logger.trace("TargetTemperature: {}", getTargetTemperature());
            logger.trace("OperationalMode: {}", getOperationalMode());
            logger.trace("FanSpeed: {}", getFanSpeed());
            logger.trace("OnTimer: {}", getOnTimer());
            logger.trace("OffTimer: {}", getOffTimer());
            logger.trace("SwingMode: {}", getSwingMode());
            logger.trace("CozySleep: {}", getCozySleep());
            logger.trace("Save: {}", getSave());
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
            logger.trace("SleepFunction: {}", getSleepFunction());
            logger.trace("TurboMode: {}", getTurboMode());
            logger.trace("Fahrenheit: {}", getFahrenheit());
            logger.trace("CatchCold: {}", getCatchCold());
            logger.trace("NightLight: {}", getNightLight());
            logger.trace("PeakElec: {}", getPeakElec());
            logger.trace("NaturalFan: {}", getNaturalFan());
            logger.trace("IndoorTemperature: {}", getIndoorTemperature());
            logger.trace("OutdoorTemperature: {}", getOutdoorTemperature());
            logger.trace("Humidity: {}", getHumidity());
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
        int on_timer_value = data[0x04];
        int on_timer_minutes = data[0x06];
        return new Timer(((on_timer_value & (byte) 0x80) >> 7) > 0, (on_timer_value & (byte) 0x7c) >> 2,
                (on_timer_value & 0x3) | ((on_timer_minutes & (byte) 0xf0) >> 4));
    }

    public Timer getOffTimer() {
        int off_timer_value = data[0x05];
        int off_timer_minutes = data[0x06];
        return new Timer(((off_timer_value & (byte) 0x80) >> 7) > 0, (off_timer_value & (byte) 0x7c) >> 2,
                (off_timer_value & 0x3) | (off_timer_minutes & (byte) 0xf));
    }

    public SwingMode getSwingMode() {
        if (getVersion() == 2) {
            // logger.debug("SwingMode value: {}", (data[0x07] & 0x0f));
            return SwingMode.fromId(data[0x07] & 0x0f);
        }

        if (getVersion() == 3) {
            // logger.debug("SwingMode value: {}", (data[0x07] & 0x0f));
            return SwingMode.fromId(data[0x07] & 0x0f);
        }
        return SwingMode.UNKNOWN;
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
        // Changed int to float, left byte[15], but blank
        double indoorTempInteger;
        double indoorTempDecimal;

        if (data[0] == (byte) 0xc0) {
            if (((Byte.toUnsignedInt(data[11]) - 50) / 2.0f) < -19
                    || ((Byte.toUnsignedInt(data[11]) - 50) / 2.0f) > 50) {
                return null;
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
                if (((Byte.toUnsignedInt(data[13]) - 50) / 2) < -19 || ((Byte.toUnsignedInt(data[13]) - 50) / 2) > 50) {
                    return null;
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

        return null;
    }

    public Float getIndoorTemperature1() {
        // this confuses me. Does not appear to be used and duplicates getIndoorTemperature() Left in for now
        logger.debug("this.responseType:{} this.bodyType:{}", this.responseType, this.bodyType);

        if ((this.responseType.equals("set") || this.responseType.equals("query")) && this.bodyType == -64) {
            if (data[11] != 0xFF) {
                int temp_integer = ((data[11]) - 50) / 2;
                double temp_decimal = ((data[15] & 0x0F)) * 0.1;
                if (data[11] > 49) {
                    return (float) (temp_integer + temp_decimal);
                } else {
                    return (float) (temp_integer - temp_decimal);
                }
            } else {
                return null;
            }
        } else {
            return (float) ((Byte.toUnsignedInt(data[11]) - 50) / 2);
        }
    }

    public Float getOutdoorTemperature() {
        // My AC just uses byte[12] for 0.5 degrees; Validated with NetHome App reading
        // Changed int to float, left byte[15], but blank
        if ((this.responseType.equals("set") || this.responseType.equals("query")) && this.bodyType == -64) {
            if (data[12] != 0xFF) {
                double temp_integer = (float) ((data[12] - 50f) / 2f);
                double temp_decimal = ((data[15] & 0xF0) >> 4) * 0.1f;
                if (data[12] > 49) {
                    return (float) (temp_integer + temp_decimal);
                } else {
                    return (float) (temp_integer - temp_decimal);
                }
            } else {
                return null;
            }
        } else {
            // return (Byte.toUnsignedInt(data[0x0c]) - 50) / 2.0f;
            return (float) ((Byte.toUnsignedInt(data[12]) - 50) / 2);
        }
    }

    public int getHumidity() {
        return (data[0x0d] & (byte) 0x7f);
    }
}
