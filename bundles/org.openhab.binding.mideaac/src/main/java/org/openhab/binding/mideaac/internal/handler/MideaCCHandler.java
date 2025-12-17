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
package org.openhab.binding.mideaac.internal.handler;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.callbacks.CCCallback;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.devices.cc.CCCommandHelper;
import org.openhab.binding.mideaac.internal.devices.cc.CCControlCommand;
import org.openhab.binding.mideaac.internal.devices.cc.CCResponse;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCAuxHeatMode;
import org.openhab.binding.mideaac.internal.devices.cc.CCStringCommands.CCOperationalMode;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaCCHandler} is responsible for handling commands, and
 * updating the commercial AC thing's status and channels based on responses
 * from the device.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class MideaCCHandler extends AbstractMideaHandler implements CCCallback {
    private final Logger logger = LoggerFactory.getLogger(MideaCCHandler.class);
    private final boolean imperialUnits;
    private boolean capabilitiesInitialized = false;

    public MideaCCHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient) {
        super(thing, unitProvider, httpClient);
        this.imperialUnits = unitProvider.getMeasurementSystem() instanceof ImperialUnits;
    }

    @Override
    protected void requestCapabilitiesIfMissing() {
        // no-op for commercial AC
        // They come from the query response
    }

    @Override
    protected void refreshDeviceStateAll() {
        // no-op for commercial AC
    }

    @Override
    protected void refreshDeviceState() {
        try {
            connectionManager.getCCStatus(this);
        } catch (MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MideaConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void handleDeviceCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());

        try {
            CCResponse lastResponse = connectionManager.getLastCCResponse();
            CCControlCommand ccCommand = null;
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    ccCommand = CCCommandHelper.handlePower(command, lastResponse);
                    break;

                case CHANNEL_TARGET_TEMPERATURE:
                    ccCommand = CCCommandHelper.handleTargetTemperature(command, lastResponse);
                    break;

                case CHANNEL_CC_FAN_SPEED:
                    ccCommand = CCCommandHelper.handleFanSpeed(command, lastResponse);
                    break;

                case CHANNEL_SWING_ANGLE_VERTICAL:
                    ccCommand = CCCommandHelper.handleVerticalAngle(command, lastResponse);
                    break;

                case CHANNEL_SWING_ANGLE_HORIZONAL:
                    ccCommand = CCCommandHelper.handleHorizontalAngle(command, lastResponse);
                    break;

                case CHANNEL_PURIFIER_MODE:
                    ccCommand = CCCommandHelper.handlePurifier(command, lastResponse);
                    break;

                case CHANNEL_CC_AUXILIARY_HEAT:
                    ccCommand = CCCommandHelper.handleAuxHeat(command, lastResponse);
                    break;

                case CHANNEL_ECO_MODE:
                    ccCommand = CCCommandHelper.handleEco(command, lastResponse);
                    break;

                // case CHANNEL_SILENT:
                // ccCommand = CCCommandHelper.handleSilent(command, lastResponse);
                // break;

                case CHANNEL_SLEEP_FUNCTION:
                    ccCommand = CCCommandHelper.handleSleep(command, lastResponse);
                    break;

                case CHANNEL_MAXIMUM_HUMIDITY:
                    ccCommand = CCCommandHelper.handleTargetHumidity(command, lastResponse);
                    break;

                case CHANNEL_SCREEN_DISPLAY:
                    ccCommand = CCCommandHelper.handleDisplay(command, lastResponse);
                    break;

                default:
                    logger.warn("Unhandled CC channel {}", channelUID.getId());
            }

            if (ccCommand != null) {
                connectionManager.sendCommand(ccCommand, this);
            }
            refreshDeviceState();
        } catch (MideaConnectionException | MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void updateChannels(CCResponse response) {
        updateChannel(CHANNEL_POWER, OnOffType.from(response.getPowerState()));
        updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getCCOperationalMode().toString()));
        updateChannel(CHANNEL_CC_FAN_SPEED, new StringType(response.getCCFanSpeed().toString()));
        updateChannel(CHANNEL_SWING_ANGLE_HORIZONAL, new StringType(response.getCCHorizontalAngle().toString()));
        updateChannel(CHANNEL_SWING_ANGLE_VERTICAL, new StringType(response.getCCVerticalAngle().toString()));
        updateChannel(CHANNEL_PURIFIER_MODE, new StringType(response.getPurifierMode().toString()));
        updateChannel(CHANNEL_CC_AUXILIARY_HEAT, new StringType(response.getAuxHeat().toString()));
        updateChannel(CHANNEL_ECO_MODE, OnOffType.from(response.getEcoMode()));
        updateChannel(CHANNEL_SLEEP_FUNCTION, OnOffType.from(response.getSleepFunction()));
        updateChannel(CHANNEL_MAXIMUM_HUMIDITY, new DecimalType(response.getMaximumHumidity()));

        QuantityType<Temperature> targetTemperature = new QuantityType<Temperature>(response.getTargetTemperature(),
                SIUnits.CELSIUS);
        QuantityType<Temperature> indoorTemperature = new QuantityType<Temperature>(response.getIndoorTemperature(),
                SIUnits.CELSIUS);

        if (imperialUnits) {
            targetTemperature = Objects.requireNonNull(targetTemperature.toUnit(ImperialUnits.FAHRENHEIT));
            indoorTemperature = Objects.requireNonNull(indoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
        }

        updateChannel(CHANNEL_TARGET_TEMPERATURE, targetTemperature);
        updateChannel(CHANNEL_INDOOR_TEMPERATURE, indoorTemperature);

        // Only run capability discovery once
        if (!capabilitiesInitialized) {
            properties = editProperties();

            // Numeric ranges
            properties.put(PROPERTY_TEMPERATURES_MIN_DEFAULT, String.valueOf(response.getTargetTemperatureMin()));
            properties.put(PROPERTY_TEMPERATURES_MAX_DEFAULT, String.valueOf(response.getTargetTemperatureMax()));

            // Boolean capabilities
            properties.put(PROPERTY_HUMIDITY_MANUAL_SET, String.valueOf(response.getSupportsHumidity()));
            properties.put(PROPERTY_FAN_SPEED_CONTROL_AUTO, String.valueOf(response.getSupportsFanSpeed()));
            properties.put(PROPERTY_SWING_UD_ANGLE, String.valueOf(response.getSupportsVertSwingAngle()));
            properties.put(PROPERTY_SWING_LR_ANGLE, String.valueOf(response.getSupportsHorzSwingAngle()));
            properties.put(PROPERTY_WIND_ON_ME, String.valueOf(response.getSupportsWindSense()));
            properties.put(PROPERTY_PRESET_ECO, String.valueOf(response.getSupportsEco()));
            properties.put(PROPERTY_FAN_SPEED_CONTROL_SILENT, String.valueOf(response.getSupportsSilent()));
            properties.put(PROPERTY_SLEEP_MODE, String.valueOf(response.getSupportsSleep()));
            properties.put(PROPERTY_SELF_CLEAN, String.valueOf(response.getSupportsSelfClean()));
            properties.put(PROPERTY_PURIFIER, String.valueOf(response.getSupportsPurifier()));
            properties.put(PROPERTY_AUX_ELECTRIC_HEAT, String.valueOf(response.getSupportsAuxHeat()));

            // Enumerated modes
            List<CCAuxHeatMode> auxModes = response.getSupportedAuxModes();

            // Reset all to false
            properties.put(PROPERTY_AUX_MODE_AUTO, "false");
            properties.put(PROPERTY_AUX_MODE_ON, "false");
            properties.put(PROPERTY_AUX_MODE_OFF, "false");

            // Mark supported ones
            for (CCAuxHeatMode mode : auxModes) {
                switch (mode) {
                    case AUTO:
                        properties.put(PROPERTY_AUX_MODE_AUTO, "true");
                        break;
                    case ON:
                        properties.put(PROPERTY_AUX_MODE_ON, "true");
                        break;
                    case OFF:
                        properties.put(PROPERTY_AUX_MODE_OFF, "true");
                        break;
                }
            }

            List<CCOperationalMode> supportedModes = response.getSupportedOpModes();

            // Reset all to false first (optional, ensures clean state)
            properties.put(PROPERTY_MODES_AUTO, "false");
            properties.put(PROPERTY_MODES_COOL, "false");
            properties.put(PROPERTY_MODES_HEAT, "false");
            properties.put(PROPERTY_MODES_DRY, "false");
            properties.put(PROPERTY_MODES_FAN_ONLY, "false");

            // Mark supported ones as true
            for (CCOperationalMode mode : supportedModes) {
                switch (mode) {
                    case AUTO:
                        properties.put(PROPERTY_MODES_AUTO, "true");
                        break;
                    case COOL:
                        properties.put(PROPERTY_MODES_COOL, "true");
                        break;
                    case HEAT:
                        properties.put(PROPERTY_MODES_HEAT, "true");
                        break;
                    case DRY:
                        properties.put(PROPERTY_MODES_DRY, "true");
                        break;
                    case FAN_ONLY:
                        properties.put(PROPERTY_MODES_FAN_ONLY, "true");
                        break;
                    default:
                        logger.warn("Unknown CCOperationalMode: {}", mode);
                        break;
                }
            }

            updateProperties(properties);
            capabilitiesInitialized = true;

            logger.debug("CC capabilities parsed and stored in properties: {}", properties);
        }
    }
}
