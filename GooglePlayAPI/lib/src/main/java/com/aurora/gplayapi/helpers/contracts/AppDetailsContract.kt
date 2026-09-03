/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.contracts

import com.aurora.gplayapi.data.models.App

interface AppDetailsContract {
    fun getAppByPackageName(packageName: String): App
    fun getAppByPackageName(packageNames: List<String>): List<App>
}
