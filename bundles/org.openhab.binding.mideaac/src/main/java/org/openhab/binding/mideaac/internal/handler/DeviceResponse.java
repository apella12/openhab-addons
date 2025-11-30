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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.Timer.TimerData;

/**
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public interface DeviceResponse {
    boolean getPowerState(); // byte 1

    FanSpeed getFanSpeed(); // byte 3

    TimerData getOnTimerData(); // byte 4

    TimerData getOffTimerData(); // byte 5

    Timer getOnTimer(); // byte 4

    Timer getOffTimer(); // byte 5

    float getIndoorTemperature();
}
