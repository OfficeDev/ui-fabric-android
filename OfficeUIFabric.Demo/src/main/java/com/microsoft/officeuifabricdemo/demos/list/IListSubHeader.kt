/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabricdemo.demos.list

/**
 * This defines data associated with [ListSubHeaderView]s.
 */
interface IListSubHeader : IBaseListItem

data class ListSubHeader(override var title: String = "") : IListSubHeader