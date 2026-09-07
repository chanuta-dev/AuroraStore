/*
 * SPDX-FileCopyrightText: 2021-2025 Rahul Kumar Patel <whyorean@gmail.com>
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.util

import android.content.Context
import androidx.preference.PreferenceManager

object Preferences {
    const val PREFERENCE_INTRO = "PREFERENCE_INTRO"
    const val PREFERENCE_INCLUDE_BETA_UPDATES = "PREFERENCE_INCLUDE_BETA_UPDATES"

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue)
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply()
    }
}
