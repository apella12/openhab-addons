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

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.MideaACConfiguration;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.binding.mideaac.internal.security.Cloud;
import org.openhab.binding.mideaac.internal.security.CloudProvider;
import org.openhab.binding.mideaac.internal.security.Clouds;
import org.openhab.binding.mideaac.internal.security.Decryption8370Result;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.binding.mideaac.internal.security.Security.MsgType;
import org.openhab.binding.mideaac.internal.security.TokenKey;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class MideaACHandler extends BaseThingHandler implements DiscoveryHandler {

    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);

    private @Nullable MideaACConfiguration config;
    private @Nullable Map<String, String> properties;
    private String ipAddress = "";
    private String ipPort = "";
    private String deviceId = "";
    private int version = 0;

    public CloudProvider cloudProvider = new CloudProvider("", "", "", "", "", "", "", "");
    private Security security = new Security(cloudProvider);

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public @Nullable Security getSecurity() {
        return security;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    private static final StringType OPERATIONAL_MODE_OFF = new StringType("OFF");
    private static final StringType OPERATIONAL_MODE_AUTO = new StringType("AUTO");
    private static final StringType OPERATIONAL_MODE_COOL = new StringType("COOL");
    private static final StringType OPERATIONAL_MODE_DRY = new StringType("DRY");
    private static final StringType OPERATIONAL_MODE_HEAT = new StringType("HEAT");
    private static final StringType OPERATIONAL_MODE_FAN_ONLY = new StringType("FAN_ONLY");

    private static final StringType FAN_SPEED_OFF = new StringType("OFF");
    private static final StringType FAN_SPEED_SILENT = new StringType("SILENT");
    private static final StringType FAN_SPEED_LOW = new StringType("LOW");
    private static final StringType FAN_SPEED_MEDIUM = new StringType("MEDIUM");
    private static final StringType FAN_SPEED_HIGH = new StringType("HIGH");
    private static final StringType FAN_SPEED_FULL = new StringType("FULL");
    private static final StringType FAN_SPEED_AUTO = new StringType("AUTO");

    private static final StringType SWING_MODE_OFF = new StringType("OFF");
    private static final StringType SWING_MODE_VERTICAL = new StringType("VERTICAL");
    private static final StringType SWING_MODE_HORIZONTAL = new StringType("HORIZONTAL");
    private static final StringType SWING_MODE_BOTH = new StringType("BOTH");
    private Clouds clouds;

    private ConnectionManager connectionManager;

    private final SystemOfUnits systemOfUnits;

    private final HttpClient httpClient;

    public boolean doPoll = true;
    public boolean retry = true;
    public boolean connectionMessage = true;

    private ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private Response getLastResponse() {
        return getConnectionManager().getLastResponse();
    }

    public MideaACHandler(Thing thing, String ipv4Address, UnitProvider unitProvider, HttpClient httpClient,
            Clouds clouds) {
        super(thing);
        this.thing = thing;
        this.systemOfUnits = unitProvider.getMeasurementSystem();
        this.httpClient = httpClient;
        this.clouds = clouds;
        connectionManager = new ConnectionManager(ipv4Address, this);
    }

    public Clouds getClouds() {
        return clouds;
    }

    protected boolean isImperial() {
        return systemOfUnits instanceof ImperialUnits ? true : false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());

        // this is to avoid collisions from handleCommand with the Polling
        getConnectionManager().cancelConnectionMonitorJob();

        if (command instanceof RefreshType) {
            connectionManager.connect();
            return;
        }

        // This is to go directly to the set commands, after authorization
        doPoll = false;
        connectionManager.connect();

        if (channelUID.getId().equals(CHANNEL_POWER)) {
            handlePower(command);
        } else if (channelUID.getId().equals(CHANNEL_OPERATIONAL_MODE)) {
            handleOperationalMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TARGET_TEMPERATURE)) {
            handleTargetTemperature(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            handleFanSpeed(command);
        } else if (channelUID.getId().equals(CHANNEL_ECO_MODE)) {
            handleEcoMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TURBO_MODE)) {
            handleTurboMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SWING_MODE)) {
            handleSwingMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SCREEN_DISPLAY)) {
            handleScreenDisplay(command);
        } else if (channelUID.getId().equals(CHANNEL_TEMP_UNIT)) {
            handleTempUnit(command);
        } else if (channelUID.getId().equals(CHANNEL_SLEEP_FUNCTION)) {
            handleSleepFunction(command);
        }
    }

    public void handlePower(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setPowerState(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setPowerState(true);
        } else {
            logger.debug("Unknown power state command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleOperationalMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof StringType) {
            if (command.equals(OPERATIONAL_MODE_OFF)) {
                commandSet.setPowerState(false);
                return;
            } else if (command.equals(OPERATIONAL_MODE_AUTO)) {
                commandSet.setOperationalMode(OperationalMode.AUTO);
            } else if (command.equals(OPERATIONAL_MODE_COOL)) {
                commandSet.setOperationalMode(OperationalMode.COOL);
            } else if (command.equals(OPERATIONAL_MODE_DRY)) {
                commandSet.setOperationalMode(OperationalMode.DRY);
            } else if (command.equals(OPERATIONAL_MODE_HEAT)) {
                commandSet.setOperationalMode(OperationalMode.HEAT);
            } else if (command.equals(OPERATIONAL_MODE_FAN_ONLY)) {
                commandSet.setOperationalMode(OperationalMode.FAN_ONLY);
            } else {
                logger.debug("Unknown operational mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    private static float convertTargetCelsiusTemperatureToInRange(float temperature) {
        if (temperature < 17.0f) {
            return 17.0f;
        }
        if (temperature > 30.0f) {
            return 30.0f;
        }

        return temperature;
    }

    private static float convertTargetFahrenheitTemperatureToInRange(float temperature) {
        if (temperature < 62.0f) {
            return 62.0f;
        }
        if (temperature > 86.0f) {
            return 86.0f;
        }

        return temperature;
    }

    @SuppressWarnings("null")
    public void handleTargetTemperature(Command command) {
        Response lastResponse = getLastResponse();
        CommandSet commandSet = CommandSet.fromResponse(lastResponse);

        if (command instanceof DecimalType) {
            QuantityType<Temperature> quantity = new QuantityType<Temperature>(((DecimalType) command).doubleValue(),
                    lastResponse.getTempUnit() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS);

            if (lastResponse.getTempUnit()) { // Is this always false? My unit always uses Celsius for calcs
                if (isImperial()) {
                    logger.debug("handleTargetTemperature: Set field of type Integer F > F");
                    commandSet.setTargetTemperature(convertTargetFahrenheitTemperatureToInRange(quantity.floatValue()));
                } else {
                    logger.debug("handleTargetTemperature: Set field of type Integer C > F");
                    commandSet.setTargetTemperature(convertTargetCelsiusTemperatureToInRange(
                            quantity.toUnit(ImperialUnits.FAHRENHEIT).floatValue()));
                }
            } else {
                if (isImperial()) {
                    logger.debug("handleTargetTemperature: Set field of type Integer F > C");
                    commandSet.setTargetTemperature(
                            convertTargetFahrenheitTemperatureToInRange(quantity.toUnit(SIUnits.CELSIUS).floatValue()));
                } else {
                    logger.debug("handleTargetTemperature: Set field of type Integer C > C");
                    commandSet.setTargetTemperature(convertTargetCelsiusTemperatureToInRange(quantity.floatValue()));
                }
            }

            getConnectionManager().sendCommandAndMonitor(commandSet);
        } else if (command instanceof QuantityType) {
            QuantityType<?> quantity = (QuantityType<?>) command;
            Unit<?> unit = quantity.getUnit();
            logger.debug(
                    "handleTargetTemperature: Set field of type Integer to value of item QuantityType with unit {}",
                    unit);
            if (unit.equals(ImperialUnits.FAHRENHEIT) || unit.equals(SIUnits.CELSIUS)) {
                if (lastResponse.getTempUnit() && unit.equals(ImperialUnits.FAHRENHEIT)) {
                    commandSet.setTargetTemperature(convertTargetFahrenheitTemperatureToInRange(quantity.floatValue()));
                } else if (lastResponse.getTempUnit() && unit.equals(SIUnits.CELSIUS)) {
                    commandSet.setTargetTemperature(convertTargetFahrenheitTemperatureToInRange(
                            quantity.toUnit(ImperialUnits.FAHRENHEIT).floatValue()));
                } else if (!lastResponse.getTempUnit() && unit.equals(SIUnits.CELSIUS)) {
                    commandSet.setTargetTemperature(convertTargetCelsiusTemperatureToInRange(quantity.floatValue()));
                } else if (!lastResponse.getTempUnit() && unit.equals(ImperialUnits.FAHRENHEIT)) {
                    commandSet.setTargetTemperature(
                            convertTargetCelsiusTemperatureToInRange(quantity.toUnit(SIUnits.CELSIUS).floatValue()));
                }

                getConnectionManager().sendCommandAndMonitor(commandSet);
            }
        } else {
            logger.debug("handleTargetTemperature unsupported commandType:{}", command.getClass().getTypeName());
        }
    }

    public void handleFanSpeed(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof StringType) {
            commandSet.setPowerState(true);
            if (command.equals(FAN_SPEED_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(FAN_SPEED_SILENT)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.SILENT2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.SILENT3);
                }
            } else if (command.equals(FAN_SPEED_LOW)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.LOW2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.LOW3);
                }
            } else if (command.equals(FAN_SPEED_MEDIUM)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM3);
                }
            } else if (command.equals(FAN_SPEED_HIGH)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.HIGH2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.HIGH3);
                }
            } else if (command.equals(FAN_SPEED_FULL)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.FULL2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.FULL3);
                }
            } else if (command.equals(FAN_SPEED_AUTO)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.AUTO2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.AUTO3);
                }
            } else {
                logger.debug("Unknown fan speed command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleEcoMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setEcoMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setEcoMode(true);
        } else {
            logger.debug("Unknown eco mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleSwingMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command instanceof StringType) {
            if (command.equals(SWING_MODE_OFF)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.OFF2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.OFF3);
                }
            } else if (command.equals(SWING_MODE_VERTICAL)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.VERTICAL2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.VERTICAL3);
                }
            } else if (command.equals(SWING_MODE_HORIZONTAL)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL3);
                }
            } else if (command.equals(SWING_MODE_BOTH)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.BOTH2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.BOTH3);
                }
            } else {
                logger.debug("Unknown swing mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleTurboMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setTurboMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setTurboMode(true);
        } else {
            logger.debug("Unknown turbo mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleScreenDisplay(Command command) {
        // this doesn't work for me. The bit is always off (false) even with the LED on
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setScreenDisplay(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setScreenDisplay(true);
        } else {
            logger.debug("Unknown screen display command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleTempUnit(Command command) {
        // This is only for the display, calcs always in Celsius
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setFahrenheit(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setFahrenheit(true);
        } else {
            logger.debug("Unknown temperature unit/farenheit command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    public void handleSleepFunction(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setSleepMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setSleepMode(true);
        } else {
            logger.debug("Unknown sleep Mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        connectionManager.disconnect();
        getConnectionManager().cancelConnectionMonitorJob();
        connectionManager.resetDroppedCommands();
        connectionManager.updateChannel(DROPPED_COMMANDS, new DecimalType(connectionManager.getDroppedCommands()));

        setCloudProvider(CloudProvider.getCloudProvider("MSmartHome"));
        setSecurity(new Security(cloudProvider));

        config = getConfigAs(MideaACConfiguration.class);
        properties = editProperties();

        logger.debug("MideaACHandler config for {} is {}", thing.getUID(), config);

        if (!config.isValid()) {
            logger.warn("Configuration invalid for {}", thing.getUID());
            if (config.isDiscoveryNeeded()) {
                logger.warn("Discovery needed, discovering....{}", thing.getUID());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Configuration missing, discovery needed. Discovering...");
                MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();

                try {
                    discoveryService.discoverThing(config.getIpAddress(), this);
                } catch (Exception e) {
                    logger.error("Discovery failure for {}: {}", thing.getUID(), e.getMessage());
                }
                return;
            } else {
                logger.debug("MideaACHandler config of {} is invalid. Check configuration", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid MideaAC config. Check configuration.");
                return;
            }
        } else {
            logger.debug("Configuration valid for {}", thing.getUID());
        }

        ipAddress = config.getIpAddress();
        ipPort = config.getIpPort();
        deviceId = config.getDeviceId();
        version = Integer.parseInt(properties.get(PROPERTY_VERSION).toString());

        logger.debug("IPAddress: {}", ipAddress);
        logger.debug("IPPort: {}", ipPort);
        logger.debug("ID: {}", deviceId);
        logger.debug("Version: {}", version);

        updateStatus(ThingStatus.UNKNOWN);

        connectionManager.connect();
    }

    @SuppressWarnings("null")
    @Override
    public void discovered(DiscoveryResult discoveryResult) {
        logger.debug("Discovered {}", thing.getUID());
        String deviceId = discoveryResult.getProperties().get(CONFIG_DEVICEID).toString();
        String ipPort = discoveryResult.getProperties().get(CONFIG_IP_PORT).toString();

        Configuration configuration = editConfiguration();

        configuration.put(CONFIG_DEVICEID, deviceId);
        configuration.put(CONFIG_IP_PORT, ipPort);

        updateConfiguration(configuration);

        properties = editProperties();
        properties.put(PROPERTY_VERSION, discoveryResult.getProperties().get(PROPERTY_VERSION).toString());
        properties.put(PROPERTY_SN, discoveryResult.getProperties().get(PROPERTY_SN).toString());
        properties.put(PROPERTY_SSID, discoveryResult.getProperties().get(PROPERTY_SSID).toString());
        properties.put(PROPERTY_TYPE, discoveryResult.getProperties().get(PROPERTY_TYPE).toString());
        updateProperties(properties);

        initialize();
    }

    /*
     * Manage the ONLINE/OFFLINE status of the thing
     */
    private void markOnline() {
        if (!isOnline()) {
            logger.debug("Changing status of {} from {}({}) to ONLINE", thing.getUID(), getStatus(), getDetail());
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void markOffline() {
        if (isOnline()) {
            logger.debug("Changing (disconnect) status of {} from {}({}) to OFFLINE", thing.getUID(), getStatus(),
                    getDetail());
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void markOfflineWithMessage(ThingStatusDetail statusDetail, String statusMessage) {
        // If it's offline with no detail or if it's not offline, mark it offline with detailed status
        if (!isOffline()) {
            logger.info("Changing status of {} from {}({}) to OFFLINE({})", thing.getUID(), getStatus(), getDetail(),
                    statusDetail);
        }
        if ((isOffline() && getDetail() == ThingStatusDetail.NONE)
                || (isOffline() && !statusMessage.equals(getDescription()))) {
            logger.debug("Changing status of {} from {}({}) to OFFLINE({})", thing.getUID(), getStatus(), getDetail(),
                    statusDetail);
        }

        updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);

        // This is to space out the looping with a short then long pause
        if (retry) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.debug("An interupted error (pause) has occured {}", e.getMessage());
            }
            getConnectionManager().cancelConnectionMonitorJob();
            getConnectionManager().disconnect();
            retry = false;
            getConnectionManager().connect();
        } else {
            if (connectionMessage) {
                logger.info("Connection issue, resetting, please wait ...");
            }
            connectionMessage = false;
            getConnectionManager().cancelConnectionMonitorJob();
            getConnectionManager().disconnect();
            getConnectionManager().scheduleConnectionMonitorJob();
        }
    }

    private boolean isOnline() {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    private boolean isOffline() {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    public boolean getDoPoll() {
        return doPoll;
    }

    @Override
    public void dispose() {
        connectionManager.cancelConnectionMonitorJob();
        markOffline();
    }

    public void resetDoPoll() {
        doPoll = true;
    }

    public void resetRetry() {
        retry = true;
    }

    public void resetConnectionMessage() {
        connectionMessage = true;
    }

    private ThingStatus getStatus() {
        return thing.getStatus();
    }

    private ThingStatusDetail getDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    private @Nullable String getDescription() {
        return thing.getStatusInfo().getDescription();
    }

    public void setCloudProvider(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    /*
     * The {@link ConnectionManager} class is responsible for managing the state of the TCP connection to the
     * indoor AC unit evaporator.
     *
     * @author Jacek Dobrowolski - Initial Contribution
     */
    private class ConnectionManager {
        private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

        private boolean deviceIsConnected;
        private int droppedCommands = 0;

        private @Nullable Socket socket;
        private @Nullable InputStream inputStream;
        private @Nullable DataOutputStream writer;

        private @Nullable ScheduledFuture<?> connectionMonitorJob;

        private byte[] data = HexFormat.of().parseHex("C00042667F7F003C0000046066000000000000000000F9ECDB");

        private String responseType = "query";

        private byte bodyType = (byte) 0xc0;

        private Response lastResponse = new Response(data, getVersion(), responseType, bodyType);
        private MideaACHandler mideaACHandler;

        public Response getLastResponse() {
            return this.lastResponse; // JO addded this. 11/28/23
        }

        Runnable connectionMonitorRunnable = () -> {
            logger.debug("Connecting to {} at IP {} for Poll", thing.getUID(), ipAddress);
            connect();
        };

        public ConnectionManager(String ipv4Address, MideaACHandler mideaACHandler) {
            deviceIsConnected = false;
            this.mideaACHandler = mideaACHandler;
        }

        public static boolean isBlank(String str) {
            return str.trim().isEmpty();
        }

        public void resetDroppedCommands() {
            droppedCommands = 0;
        }

        public int getDroppedCommands() {
            return droppedCommands = 0;
        }

        /*
         * Connect to the command and serial port(s) on the device. The serial connections are established only for
         * devices that support serial.
         */

        @SuppressWarnings("null")
        private Date getTokenReqested() {
            Cloud cloud = mideaACHandler.getClouds().get(config.getEmail(), config.getPassword(), cloudProvider);
            return cloud.getTokenRequested();
        }

        @SuppressWarnings("null")
        private boolean reAuthenticationNeeded() {
            int reuth = config.getReauth();
            if (reuth == 0) {
                return false;
            }
            Calendar now = Calendar.getInstance();
            Calendar tokenRequestedAt = Calendar.getInstance();
            tokenRequestedAt.setTime(getTokenReqested());
            tokenRequestedAt.add(Calendar.HOUR, reuth);

            return now.compareTo(tokenRequestedAt) > 0;
        }

        @SuppressWarnings("null")
        protected synchronized void connect() {
            if (reAuthenticationNeeded()) {
                logger.debug("Force re-authentication has initiated");
                this.authenticate();
            }

            logger.trace("Connecting to {} at {}:{}", thing.getUID(), ipAddress, ipPort);

            // Open socket
            try {
                socket = new Socket();
                socket.setSoTimeout(config.getTimeout() * 1000);
                if (ipPort != null) {
                    int port = Integer.parseInt(ipPort);
                    socket.connect(new InetSocketAddress(ipAddress, port), config.getTimeout() * 1000);
                }
            } catch (IOException e) {
                logger.debug("IOException connecting to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }

            // Create streams
            try {
                writer = new DataOutputStream(socket.getOutputStream());
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                logger.debug("IOException getting streams for {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }
            if (!deviceIsConnected || !connectionMessage) {
                logger.info("Connected to {} at {}", thing.getUID(), ipAddress);
                mideaACHandler.resetRetry();
                mideaACHandler.resetConnectionMessage();
            }
            logger.debug("Connected to {} at {}", thing.getUID(), ipAddress);
            deviceIsConnected = true;
            markOnline();
            if (getVersion() != 3) {
                logger.debug("Device {}@{} not require authentication, getting status", thing.getUID(), ipAddress);
                requestStatus(mideaACHandler.getDoPoll());
            } else {
                logger.debug("Device {}@{} require authentication, going to authenticate", thing.getUID(), ipAddress);
                authenticate();
            }
        }

        @SuppressWarnings("null")
        public void authenticate() {
            logger.trace("Version: {}", getVersion());
            logger.trace("Key: {}", config.getKey());
            logger.trace("Token: {}", config.getToken());

            if (getVersion() == 3) {
                if (!isBlank(config.getToken()) && !isBlank(config.getKey())) {
                    logger.debug("Device {}@{} authenticating", thing.getUID(), ipAddress);
                    doAuthentication();
                } else {
                    if (isBlank(config.getToken()) && isBlank(config.getKey())) {
                        if (isBlank(config.getEmail()) || isBlank(config.getPassword())) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Token and Key missing in configuration.");
                            logger.warn("Device {}@{} cannot authenticate, token and key missing", thing.getUID(),
                                    ipAddress);
                        } else {
                            if (isBlank(config.getCloud())) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "Cloud Provider missing in configuration.");
                                logger.warn("Device {}@{} cannot authenticate, Cloud Provider missing", thing.getUID(),
                                        ipAddress);
                            } else {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                        "Retrieving Token and Key from cloud.");
                                logger.info("Retrieving Token and Key from cloud");
                                CloudProvider cloudProvider = CloudProvider.getCloudProvider(config.getCloud());
                                getTokenKeyCloud(cloudProvider);
                            }
                        }
                    } else if (isBlank(config.getToken())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Token missing in configuration.");
                        logger.warn("Device {}@{} cannot authenticate, token missing", thing.getUID(), ipAddress);
                    } else if (isBlank(config.getKey())) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Key missing in configuration.");
                        logger.warn("Device {}@{} cannot authenticate, key missing", thing.getUID(), ipAddress);
                    }
                }
            } else {
                logger.debug("Device {}@{} with version {} does not require authentication, not going to authenticate",
                        thing.getUID(), ipAddress, getVersion());
            }
        }

        @SuppressWarnings("null")
        private void getTokenKeyCloud(CloudProvider cloudProvider) {
            Cloud cloud = mideaACHandler.getClouds().get(config.getEmail(), config.getPassword(), cloudProvider);
            cloud.setHttpClient(httpClient);
            if (cloud.login()) {
                TokenKey tk = cloud.getToken(config.getDeviceId());
                Configuration configuration = editConfiguration();

                configuration.put(CONFIG_TOKEN, tk.getToken());
                configuration.put(CONFIG_KEY, tk.getKey());
                updateConfiguration(configuration);

                logger.trace("Token: {}", tk.getToken());
                logger.trace("Key: {}", tk.getKey());
                logger.info("Token and Key obtained from cloud, saving, initializing");
                initialize();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Can't retrieve Token and Key from Cloud (%s).", cloud.getErrMsg()));
                logger.warn("Can't retrieve Token and Key from Cloud ({})", cloud.getErrMsg());
            }
        }

        @SuppressWarnings("null")
        private void doAuthentication() {
            byte[] request = mideaACHandler.getSecurity().encode8370(Utils.hexStringToByteArray(config.getToken()),
                    MsgType.MSGTYPE_HANDSHAKE_REQUEST);
            try {
                logger.trace("Device {}@{} writing handshake_request: {}", thing.getUID(), ipAddress,
                        Utils.bytesToHex(request));

                write(request);
                byte[] response = read();

                if (response != null && response.length > 0) {
                    logger.trace("Device {}@{} response for handshake_request length: {}", thing.getUID(), ipAddress,
                            response.length);
                    if (response.length == 72) {
                        boolean success = mideaACHandler.getSecurity().tcpKey(Arrays.copyOfRange(response, 8, 72),
                                Utils.hexStringToByteArray(config.getKey()));
                        if (success) {
                            logger.debug("Authentication successful");
                            // altering the sleep causes write errors problems, needs to be at least 1000
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                logger.debug("An interupted error (success) has occured {}", e.getMessage());
                            }
                            requestStatus(mideaACHandler.getDoPoll());
                        } else {
                            logger.debug("Invalid Key. Correct Key in configuration");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Invalid Key. Correct Key in configuration.");
                        }
                    } else if (Arrays.equals(new String("ERROR").getBytes(), response)) {
                        logger.warn("Authentication failed!");
                    } else {
                        logger.warn("Authentication reponse unexpected data length ({} instead of 72)!",
                                response.length);
                        logger.debug("Invalid Token. Correct Token in configuration");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Invalid Token. Correct Token in configuration.");
                    }
                }
            } catch (IOException e) {
                logger.warn("An IO error in doAuthentication has occured {}", e.getMessage());
            }
        }

        public void requestStatus(boolean polling) {
            if (polling) {
                CommandBase requestStatusCommand = new CommandBase();
                sendCommandAndMonitor(requestStatusCommand);
            }
        }

        public void sendCommandAndMonitor(CommandBase command) {
            sendCommand(command);
            mideaACHandler.resetDoPoll();
            disconnect();
            if (connectionMonitorJob == null) {
                scheduleConnectionMonitorJob();
            }
        }

        @SuppressWarnings("null")
        public void sendCommand(CommandBase command) {
            if (command instanceof CommandSet) {
                ((CommandSet) command).setPromptTone(config.getPromptTone());
            }
            Packet packet = new Packet(command, deviceId, mideaACHandler);
            packet.compose();

            if (!isConnected()) {
                logger.debug("Unable to send message; no connection to {}. starting over: {}", thing.getUID(), command);
                markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
            }

            try {
                byte[] bytes = packet.getBytes();
                logger.debug("Writing to {} at {} bytes.length: {}", thing.getUID(), ipAddress, bytes.length);

                if (getVersion() == 3) {
                    bytes = mideaACHandler.getSecurity().encode8370(bytes, MsgType.MSGTYPE_ENCRYPTED_REQUEST);
                }

                if (inputStream.available() == 0) {
                    logger.debug("Input stream empty sending write {}", command);
                    write(bytes);
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    logger.debug("An interupted error (retrycommand3) has occured {}", e.getMessage());
                }

                if (inputStream.available() == 0) {
                    logger.debug("Input stream empty sending second write {}", command);
                    write(bytes);
                }

                // Socket timeout will wait up to 2-10 seconds (as set) for bytes. Typically less than 1 second for me
                byte[] responseBytes = read();

                if (responseBytes != null) {
                    if (getVersion() == 3) {
                        Decryption8370Result result = mideaACHandler.getSecurity().decode8370(responseBytes);
                        for (byte[] response : result.getResponses()) {
                            logger.debug("Response length:{} thing:{} ", response.length, thing.getUID());
                            if (response.length > 40 + 16) {
                                byte[] data = mideaACHandler.getSecurity()
                                        .aesDecrypt(Arrays.copyOfRange(response, 40, response.length - 16)); // JO 16

                                // data[1]: BodyType
                                logger.trace("Bytes in HEX, decoded and with header: length: {}, data: {}", data.length,
                                        Utils.bytesToHex(data));
                                byte bodyType2 = data[0x1];

                                // data[3]: Device Type - 0xAC = AC
                                // https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea_devices.py#L96

                                // data[9]: MessageType - set, query, notify1, notify2, exception, querySN, exception2,
                                // querySubtype
                                // https://github.com/georgezhao2010/midea_ac_lan/blob/30d0ff5ff14f150da10b883e97b2f280767aa89a/custom_components/midea_ac_lan/midea/core/message.py#L22-L29
                                String responseType = "";
                                switch (data[0x9]) {
                                    case 0x02:
                                        responseType = "set";
                                        break;
                                    case 0x03:
                                        responseType = "query";
                                        break;
                                    case 0x04:
                                        responseType = "notify1";
                                        break;
                                    case 0x05:
                                        responseType = "notify2";
                                        break;
                                    case 0x06:
                                        responseType = "exception";
                                        break;
                                    case 0x07:
                                        responseType = "querySN";
                                        break;
                                    case 0x0A:
                                        responseType = "exception2";
                                        break;
                                    case 0x09: // Helyesen: 0xA0
                                        responseType = "querySubtype";
                                        break;
                                    default:
                                        logger.error("Invalid response type: {}", data[0x9]);
                                        // code block
                                }
                                logger.trace("Response Type: {} and bodyType:{}", responseType, data[0x1]);

                                // The response data from the appliance includes a packet header which we don't want
                                data = Arrays.copyOfRange(data, 10, data.length);
                                byte bodyType = data[0x0];
                                logger.trace("Response Type expected: {} and bodyType2:{}", responseType, bodyType2);
                                logger.trace("Bytes in HEX, decoded and stripped without header: length: {}, data: {}",
                                        data.length, Utils.bytesToHex(data));
                                logger.debug(
                                        "Bytes in BINARY, decoded and stripped without header: length: {}, data: {}",
                                        data.length, Utils.bytesToBinary(data));

                                if (data.length > 0) {
                                    if (data.length < 21) {
                                        logger.error("Response data is {} long minimum is 21!", data.length);
                                        return;
                                    }
                                    lastResponse = new Response(data, getVersion(), responseType, bodyType);
                                    try {
                                        if (bodyType != 30) {
                                            processMessage(lastResponse);
                                            logger.trace("data length is {} version is {} thing is {}", data.length,
                                                    version, thing.getUID());
                                        } else {
                                            logger.warn("invalid response received ignoring update from:{}",
                                                    thing.getUID());
                                            return;
                                        }
                                    } catch (Exception ex) {
                                        logger.warn("Error processing response: {}", ex.getMessage());
                                    }
                                }
                            }
                        }
                    } else {
                        byte[] data = mideaACHandler.getSecurity()
                                .aesDecrypt(Arrays.copyOfRange(responseBytes, 40, responseBytes.length - 16));
                        // The response data from the appliance includes a packet header which we don't want
                        logger.trace("V2 Bytes decoded with header: length: {}, data: {}", data.length,
                                Utils.bytesToHex(data));
                        if (data.length > 0) {
                            data = Arrays.copyOfRange(data, 10, data.length);
                            logger.trace("V2 Bytes decoded and stripped without header: length: {}, data: {}",
                                    data.length, Utils.bytesToHex(data));

                            lastResponse = new Response(data, getVersion(), "", (byte) 0x00);
                            processMessage(lastResponse);
                            logger.debug("V2 data length is {} version is {} thing is {}", data.length, version,
                                    thing.getUID());
                        } else {
                            logger.debug("Problem with reading V2 response, skipping command {}", command);
                            droppedCommands = droppedCommands + 1;
                            updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                        }
                    }
                    return;
                } else {
                    logger.debug("Problem with reading response, skipping command {}", command);
                    droppedCommands = droppedCommands + 1;
                    updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                    return;
                }
            } catch (SocketException e) {
                logger.debug("SocketException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            } catch (IOException e) {
                logger.debug(" Send IOException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }
        }

        protected synchronized void disconnect() {
            // Make sure writer, inputStream and socket are closed
            logger.debug("Disconnecting from {} at {}", thing.getUID(), ipAddress);

            try {
                if (writer != null) {
                    writer.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.warn("IOException closing connection to {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
            }
            // deviceIsConnected = false;
            socket = null;
            inputStream = null;
            writer = null;
            // markOffline();
        }

        private void updateChannel(String channelName, State state) {
            if (isOffline()) {
                return;
            }
            Channel channel = thing.getChannel(channelName);
            if (channel != null) {
                updateState(channel.getUID(), state);
            }
        }

        @SuppressWarnings("null")
        private void processMessage(@Nullable Response response) {
            updateChannel(CHANNEL_POWER, response.getPowerState() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_IMODE_RESUME, response.getImmodeResume() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TIMER_MODE, response.getTimerMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_APPLIANCE_ERROR, response.getApplianceError() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TARGET_TEMPERATURE, new QuantityType<Temperature>(response.getTargetTemperature(),
                    response.getTempUnit() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS)); // new
                                                                                           // DecimalType(response.getTargetTemperature()));
            updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getOperationalMode().toString()));
            updateChannel(CHANNEL_FAN_SPEED, new StringType(response.getFanSpeed().toString()));
            updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
            updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
            updateChannel(CHANNEL_SWING_MODE, new StringType(response.getSwingMode().toString()));
            updateChannel(CHANNEL_COZY_SLEEP, new DecimalType(response.getCozySleep()));
            updateChannel(CHANNEL_SAVE, response.getSave() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_LOW_FREQUENCY_FAN, response.getLowFrequencyFan() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SUPER_FAN, response.getSuperFan() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_FEEL_OWN, response.getFeelOwn() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CHILD_SLEEP_MODE, response.getChildSleepMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_EXCHANGE_AIR, response.getExchangeAir() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_DRY_CLEAN, response.getDryClean() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_AUX_HEAT, response.getAuxHeat() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_ECO_MODE, response.getEcoMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CLEAN_UP, response.getCleanUp() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TEMP_UNIT, response.getFahrenheit() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SLEEP_FUNCTION, response.getSleepFunction() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TURBO_MODE, response.getTurboMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_CATCH_COLD, response.getCatchCold() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_NIGHT_LIGHT, response.getNightLight() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_PEAK_ELEC, response.getPeakElec() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_NATURAL_FAN, response.getNaturalFan() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_INDOOR_TEMPERATURE, new QuantityType<Temperature>(response.getIndoorTemperature(),
                    response.getTempUnit() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
            updateChannel(CHANNEL_OUTDOOR_TEMPERATURE, new QuantityType<Temperature>(response.getOutdoorTemperature(),
                    response.getTempUnit() ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS));
            updateChannel(CHANNEL_HUMIDITY, new DecimalType(response.getHumidity()));
        }

        public synchronized byte @Nullable [] read() {
            byte[] bytes = new byte[512];

            if (inputStream == null) {
                logger.debug("No bytes to read");
                return null;
            }
            try {
                if (inputStream != null) {
                    int len = inputStream.read(bytes);
                    if (len > 0) {
                        logger.debug("Response received length: {} Thing:{}", len, thing.getUID());
                        bytes = Arrays.copyOfRange(bytes, 0, len);
                        return bytes;
                    }
                }
            } catch (IOException e) {
                String message = e.getMessage();
                logger.debug(" Byte read exception {}", message);
            }
            return null;
        }

        @SuppressWarnings("null")
        public synchronized void write(byte[] buffer) throws IOException {
            if (writer == null) {
                logger.warn("Writer for {} is null when trying to write to {}!!!", thing.getUID(), ipAddress);
                return;
            }

            try {
                writer.write(buffer, 0, buffer.length);
            } catch (IOException e) {
                String message = e.getMessage();
                logger.debug("Write error {}", message);
            }
        }

        @SuppressWarnings("null")
        private boolean isConnected() {
            return deviceIsConnected && !socket.isClosed() && socket.isConnected();
        }

        /*
         * Periodical polling.
         */
        @SuppressWarnings("null")
        private void scheduleConnectionMonitorJob() {
            if (connectionMonitorJob == null) {
                logger.debug("Starting connection monitor job in {} seconds for {} at {} after 30 second delay",
                        config.getPollingTime(), thing.getUID(), ipAddress);
                long frequency = config.getPollingTime();
                long delay = 30L;
                connectionMonitorJob = scheduler.scheduleWithFixedDelay(connectionMonitorRunnable, delay, frequency,
                        TimeUnit.SECONDS);
            }
        }

        @SuppressWarnings("null")
        private void cancelConnectionMonitorJob() {
            if (connectionMonitorJob != null) {
                logger.debug("Cancelling connection monitor job for {} at {}", thing.getUID(), ipAddress);
                connectionMonitorJob.cancel(true);
                connectionMonitorJob = null;
            }
        }
    }
}
