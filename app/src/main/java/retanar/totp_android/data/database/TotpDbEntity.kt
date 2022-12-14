package retanar.totp_android.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Set `id` to 0 to autogenerate id by the database */
@Entity(tableName = totpTableName)
data class TotpDbEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val secret: ByteArray,
    val iv: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TotpDbEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (!secret.contentEquals(other.secret)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + secret.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
