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

package org.openhab.binding.mideaac.internal.connection;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.mideaac.internal.callbacks.Callback;
import org.openhab.binding.mideaac.internal.connection.exception.MideaAuthenticationException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaConnectionException;
import org.openhab.binding.mideaac.internal.connection.exception.MideaException;
import org.openhab.binding.mideaac.internal.devices.CommandBase;
import org.openhab.binding.mideaac.internal.devices.cc.CCQueryCommand;

/**
 * The {@link ConnectionManagerTest} tests that the CC device command is sent, not
 * the command Base (used for AC and A1)
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ConnectionManagerTest {

    @Test
    public void testGetCCStatusSendsQueryCommand()
            throws MideaConnectionException, MideaAuthenticationException, MideaException, IOException {
        Callback mockCallback = Mockito.mock(Callback.class);

        // Spy with dummy constructor args
        ConnectionManager manager = Mockito.spy(new ConnectionManager("127.0.0.1", 8888, 5, "dummyKey", "dummyToken",
                "dummyCloud", "test@example.com", "password", "device123", 1, false));

        // Stub sendCommand so it does nothing (avoids real network call)
        Mockito.doNothing().when(manager).sendCommand(Mockito.any(CommandBase.class), Mockito.any(Callback.class));

        // Act
        manager.getCCStatus(mockCallback);

        // Assert: verify sendCommand was called with a QueryCommand
        Mockito.verify(manager).sendCommand(Mockito.any(CCQueryCommand.class), Mockito.eq(mockCallback));
    }
}
