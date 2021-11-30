package transit.ticketing.bpp.protocol.protocol.shared.security

import java.time.Instant

data class Authorization(
  val keyId: String,
  val algorithm: String = ED25519,
  val created: Long,
  val expires: Long,
  val headers: String = "(created) (expires) digest",
  val signature: String,
  val Accept :String = "application/json"
) {

  fun isNotExpired() = Instant.now().toEpochMilli() / 1000 < expires

  val headerString by lazy {
    """Signature keyId="$keyId",algorithm="$algorithm",created="$created",expires="$expires",headers="$headers",signature="$signature""""
  }

  val parseKey by lazy {
    val keyComponents = keyId.trim().split("|")
    Triple(keyComponents[0], keyComponents[1], keyComponents[2])
  }

  companion object {
    private const val KEY_ID = "keyId"
    private const val ALGORITHM = "algorithm"
    private const val CREATED = "created"
    private const val EXPIRES = "expires"
    private const val HEADERS = "headers"
    private const val SIGNATURE = "signature"
    private const val ED25519 = "ed25519"
    const val HEADER_NAME = "Authorization"
    const val ACCEPT = "Accept"

    fun parse(auth: String?): Authorization? {
      try{
        return auth?.let { authStr ->
          val authParams = authStr.trim()
            .removePrefix("Signature ")
            .split(",")
            .associate {
              val keyValue = it.trim().split("=\"", limit = 2)
              Pair(keyValue[0], keyValue[1].removeSuffix("\""))
            }

          val keyId = authParams[KEY_ID] ?: return null
          val algorithm = authParams[ALGORITHM] ?: return null
          val created = authParams[CREATED]?.toLong() ?: return null
          val expires = authParams[EXPIRES]?.toLong() ?: return null
          val headers = authParams[HEADERS] ?: return null
          val signature = authParams[SIGNATURE] ?: return null

          Authorization(
            keyId = keyId,
            algorithm = algorithm,
            created = created,
            expires = expires,
            headers = headers,
            signature = signature
          )
        }
      }catch (e: Exception){
        return  null
      }
    }

    fun create(subscriberId: String,
               uniqueKeyId: String,
               signature: String,
               created: Long,
               expires: Long): Authorization {
      return Authorization(
        keyId = "$subscriberId|$uniqueKeyId|$ED25519",
        created = created,
        expires =  expires,
        signature = signature
      )
    }
  }
}