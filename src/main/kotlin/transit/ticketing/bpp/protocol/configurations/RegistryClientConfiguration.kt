package transit.ticketing.bpp.protocol.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.retrofit.RetryCallAdapter
import io.github.resilience4j.retry.Retry
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.RegistryClient
import transit.ticketing.bpp.protocol.protocol.shared.factories.RetryFactory
import transit.ticketing.bpp.protocol.protocol.shared.security.SignRequestInterceptor


@Configuration
class RegistryClientConfiguration(
  @Autowired @Value("\${registry_service.url}")
  private val registryServiceUrl: String,
  @Value("\${registry_service.retry.max_attempts}")
  private val maxAttempts: Int,
  @Value("\${registry_service.retry.initial_interval_in_millis}")
  private val initialIntervalInMillis: Long,
  @Value("\${registry_service.retry.interval_multiplier}")
  private val intervalMultiplier: Double,
  @Autowired @Value("\${bpp_registry_service.url}")
  private val bppRegistryServiceUrl: String,
  @Autowired
  private val objectMapper: ObjectMapper,
  @Autowired
  private val interceptor: SignRequestInterceptor
) {
  private val retry: Retry = RetryFactory.create(
    "RegistryClient",
    maxAttempts,
    initialIntervalInMillis,
    intervalMultiplier
  )

  @Bean
  @Primary
  fun registryServiceClient(): RegistryClient {
    val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
    val retrofit = Retrofit.Builder()
      .baseUrl(registryServiceUrl)
      .client(okHttpClient)
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(retry))
      .build()

    return retrofit.create(RegistryClient::class.java)
  }

  @Bean(BPP_REGISTRY_SERVICE_CLIENT)
  fun bppRegistryServiceClient(): RegistryClient {
    val retrofit = Retrofit.Builder()
      .baseUrl(bppRegistryServiceUrl)
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RetryCallAdapter.of(retry))
      .build()

    return retrofit.create(RegistryClient::class.java)
  }

  companion object {
    const val BPP_REGISTRY_SERVICE_CLIENT = "bppRegistryServiceClient"

  }
}