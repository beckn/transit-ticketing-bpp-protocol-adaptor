package transit.ticketing.bpp.protocol.protocol.status.services

import arrow.core.Either
import arrow.core.flatMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils.hasText
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOrderStatusRequestMessage
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class StatusService @Autowired constructor(
  private val registryService: RegistryService,
  private val bppClientStatusService: BppClientStatusService
) {
  val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

  fun postOrderRequest(context: ProtocolContext, message: ProtocolOrderStatusRequestMessage): Either<HttpError, ProtocolAckResponse> {
    log.info("Got init request with message: {} ", message)
    if (message?.orderId == null) {
      log.info("Empty order received,no order: {}", message)
      return Either.Left(BppError.BadRequestError)
    }
    return registryService
        .lookupBapById(context.bapId!!)
        .flatMap {
            bppClientStatusService.postStatus(it.first(), context, message)
        }.flatMap {
            Either.Right(ProtocolAckResponse(context, ResponseMessage.ack()))
        }
  }

  private fun isBppFilterSpecified(context: ProtocolContext) =
    hasText(context.bppId)

}
