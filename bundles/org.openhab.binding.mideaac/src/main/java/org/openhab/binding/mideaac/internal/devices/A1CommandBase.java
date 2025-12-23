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
package org.openhab.binding.mideaac.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link A1CommandBase} builds the base command frame for A1-style devices.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1CommandBase extends CommandBase {
    /**
     * Base class for A1-style commands
     */
    public A1CommandBase() {
        super(); // build the AC-style base frame
        data[2] = (byte) 0xa1; // override device type byte
    }
}
