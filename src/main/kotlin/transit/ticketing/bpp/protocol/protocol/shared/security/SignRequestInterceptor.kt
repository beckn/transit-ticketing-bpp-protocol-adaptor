package transit.ticketing.bpp.protocol.protocol.shared.security

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SignRequestInterceptor @Autowired constructor(
  @Value("\${security.self.private_key}") private val b64PrivateKey: String,
  @Value("\${security.self.unique_key_id}") private val uniqueKeyId: String,
  @Value("\${context.bpp_id}") private val subscriberId: String,
  @Value("\${context.ttl_seconds}") private val ttlInSeconds: String,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val authorization = createAuthorization(original)
    val request = original.newBuilder()
      .header(Authorization.HEADER_NAME, authorization.headerString)
      .header(Authorization.ACCEPT,authorization.Accept)
      .build()
    return chain.proceed(request)
  }

  private fun getBodyContent(request: RequestBody): String {
    val buffer = Buffer()
    request.writeTo(buffer)
    return buffer.readUtf8()
  }

  private fun createAuthorization(request: Request): Authorization {
    val now = Instant.now()
    val created = now.epochSecond
    val expires = now.plusSeconds(ttlInSeconds.toLong()).epochSecond
    val bodyContent = getBodyContent(request.body()!!)
    return Authorization.create(
      subscriberId = subscriberId,
      uniqueKeyId = uniqueKeyId,
      signature = Cryptic.sign(b64PrivateKey, bodyContent, created, expires),
      created = created,
      expires = expires
    )

  }


}