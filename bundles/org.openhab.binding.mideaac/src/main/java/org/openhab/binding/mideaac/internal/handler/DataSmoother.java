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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
public class DataSmoother {

    private final HashMap<String, Float> history = new HashMap<String, Float>();
    private final ArrayList<String> managed = new ArrayList<String>();

    public void setManaged(String channelName) {
        if (!managed.contains(channelName)) {
            managed.add(channelName);
        }
    }

    private boolean isManaged(String channelName) {
        return managed.contains(channelName);
    }

    public Float get(String channelName, Float value) {
        if (!isManaged(channelName)) {
            return value;
        }
        if (!history.containsKey(channelName)) {
            return value;
        } else {
            Float previousvalue = this.history.get(channelName); // JO added this.
            float avg = average(previousvalue, value);
            this.history.put(channelName, avg);
            return avg;
        }
    }

    public float average(float previousvalue, float value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Float.parseFloat(decimalFormat.format((previousvalue + value) / 2));
    }

    public DataSmoother() {
    }
}
