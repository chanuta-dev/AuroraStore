/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.contracts

import com.aurora.gplayapi.data.models.Category

interface CategoryContract {
    fun getAllCategories(type: Category.Type): List<Category>
}
