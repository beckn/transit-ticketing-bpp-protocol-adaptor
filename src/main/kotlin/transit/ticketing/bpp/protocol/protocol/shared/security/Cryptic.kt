package transit.ticketing.bpp.protocol.protocol.shared.security

import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.jcajce.provider.digest.Blake2b
import java.security.MessageDigest
import java.util.*

object Cryptic {

  fun sign(
    b64PrivateKey: String,
    requestBody: String,
    created: Long,
    expires: Long
  ): String {
    val signer = getEd25519SignerForSigning(b64PrivateKey)
    val formattedRequest = formatBodyForSigning(created, expires, requestBody)
    signer.update(formattedRequest.toByteArray(), 0, formattedRequest.length)
    return Base64.getEncoder().encodeToString(signer.generateSignature())
  }

  fun verify(
    authorization: Authorization,
    b64PublicKey: String,
    requestBody: String
  ): Boolean {
    val signer = getEd25519SignerForVerification(b64PublicKey)
    val formattedRequest = formatBodyForSigning(authorization.created, authorization.expires, requestBody)
    signer.update(formattedRequest.toByteArray(), 0, formattedRequest.length)
    return signer.verifySignature(Base64.getDecoder().decode(authorization.signature))
  }

  private fun getEd25519SignerForVerification(b64PublicKey: String): Signer {
    val publicKey = Base64.getDecoder().decode(b64PublicKey)
    val cipherParams = Ed25519PublicKeyParameters(publicKey, 0)
    val sv = Ed25519Signer()
    sv.init(false, cipherParams)
    return sv
  }

  private fun getEd25519SignerForSigning(b64PrivateKey: String): Signer {
    val privateKey = Base64.getDecoder().decode(b64PrivateKey)
    val cipherParams = Ed25519PrivateKeyParameters(privateKey, 0)
    val sv = Ed25519Signer()
    sv.init(true, cipherParams)
    return sv
  }

  private fun formatBodyForSigning(
    created: Long,
    expires: Long,
    requestBody: String
  ): String = "(created): $created\n(expires): $expires\ndigest: BLAKE-512=${blakeHash(requestBody)}"


  private fun blakeHash(requestBody: String): String {
    val digest: MessageDigest = Blake2b.Blake2b512()
    digest.reset()
    digest.update(requestBody.toByteArray())
    val hash: ByteArray = digest.digest()
    //val hex: String = Hex.toHexString(hash)
    return Base64.getEncoder().encodeToString(hash)
  }
}






