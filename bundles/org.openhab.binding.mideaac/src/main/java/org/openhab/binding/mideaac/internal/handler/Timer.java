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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * On Off Timer Channels.
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class Timer {
    private boolean status;
    private int hours;
    private int minutes;

    public Timer(boolean status, int hours, int minutes) {
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }

    public String toString() {
        if (status) {
            return String.format("enabled: %s, hours: %d, minutes: %d", status, hours, minutes);
        } else {
            return String.format("enabled: %s", status);
        }
    }

    public String toChannel() {
        if (status) {
            return String.format("%02d:%02d", hours, minutes);
        } else {
            return "";
        }
    }
}
