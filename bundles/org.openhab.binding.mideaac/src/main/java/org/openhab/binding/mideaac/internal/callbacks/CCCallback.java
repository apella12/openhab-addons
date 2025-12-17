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
package org.openhab.binding.mideaac.internal.callbacks;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.devices.a1.A1Response;
import org.openhab.binding.mideaac.internal.devices.ac.EnergyResponse;
import org.openhab.binding.mideaac.internal.devices.ac.HumidityResponse;
import org.openhab.binding.mideaac.internal.devices.ac.Response;
import org.openhab.binding.mideaac.internal.devices.ac.TemperatureResponse;
import org.openhab.binding.mideaac.internal.devices.capabilities.CapabilitiesResponse;
import org.openhab.binding.mideaac.internal.devices.cc.CCResponse;

/**
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public interface CCCallback extends Callback {

    /**
     * Updates channels with a commercial AC (0x01) response.
     *
     * @param response The a commercial AC (0x01) response from the device used to update properties.
     */
    @Override
    void updateChannels(CCResponse ccResponse);

    default void updateChannels(A1Response a1Response) {
        // No implementation needed for Commercial AC
    }

    default void updateChannels(Response response) {
        // No implementation needed for Commercial AC
    }

    default void updateChannels(CapabilitiesResponse capabilitiesResponse) {
        // No implementation needed for Commercial AC
    }

    default void updateChannels(EnergyResponse energyResponse) {
        // No implementation needed for Commercial AC
    }

    default void updateHumidityFromEnergy(EnergyResponse energyResponse) {
        // No implementation needed for Commercial AC
    }

    default void updateChannels(HumidityResponse humidityResponse) {
        // No implementation needed for Commercial AC
    }

    default void updateChannels(TemperatureResponse temperatureResponse) {
        // No implementation needed for Commercial AC
    }
}
