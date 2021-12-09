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
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class StatusService @Autowired constructor(
    private val registryService: RegistryService,
    val repository: ResponseStorageService<ProtocolOnOrderStatus, OnOrderStatusDao>,
) {
    val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

    fun getOrderStatusRequest(
        context: ProtocolContext,
        message: ProtocolOrderStatusRequestMessage
    ): Either<HttpError, ProtocolOnOrderStatus> {
        log.info("Got init request with message: {} ", message)
        return if (message?.orderId == null) {
            log.info("Empty order received,no order: {}", message)
            Either.Left(BppError.BadRequestError)
        } else {
            registryService
                .lookupBapById(context.bapId!!)
                .flatMap {
                    repository.findById(context.transactionId)
                }
        }
    }
}
