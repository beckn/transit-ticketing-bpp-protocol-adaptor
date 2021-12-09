package transit.ticketing.bpp.protocol.protocol.shared

import java.nio.ByteBuffer
import java.util.*

/** This class is to define all common functions or business logic which can be reused in the project */
object Util {
  /** Validate BaseUrl ends with slash or not
   *@param baseUrl String
   * @return baseUrl String
   **/
  fun getBaseUri(baseUrl: String): String {
    return if (baseUrl.endsWith("/", true)) baseUrl else "$baseUrl/"
  }

  fun uuidToBase64(str: String): String {
    val base64 = Base64.getEncoder()
    val uuid = UUID.fromString(str)
    val bb = ByteBuffer.wrap(ByteArray(16))
    bb.putLong(uuid.mostSignificantBits)
    bb.putLong(uuid.leastSignificantBits)
    return base64.encodeToString(bb.array())
  }

  fun uuidFromBase64(key: String): String? {
    val base64 = Base64.getDecoder()
    val bytes: ByteArray = base64.decode(key)
    val bb: ByteBuffer = ByteBuffer.wrap(bytes)
    val uuid = UUID(bb.getLong(), bb.getLong())
    return uuid.toString()
  }
}