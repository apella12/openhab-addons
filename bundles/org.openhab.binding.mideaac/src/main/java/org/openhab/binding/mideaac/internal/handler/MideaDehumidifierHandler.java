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
import java.util.Objects;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.callbacks.HumidifierCallback;
import org.openhab.binding.mideaac.internal.commands.A1CommandHelper;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.responses.A1Response;
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
 * The {@link MideaDehumidifierHandler} is responsible for handling commands, and
 * updating the dehumidifier thing's status and channels based on responses
 * from the device.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class MideaDehumidifierHandler extends AbstractMideaHandler implements HumidifierCallback {
    private final Logger logger = LoggerFactory.getLogger(MideaDehumidifierHandler.class);
    private final boolean imperialUnits;

    public MideaDehumidifierHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient) {
        super(thing, unitProvider, httpClient);
        this.imperialUnits = unitProvider.getMeasurementSystem() instanceof ImperialUnits;
    }

    @Override
    protected void requestCapabilitiesIfMissing() {
        // no-op for dehumidifier
    }

    @Override
    protected void refreshDeviceStateAll() {
        // no-op for dehumidifier
    }

    @Override
    protected void refreshDeviceState() {
        try {
            connectionManager.getStatus(this);
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
            A1Response lastresponse = connectionManager.getLastA1Response();
            if (channelUID.getId().equals(CHANNEL_POWER)) {
                connectionManager.sendCommand(A1CommandHelper.handlePower(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DH_FAN_SPEED)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1FanSpeed(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_ON_TIMER)) {
                connectionManager.sendCommand(A1CommandHelper.handleOnTimer(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_OFF_TIMER)) {
                connectionManager.sendCommand(A1CommandHelper.handleOffTimer(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_MAXIMUM_HUMIDITY)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1MaximumHumidity(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DEHUMIDIFIER_MODE)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1OperationalMode(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DEHUMIDIFIER_SWING)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1Swing(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DEHUMIDIFIER_CHILD_LOCK)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1ChildLock(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DEHUMIDIFIER_TANK_SETPOINT)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1TankSetpoint(command, lastresponse), this);
            } else if (channelUID.getId().equals(CHANNEL_DEHUMIDIFIER_ANION)) {
                connectionManager.sendCommand(A1CommandHelper.handleA1Anion(command, lastresponse), this);
            }
        } catch (MideaConnectionException | MideaAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MideaException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void updateChannels(A1Response response) {
        updateChannel(CHANNEL_POWER, OnOffType.from(response.getPowerState()));
        updateChannel(CHANNEL_DH_FAN_SPEED, new StringType(response.getA1FanSpeed().toString()));
        updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
        updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
        updateChannel(CHANNEL_DEHUMIDIFIER_MODE, new StringType(response.getA1OperationalMode().toString()));
        updateChannel(CHANNEL_MAXIMUM_HUMIDITY, new DecimalType(response.getMaximumHumidity()));
        updateChannel(CHANNEL_HUMIDITY, new DecimalType(response.getCurrentHumidity()));
        updateChannel(CHANNEL_DEHUMIDIFIER_ANION, OnOffType.from(response.getA1Anion()));
        updateChannel(CHANNEL_DEHUMIDIFIER_CHILD_LOCK, OnOffType.from(response.getA1ChildLock()));
        updateChannel(CHANNEL_DEHUMIDIFIER_TANK, new DecimalType(response.getTank()));
        updateChannel(CHANNEL_DEHUMIDIFIER_TANK_SETPOINT, new DecimalType(response.getTankSetpoint()));
        updateChannel(CHANNEL_DEHUMIDIFIER_SWING, OnOffType.from(response.getA1SwingMode()));

        QuantityType<Temperature> indoorTemperature = new QuantityType<Temperature>(response.getIndoorTemperature(),
                SIUnits.CELSIUS);

        if (imperialUnits) {
            indoorTemperature = Objects.requireNonNull(indoorTemperature.toUnit(ImperialUnits.FAHRENHEIT));
        }

        updateChannel(CHANNEL_INDOOR_TEMPERATURE, indoorTemperature);
    }
}
