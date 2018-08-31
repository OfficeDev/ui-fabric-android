package com.microsoft.officeuifabric

import android.content.Context

/**
 * The [id] passed to this enum class references a dimens resource that [getDisplayValue] converts into an int.
 * This int specifies the layout width and height for the AvatarView.
 */
enum class AvatarSize(private val id: Int) {
    SIZE_18(R.dimen.avatar_size_18),
    SIZE_25(R.dimen.avatar_size_25),
    SIZE_30(R.dimen.avatar_size_30),
    SIZE_35(R.dimen.avatar_size_35),
    SIZE_40(R.dimen.avatar_size_40),
    SIZE_70(R.dimen.avatar_size_70);

    /**
     * This method uses [context] to convert the [id] resource into an int that becomes
     * AvatarView's layout width and height
     */
    fun getDisplayValue(context: Context): Int {
        return context.resources.getDimension(id).toInt()
    }
}