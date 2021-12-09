package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.flatMap
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.errors.database.DatabaseError
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.mappers.GenericResponseMapper
import transit.ticketing.bpp.protocol.message.services.ResponseStorageService
import transit.ticketing.bpp.protocol.protocol.discovery.services.SearchService
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.Util
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService

@Service
class ConfirmService @Autowired constructor(
    private val registryService: RegistryService,
    private val bppClientConfirmService: BppClientConfirmService,
    private val bapOnConfirmService: BapOnConfirmService,
    val repository: ResponseStorageService<ProtocolOnConfirm, OnConfirmDao>,
    val mapper: GenericResponseMapper<ProtocolOnConfirm, OnConfirmDao>,
) {
    val log: Logger = LoggerFactory.getLogger(SearchService::class.java)

    fun postConfirmRequest(
        context: ProtocolContext,
        message: ProtocolConfirmRequestMessage
    ): Either<HttpError, ProtocolOnConfirm> {
        log.info("Confirm Service : Got init request with message: {} ", message)
        var subscriber: SubscriberDto? = null
        if (message?.order == null ||
            message.order.items.isNullOrEmpty()
        ) {
            log.info("Empty order received, no op. Order: {}", message)
            return Either.Left(BppError.BadRequestError)
        }
        if(message.order.fulfillment?.id!=null){
            val data = Util.uuidFromBase64(message.order.fulfillment.id)
            return Either.Left(BppError.BadRequestError)
        }
        return registryService
            .lookupBapById(context.bapId!!)
            .flatMap { subscriberInfo ->
                if (!subscriberInfo.isNullOrEmpty()) {
                    subscriber = subscriberInfo.first()
                }
                if (context.transactionId != null) {
                    repository.findById(context.transactionId).fold(
                        {
                            // No info available for this Transaction Id in DB
                            bppClientConfirmService.blockTicket(null, context, message).fold(
                                {
                                    // Failed to block ticket in Bpp
                                    log.error("Confirm Service : Failed to block ticket in Bpp")
                                    return Either.Left(it)
                                }, {
                                    // Received Response from Block ticket with message
                                    log.info("Confirm Service : Received Response from Block ticket with message")

                                    return updateOrder(it)
                                }
                            )
                        },
                        {
                            log.info("Confirm Service : Received Response from Db with message")
                            return Either.Right(it!!)
                        }
                    )
                } else {
                    log.error("Confirm Service : No Transaction Id available")
                    return Either.Left(BppError.BadRequestError)
                }
            }
    }

    fun updateOrder(protocolOnConfirm: ProtocolOnConfirm): Either<DatabaseError, ProtocolOnConfirm> {
        return if (protocolOnConfirm.context?.transactionId == null) {
            log.error("Confirm Service :Transaction id is not available")
            Either.Left(DatabaseError.NotFound)
        } else {
            var dataDao = mapper.protocolToEntity(protocolOnConfirm)
            log.info("Confirm Service : Updating db on confirm callback")
            return repository.updateDocByQuery(
                OnConfirmDao::context / ProtocolContext::transactionId eq protocolOnConfirm.context.transactionId,
                dataDao
            )
        }
    }

}
