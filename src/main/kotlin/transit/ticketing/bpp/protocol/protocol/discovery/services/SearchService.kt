package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.flatMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolIntent
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ResponseMessage
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class SearchService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppClientSearchService: BppClientSearchService
) {
  val log: Logger = LoggerFactory.getLogger(StatusService::class.java)

  fun search(context: ProtocolContext, intent: ProtocolIntent): Either<HttpError, ProtocolAckResponse> {
    log.info("Got search request with intent: {} ", intent)
    lateinit var subscriber :SubscriberDto
      return registryService
        .lookupBppById(context.bppId!!)
        .flatMap {
          subscriber=   it.first()
            bppClientSearchService.search(subscriber, context, intent)
        }.flatMap {
          bppClientSearchService.onSearch(subscriber, it)
        }
  }

  private fun isBppFilterSpecified(context: ProtocolContext) =
    hasText(context.bppId)

}
