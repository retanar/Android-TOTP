package retanar.totp_android.domain.entities

data class TotpKey(
    val name: String,
//    Todo: Maybe a second identifier variable
//    val account: String,
    val secret: ByteArray,
)
