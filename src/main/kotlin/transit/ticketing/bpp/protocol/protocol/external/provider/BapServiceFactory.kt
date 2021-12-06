package transit.ticketing.bpp.protocol.protocol.external.provider

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.retrofit.RetryCallAdapter
import io.github.resilience4j.retry.Retry
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.factories.RetryFactory
import transit.ticketing.bpp.protocol.protocol.shared.security.BppClientInterceptor
import transit.ticketing.bpp.protocol.protocol.shared.security.SignRequestInterceptor
import java.util.concurrent.TimeUnit

@Service
class BapServiceFactory @Autowired constructor(
  val objectMapper: ObjectMapper,
  @Value("\${bpp_service.retry.max_attempts}")
  private val maxAttempts: Int,
  @Value("\${bpp_service.retry.initial_interval_in_millis}")
  private val initialIntervalInMillis: Long,
  @Value("\${bpp_service.retry.interval_multiplier}")
  private val intervalMultiplier: Double,
  @Value("\${transit.security.enabled}") val enableSecurity: Boolean,
  private val interceptor: SignRequestInterceptor,
  @Value("\${bpp_service.timeouts.connection_in_seconds}") private val connectionTimeoutInSeconds: Long,
  @Value("\${bpp_service.timeouts.read_in_seconds}") private val readTimeoutInSeconds: Long,
  @Value("\${bpp_service.timeouts.write_in_seconds}") private val writeTimeoutInSeconds: Long,

  ) {
  @Cacheable("bapClients")
  fun getBapClient(bapUri: String): BapServiceClient {
    val url : String = Util.getBaseUri(bapUri)
    val retrofit = Retrofit.Builder()
      .baseUrl(url)
      .client(buildHttpClient())
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(getRetryConfig(bapUri)))
      .build()
    return retrofit.create(BapServiceClient::class.java)
  }

  private fun buildHttpClient(): OkHttpClient {
    val httpClientBuilder = OkHttpClient.Builder()
      .connectTimeout(connectionTimeoutInSeconds, TimeUnit.SECONDS)
      .readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
      .writeTimeout(writeTimeoutInSeconds, TimeUnit.SECONDS)
    if (enableSecurity) {
      httpClientBuilder.addInterceptor(interceptor)
    }
    return httpClientBuilder.build()
  }

  private fun getRetryConfig(bppUri: String): Retry {
    return RetryFactory.create(
      bppUri,
      maxAttempts,
      initialIntervalInMillis,
      intervalMultiplier
    )
  }
}