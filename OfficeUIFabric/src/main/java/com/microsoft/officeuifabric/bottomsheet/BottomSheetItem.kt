/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.microsoft.officeuifabric.bottomsheet

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes

class BottomSheetItem : Parcelable {
    val id: Int
    @DrawableRes
    val imageId: Int
    val title: String
    val subtitle: String

    @JvmOverloads
    constructor(id: Int, @DrawableRes imageId: Int, title: String, subtitle: String = "") {
        this.id = id
        this.imageId = imageId
        this.title = title
        this.subtitle = subtitle
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        imageId = parcel.readInt(),
        title = parcel.readString() ?: "",
        subtitle = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(imageId)
        parcel.writeString(title)
        parcel.writeString(subtitle)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BottomSheetItem> {
        override fun createFromParcel(source: Parcel): BottomSheetItem = BottomSheetItem(source)
        override fun newArray(size: Int): Array<BottomSheetItem?> = arrayOfNulls(size)
    }
}