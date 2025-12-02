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
import org.openhab.binding.mideaac.internal.handler.capabilities.CapabilitiesResponse;
import org.openhab.binding.mideaac.internal.responses.A1Response;
import org.openhab.binding.mideaac.internal.responses.EnergyResponse;
import org.openhab.binding.mideaac.internal.responses.HumidityResponse;
import org.openhab.binding.mideaac.internal.responses.Response;
import org.openhab.binding.mideaac.internal.responses.TemperatureResponse;

/**
 * 
 * @author Leo Siepel - Initial contribution
 * @author Bob Eckhoff - added additional Callbacks after Response
 */
@NonNullByDefault
public interface HumidifierCallback extends Callback {
    /**
     * Updates dehumidifier channels with (0xC8) response .
     *
     * @param a1Response The humidifier (0xC8) response from the device used to update properties.
     */
    void updateChannels(A1Response a1Response);

    default void updateChannels(Response response) {
        // No implementation needed for humidifier
    }

    default void updateChannels(CapabilitiesResponse capabilitiesResponse) {
        // No implementation needed for humidifier
    }

    default void updateChannels(EnergyResponse energyResponse) {
        // No implementation needed for humidifier
    }

    default void updateHumidityFromEnergy(EnergyResponse energyResponse) {
        // No implementation needed for humidifier
    }

    default void updateChannels(HumidityResponse humidityResponse) {
        // No implementation needed for humidifier
    }

    default void updateChannels(TemperatureResponse temperatureResponse) {
        // No implementation needed for humidifier
    }
}
