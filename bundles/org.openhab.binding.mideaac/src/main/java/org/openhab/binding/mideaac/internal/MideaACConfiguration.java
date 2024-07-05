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
package org.openhab.binding.mideaac.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MideaACConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class MideaACConfiguration {
    /**
     * IP Address of the device.
     */
    private String ipAddress = "";

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    private String ipPort = "";

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    private String deviceId = "";

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private String email = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private int reauth;

    public Integer getReauth() {
        return reauth;
    }

    public void setReauth(Integer reauth) {
        this.reauth = reauth;
    }

    private String password = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String cloud = "";

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.token = cloud;
    }

    private String token = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String key = "";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private int pollingTime;

    public int getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(int pollingTime) {
        this.pollingTime = pollingTime;
    }

    private int timeout;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private boolean promptTone;

    public boolean getPromptTone() {
        return promptTone;
    }

    public void setPromptTone(boolean promptTone) {
        this.promptTone = promptTone;
    }

    public boolean isValid() {
        return !("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank());
    }

    public boolean isDiscoveryNeeded() {
        return ("0".equalsIgnoreCase(deviceId) || deviceId.isBlank() || ipPort.isBlank() || ipAddress.isBlank()
                || !Utils.validateIP(ipAddress));
    }
}
