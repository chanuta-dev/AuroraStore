/*
 * SPDX-FileCopyrightText: 2020-2021 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Util {

    public static Map<String, String> parseResponse(byte[] response) {
        final Map<String, String> keyValueMap = new HashMap<>();
        final StringTokenizer stringTokenizer = new StringTokenizer(new String(response), "\n\r");
        while (stringTokenizer.hasMoreTokens()) {
            final String[] keyValue = stringTokenizer.nextToken().split("=", 2);
            if (keyValue.length >= 2) {
                keyValueMap.put(keyValue[0], keyValue[1]);
            }
        }
        return keyValueMap;
    }

    public static long parseLong(String intAsString, long defaultValue) {
        try {
            return Long.parseLong(intAsString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
