/*
 * SPDX-FileCopyrightText: 2023-2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

import com.aurora.gplayapi.utils.dig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal object RpcBuilder {

    private val gson = Gson()

    fun wrapResponse(input: String): HashMap<String, HashMap<String, Any>> {
        val lines = input.lines()
        val filteredLines: MutableList<Any> = mutableListOf()
        val result = HashMap<String, HashMap<String, Any>>()

        val wrbFrames = lines
            .filter { it.startsWith("[[\"wrb.fr") }
            .map { parseJaggedString(it) }

        if (wrbFrames.isEmpty()) return hashMapOf()

        wrbFrames.first().forEach {
            if (it.dig<String>(0) == "wrb.fr") {
                filteredLines.add(it)
            }
        }

        filteredLines
            .forEach {
                val (type, packageName) = (it.dig<String>(6)).toString().split("@")
                val rpcData = it.dig<String>(2)

                if (result[type] == null) {
                    result[type] = hashMapOf()
                }

                result[type]?.put(packageName, parseJaggedString(rpcData))
            }

        return result
    }

    private fun parseJaggedString(input: String): List<Any> {
        if (input == "null" || input.isEmpty()) {
            return emptyList()
        }

        val type = object : TypeToken<List<Any>>() {}.type
        return gson.fromJson(input, type)
    }
}
