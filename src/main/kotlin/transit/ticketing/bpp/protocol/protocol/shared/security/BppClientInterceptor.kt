package transit.ticketing.bpp.protocol.protocol.shared.security

import okhttp3.Interceptor
import okhttp3.Response
import org.springframework.stereotype.Component

const val HEADER_NAME = "X-API-KEY"

@Component
class BppClientInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val request = original.newBuilder()
      .header(HEADER_NAME, "a8d6ge7d-5tsa-8d9c-m3b2-30e21c0e9564")
      .header(Authorization.ACCEPT,"application/json")
      .build()
    return chain.proceed(request)
  }
}