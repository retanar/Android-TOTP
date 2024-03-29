package retanar.totp_android.domain.usecases

import retanar.totp_android.domain.crypto.SecretEncryptor
import retanar.totp_android.domain.entities.EncryptedTotpKey
import retanar.totp_android.domain.repository.TotpKeyRepository
import java.security.SecureRandom

class AddNewTotpUseCase(
    private val repository: TotpKeyRepository,
    private val encryptor: SecretEncryptor,
) {
    suspend operator fun invoke(plainSecret: ByteArray, name: String) {
        if (plainSecret.isEmpty())
            throw IllegalArgumentException("Secret cannot be empty")
        val random = SecureRandom()
        val iv = ByteArray(encryptor.ivSize)
        random.nextBytes(iv)
        repository.addKey(EncryptedTotpKey(0, name, encryptor.encrypt(plainSecret, iv), iv))
    }
}