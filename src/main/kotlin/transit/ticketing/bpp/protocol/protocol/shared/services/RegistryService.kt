package transit.ticketing.bpp.protocol.protocol.shared.services

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import retrofit2.Response
import transit.ticketing.bpp.protocol.configurations.RegistryClientConfiguration.Companion.BPP_REGISTRY_SERVICE_PROTOCOL
import transit.ticketing.bpp.protocol.errors.registry.RegistryLookupError
import transit.ticketing.bpp.protocol.protocol.external.domains.Subscriber
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.registry.RegistryClient
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberLookupRequest


@Service
class RegistryService(
  @Autowired private val registryServiceClient: RegistryClient,
  @Qualifier(BPP_REGISTRY_SERVICE_PROTOCOL) @Autowired private val bppRegistryServiceClient: RegistryClient,
  @Value("\${context.domain}") private val domain: String,
  @Value("\${context.city}") private val city: String,
  @Value("\${context.country}") private val country: String
) {
  private val log: Logger = LoggerFactory.getLogger(RegistryService::class.java)

  @Cacheable(CacheName.bapById)
  fun lookupBapById(id: String): Either<RegistryLookupError, List<SubscriberDto>> {
    return lookup(bppRegistryServiceClient, lookupRequest(subscriberType = Subscriber.Type.BAP, subscriberId = id))
  }


  @Cacheable(CacheName.gateways)
  fun lookupGateways(): Either<RegistryLookupError, List<SubscriberDto>> {
    return lookup(registryServiceClient, lookupRequest(subscriberType = Subscriber.Type.BG))
  }

  private fun lookup(
    client: RegistryClient,
    request: SubscriberLookupRequest
  ): Either<RegistryLookupError, List<SubscriberDto>> {
    return Either.catch {
      log.info("Looking up subscribers: {}", request)
      val httpResponse = client.lookup(request).execute()
      log.info("Lookup subscriber response. Status: {}, Body: {}", httpResponse.code(), httpResponse.body())
      return when {
        httpResponse.isInternalServerError() -> Left(RegistryLookupError.Internal)
        noSubscribersFound(httpResponse) ->Right(
         listOf()
        )//Left(RegistryLookupError.NoSubscriberFound)
        else -> Right(
          httpResponse.body()!!
        )
      }
    }.mapLeft {
      log.error("Error when looking up subscribers", it)
      RegistryLookupError.Internal
    }
  }

  private fun lookupRequest(subscriberType: Subscriber.Type, subscriberId: String? = null) = SubscriberLookupRequest(
    subscriber_id = subscriberId,
    type = subscriberType,
    domain = domain,
    city = city,
    country = country
  )

  private fun noSubscribersFound(httpResponse: Response<List<SubscriberDto>>) =
    httpResponse.body() == null || httpResponse.body()?.isEmpty() == true

  object CacheName {
    const val gateways = "gateways"
    const val bapById = "bapById"
  }
}
