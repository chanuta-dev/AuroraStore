/*
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.sampleapp.ui.theme.GPlayApiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPlayApiTheme {
                Scaffold {
                    val context = LocalContext.current
                    LaunchedEffect(key1 = Unit) { viewModel.buildAuthData(context) }

                    Column(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        val authData: AuthData? by viewModel.authData.collectAsStateWithLifecycle()

                        if (authData != null) {
                            authData?.let { data ->
                                AsyncImage(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .requiredSize(192.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    model = data.userProfile?.artwork?.url,
                                    contentDescription = null
                                )

                                Text(
                                    modifier = Modifier.padding(vertical = 20.dp),
                                    text = data.email
                                )
                            }

                            Button(onClick = { viewModel.doSomething(context) }) {
                                Text(text = "Do something!")
                            }
                        } else {
                            CircularProgressIndicator(modifier = Modifier.requiredSize(100.dp))
                        }
                    }
                }
            }
        }
    }
}
