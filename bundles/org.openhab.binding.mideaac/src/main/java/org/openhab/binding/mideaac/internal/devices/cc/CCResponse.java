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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCAuxHeatMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCFanSpeed;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCOperationalMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCPurifierMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCSwingAngle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CCResponse} performs the byte data stream decoding
 * The original reference is
 * https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea/devices/ac/message.py#L418
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCResponse {
    byte[] data;

    // set empty to match the return from an empty byte avoid null
    float empty = (float) -19.0;
    private Logger logger = LoggerFactory.getLogger(CCResponse.class);

    /**
     * Response class Parameters
     * 
     * @param data byte array from device
     * @param version version of the device
     */
    public CCResponse(byte[] data) {
        this.data = data;

        if (logger.isDebugEnabled()) {
            logger.debug("Power State: {}", getPowerState());
            logger.debug("Target Temperature: {}", getTargetTemperature());
            logger.debug("Indoor Temperature: {}", getIndoorTemperature());
            logger.debug("Operational Mode: {}", getCCOperationalMode());
            logger.debug("Fan Speed: {}", getCCFanSpeed());
            logger.debug("Vertical Angle: {}", getCCVerticalAngle());
            logger.debug("Horizontal Angle: {}", getCCHorizontalAngle());
            logger.debug("Purifier Mode: {}", getPurifierMode());
            logger.debug("Aux Heat Mode: {}", getAuxHeat());
            logger.debug("Eco Mode: {}", getEcoMode());
            logger.debug("Silent Mode: {}", getSilentMode());
            logger.debug("Sleep Function: {}", getSleepFunction());
            logger.debug("Target Humidity: {}", getMaximumHumidity());
            logger.debug("Room Humidity: {}", getHumidity());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Target Temperature Min: {}", getTargetTemperatureMin());
            logger.trace("Target Temperature Max: {}", getTargetTemperatureMax());
            logger.trace("Supports Humidity: {}", getSupportsHumidity());
            logger.trace("Supports Fan Speed: {}", getSupportsFanSpeed());
            logger.trace("Supports Vert Swing Angle: {}", getSupportsVertSwingAngle());
            logger.trace("Supports Horz Swing Angle: {}", getSupportsHorzSwingAngle());
            logger.trace("Supports Wind Sense: {}", getSupportsWindSense());
            logger.trace("Supports Eco: {}", getSupportsEco());
            logger.trace("Supports Silent: {}", getSupportsSilent());
            logger.trace("Supports Sleep: {}", getSupportsSleep());
            logger.trace("Supports Self Clean: {}", getSupportsSelfClean());
            logger.trace("Supports Purifier: {}", getSupportsPurifier());
            logger.trace("Supports Aux Heat: {}", getSupportsAuxHeat());
            logger.trace("Supported Aux Modes: {}", getSupportedAuxModes());
            logger.trace("Supported Op Modes: {}", getSupportedOpModes());
        }
    }

    /**
     * Device On or Off
     * 
     * @return power state true or false
     */
    public boolean getPowerState() {
        return (data[8]) > 0;
    }

    /**
     * Setpoint for Commercial AC
     * 
     * @return current setpoint in degrees C
     */
    public float getTargetTemperature() {
        return (float) ((data[11] / 2.0) - 40);
    }

    /**
     * Indoor Temperature
     * 
     * @return Indoor temperature
     */
    public double getIndoorTemperature() {
        return ((data[12] & 0xFF) << 8 | (data[13] & 0xFF)) / 10.0;
    }

    /**
     * This returns the target humidity for Dry mode, if supported
     * Possibly an add-on sensor is required in some cases
     * 
     * @return Target Humidity in Dry Mode
     */
    public int getMaximumHumidity() {
        return (data[24]);
    }

    /**
     * Room Humidity (if supported)
     * -1 = Off
     * 
     * @return Room Humidity
     */
    public int getHumidity() {
        if (data[25] == 0xff) {
            return 0;
        } else {
            return (data[25]);
        }
    }

    /**
     * Cool, Heat, Fan Only, etc.
     * 
     * @return Cool, Heat, Fan Only, etc.
     */
    public CCOperationalMode getCCOperationalMode() {
        return CCOperationalMode.fromId(data[31]);
    }

    /**
     * Low, Medium, High, Auto etc.
     * 
     * @return Low, Medium, High, Auto etc.
     */
    public CCFanSpeed getCCFanSpeed() {
        return CCFanSpeed.fromId(data[34]);
    }

    /**
     * Status of the vertical angle
     * 
     * @return Vertical angle Position
     */
    public CCSwingAngle getCCVerticalAngle() {
        return CCSwingAngle.fromId(data[41]);
    }

    /**
     * Status of the horizontal angle
     * 
     * @return Horizontal angle Position
     */
    public CCSwingAngle getCCHorizontalAngle() {
        return CCSwingAngle.fromId(data[43]);
    }

    /**
     * Ecomode status - Fan to Auto and temp to 24 C
     * 
     * @return Eco mode on (true) or (false)
     */
    public boolean getEcoMode() {
        return (data[56]) > 0;
    }

    /**
     * Silent Running Mode
     * 
     * @return Silent mode on (true) or (false)
     */
    public boolean getSilentMode() {
        return (data[58]) > 0;
    }

    /**
     * Sleep function status. Setpoint Temp increases in first
     * two hours of sleep by 1 degree in Cool mode
     * 
     * @return Sleep mode on (true) or (false)
     */
    public boolean getSleepFunction() {
        return (data[60]) > 0;
    }

    /**
     * Purifier Mode
     * 
     * @return Purifier Mode active
     */
    public CCPurifierMode getPurifierMode() {
        return CCPurifierMode.fromId(data[75]);
    }

    /**
     * Auxiliary Heat Mode
     * 
     * @return auxiliary heat active
     */
    public CCAuxHeatMode getAuxHeat() {
        return CCAuxHeatMode.fromId(data[87]);
    }

    /**
     * Command Tone
     * 
     * @return prompt tone true or false
     */
    public boolean getPromptTone() {
        return (data[80]) > 0;
    }

    // Capabilities Section

    /**
     * Minimum Temperature (Cool mode)
     * 
     * @return Minimum Temperature
     */
    public float getTargetTemperatureMin() {
        return (float) ((data[9] / 2.0) - 40);
    }

    /**
     * Maximum Temperature (Heat Mode)
     * 
     * @return Minimum Temperature
     */
    public float getTargetTemperatureMax() {
        return (float) (((data[10] & 0xFF) / 2.0) - 40);
    }

    /**
     * Supports Humidity
     * 
     * @return Humidity support true or false
     */
    public boolean getSupportsHumidity() {
        return (data[23]) > 0;
    }

    /**
     * Supports Fan Speed
     * 
     * @return Fan speed support true or false
     */
    public boolean getSupportsFanSpeed() {
        return (data[32]) > 0;
    }

    /**
     * Supports Vertical Swing Angle
     * 
     * @return Vertical swing angle support true or false
     */
    public boolean getSupportsVertSwingAngle() {
        return (data[40]) > 0;
    }

    /**
     * Supports Horizontal Swing Angle
     * 
     * @return Horizontal swing angle support true or false
     */
    public boolean getSupportsHorzSwingAngle() {
        return (data[42]) > 0;
    }

    /**
     * Supports Wind Sense
     * 
     * @return Wind sense support true or false
     */
    public boolean getSupportsWindSense() {
        return (data[44]) > 0;
    }

    /**
     * Supports Eco Mode
     * 
     * @return Eco mode support true or false
     */
    public boolean getSupportsEco() {
        return (data[55]) > 0;
    }

    /**
     * Supports Silent Mode
     * 
     * @return Silent mode support true or false
     */
    public boolean getSupportsSilent() {
        return (data[57]) > 0;
    }

    /**
     * Supports Sleep Function
     * 
     * @return Sleep function support true or false
     */
    public boolean getSupportsSleep() {
        return (data[59]) > 0;
    }

    /**
     * Supports Self Clean
     * 
     * @return Self clean support true or false
     */
    public boolean getSupportsSelfClean() {
        return (data[61]) > 0;
    }

    /**
     * Supports Purifier
     * 
     * @return Purifier support true or false
     */
    public boolean getSupportsPurifier() {
        return (data[73]) > 0;
    }

    /**
     * Supports Auxiliary Heat
     * 
     * @return Auxiliary heat support true or false
     */
    public boolean getSupportsAuxHeat() {
        return (data[82]) > 0;
    }

    /**
     * Supported Auxiliary Heat Modes
     *
     * @return List of supported auxiliary heat modes
     */
    public List<CCAuxHeatMode> getSupportedAuxModes() {
        List<CCAuxHeatMode> modes = new ArrayList<>();
        if (getSupportsAuxHeat()) {
            for (int i = 83; i < 87; i++) {
                int id = Byte.toUnsignedInt(data[i]);
                modes.add(CCAuxHeatMode.fromId(id));
            }
        }
        return modes;
    }

    /**
     * Supported Operational Modes
     *
     * @return List of supported operational modes
     */
    public List<CCOperationalMode> getSupportedOpModes() {
        List<CCOperationalMode> modes = new ArrayList<>();
        for (int i = 26; i < 31; i++) {
            int id = Byte.toUnsignedInt(data[i]);
            modes.add(CCOperationalMode.fromId(id));
        }
        return modes;
    }
}
