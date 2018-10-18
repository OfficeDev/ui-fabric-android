package com.microsoft.officeuifabric.persona

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri

interface IPersona {
    var name: String
    var email: String
    var subtitle: String
    var footer: String
    var avatarImageBitmap: Bitmap?
    var avatarImageDrawable: Drawable?
    var avatarImageResource: Int?
    var avatarImageUri: Uri?
}

data class Persona(override var name: String = "", override var email: String = "") : IPersona {
    override var subtitle: String = ""
    override var footer: String = ""
    override var avatarImageBitmap: Bitmap? = null
    override var avatarImageDrawable: Drawable? = null
    override var avatarImageResource: Int? = null
    override var avatarImageUri: Uri? = null
}