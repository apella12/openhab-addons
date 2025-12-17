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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCAuxHeatMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCFanSpeed;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCOperationalMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCSwingAngle;

/**
 * The {@link CCResponseTest} tests the methods in the CCResponse class
 * against an example CCresponse string.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class CCResponseTest {
    @org.jupnp.registry.event.Before

    byte[] data = HexFormat.of().parseHex(
            "01fe00000043005001728c7800cf00728c728c787800010141ff010203000603010008000000000001010103010000000000000000000001000100010000000000000000000000000001000200000100000101000102ff02ff");
    CCResponse response = new CCResponse(data);

    /**
     * Power State Test
     */
    @Test
    public void testGetPowerState() {
        boolean actualPowerState = response.getPowerState();
        assertEquals(true, actualPowerState);
    }

    /**
     * Operational Mode Test
     */
    @Test
    public void testGetCCOperationalMode() {
        CCOperationalMode mode = response.getCCOperationalMode();
        assertEquals(CCStringCommands.CCOperationalMode.HEAT, mode);
    }

    /**
     * Fan Speed Test
     */
    @Test
    public void testGetFanSpeed() {
        CCFanSpeed fanSpeed = response.getCCFanSpeed();
        assertEquals(CCStringCommands.CCFanSpeed.AUTO, fanSpeed);
    }

    /**
     * Target Humidity Test
     */
    @Test
    public void testGetTargetHumidity() {
        assertEquals(65, response.getMaximumHumidity());
    }

    /**
     * Indoor Temperature Test
     */
    @Test
    public void testGetIndoorTemperature() {
        assertEquals(20.7, response.getIndoorTemperature());
    }

    /**
     * Auxiliary Heat Status Test
     */
    @Test
    public void testGetAuxHeat() {
        CCAuxHeatMode auxHeatMode = response.getAuxHeat();
        assertEquals(CCStringCommands.CCAuxHeatMode.OFF, auxHeatMode);
    }

    /**
     * Vertical Angle Test
     */
    @Test
    public void testGetCCVerticalAngle() {
        CCSwingAngle vertAngle = response.getCCVerticalAngle();
        assertEquals(CCStringCommands.CCSwingAngle.POSITION_1, vertAngle);
    }

    /**
     * Horizontal Angle Test
     */
    @Test
    public void testGetCCHorizontalAngle() {
        CCSwingAngle horzAngle = response.getCCHorizontalAngle();
        assertEquals(CCStringCommands.CCSwingAngle.POSITION_3, horzAngle);
    }

    /**
     * Eco Mode Test
     */
    @Test
    public void testGetEcoMode() {
        assertEquals(false, response.getEcoMode());
    }

    /**
     * Sleep Function Test
     */
    @Test
    public void testGetSleepFunction() {
        assertEquals(false, response.getSleepFunction());
    }

    /**
     * Target Temperature Minimum Test
     */
    @Test
    public void testGetTargetTemperatureMin() {
        assertEquals(17.0f, response.getTargetTemperatureMin());
    }

    /**
     * Target Temperature Maximum Test
     */
    @Test
    public void testGetTargetTemperatureMax() {
        assertEquals(30.0f, response.getTargetTemperatureMax());
    }

    /**
     * Supports Humidity Test
     */
    @Test
    public void testGetSupportsHumidity() {
        assertEquals(true, response.getSupportsHumidity());
    }

    /**
     * Supports Fan Speed Test
     */
    @Test
    public void testGetSupportsFanSpeed() {
        assertEquals(true, response.getSupportsFanSpeed());
    }

    /**
     * Supports Vertical Swing Angle Test
     */
    @Test
    public void testGetSupportsVertSwingAngle() {
        assertEquals(true, response.getSupportsVertSwingAngle());
    }

    /**
     * Supports Horizontal Swing Angle Test
     */
    @Test
    public void testGetSupportsHorzSwingAngle() {
        assertEquals(true, response.getSupportsHorzSwingAngle());
    }

    /**
     * Supports Wind Sense Test
     */
    @Test
    public void testGetSupportsWindSense() {
        assertEquals(true, response.getSupportsWindSense());
    }

    /**
     * Supports Eco Mode Test
     */
    @Test
    public void testGetSupportsEco() {
        assertEquals(true, response.getSupportsEco());
    }

    /**
     * Supports Silent Mode Test
     */
    @Test
    public void testGetSupportsSilent() {
        assertEquals(true, response.getSupportsSilent());
    }

    /**
     * Supports Sleep Function Test
     */
    @Test
    public void testGetSupportsSleep() {
        assertEquals(true, response.getSupportsSleep());
    }

    /**
     * Supports Self Clean Test
     */
    @Test
    public void testGetSupportsSelfClean() {
        assertEquals(false, response.getSupportsSelfClean());
    }

    /**
     * Supports Purifier Test
     */
    @Test
    public void testGetSupportsPurifier() {
        assertEquals(true, response.getSupportsPurifier());
    }

    /**
     * Supports Auxiliary Heat Test
     */
    @Test
    public void testGetSupportsAuxHeat() {
        assertEquals(true, response.getSupportsAuxHeat());
    }

    /**
     * Supported Auxiliary Heat Modes Test
     */
    @Test
    public void testGetSupportedAuxModes() {
        var auxModes = response.getSupportedAuxModes();
        assertEquals(3, auxModes.stream().distinct().count());
    }

    /**
     * Supported Operational Modes Test
     */
    @Test
    public void testGetSupportedOpModes() {
        var opModes = response.getSupportedOpModes();
        assertEquals(4, opModes.stream().distinct().count());
    }
}
