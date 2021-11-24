package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.flatMap
import org.beckn.protocol.schemas.ProtocolAckResponse
import org.beckn.protocol.schemas.ProtocolContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.protocol.shared.dtos.SearchCriteria
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class SearchService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppClientSearchService: BppClientSearchService
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun search(context: ProtocolContext, criteria: SearchCriteria): Either<HttpError, ProtocolAckResponse> {
    log.info("Got search request with criteria: {} ", criteria)
    return registryService
      .lookupBppById(context.bppId!!)
      .flatMap {
        bppClientSearchService.search(
          it.first(),
          context,
         criteria
        )
      }
  }

  private fun isBppFilterSpecified(context: ProtocolContext) =
    hasText(context.bppId)

}
