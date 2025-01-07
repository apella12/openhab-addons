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
package org.openhab.binding.fronius.internal.api.dto.meter;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link MeterRealtimeDetails} is responsible for storing
 * the "Details" node of the {@link MeterRealtimeBodyData}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MeterRealtimeDetails {
    @SerializedName("Manufacturer")
    private String manufacturer;
    @SerializedName("Model")
    private String model;
    @SerializedName("Serial")
    private String serial;

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getSerial() {
        return serial;
    }
}
