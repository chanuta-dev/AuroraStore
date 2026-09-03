/*
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.sampleapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AuthHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Properties

class MainActivityViewModel : ViewModel() {

    private val _authData: MutableStateFlow<AuthData?> = MutableStateFlow(null)
    val authData = _authData.asStateFlow()

    fun buildAuthData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _authData.value = AuthHelper.build(
                email = BuildConfig.GPLAY_API_EMAIL,
                token = BuildConfig.GPLAY_API_TOKEN,
                tokenType = AuthHelper.Token.AAS,
                properties = Properties().apply {
                    load(context.resources.openRawResource(com.aurora.gplayapi.R.raw.gplayapi_px_9a))
                }
            )
        }
    }

    fun doSomething(context: Context) {
        // Run the thing you want to test here!
    }
}
