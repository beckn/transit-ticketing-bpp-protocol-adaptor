package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.registry.RegistryLookupError
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolIntent
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage
import transit.ticketing.bpp.protocol.protocol.shared.services.RegistryService
import transit.ticketing.bpp.protocol.protocol.status.services.StatusService

@Service
class SearchService @Autowired constructor(
    private val registryService: RegistryService,
    private val bppClientSearchService: BppClientSearchService,
    private val bapOnSearchService: BapOnSearchService,
) {
    val log: Logger = LoggerFactory.getLogger(StatusService::class.java)

     fun search(context: ProtocolContext, intent: ProtocolIntent): Either<HttpError, ProtocolAckResponse> {
        log.info("Got search request with intent: {} ", intent)
        var subscriber: SubscriberDto? = null
        return registryService
            .lookupBapById(context.bapId!!)
            .flatMap {
                if (!it.isNullOrEmpty()) {
                    subscriber = it.first()
                    bppClientSearchService.search(subscriber, context, intent)
                }else{
                    Either.Left(RegistryLookupError.NoSubscriberFound)
                }
            }.flatMap {
                     CoroutineScope(Dispatchers.Default).launch {
                        bapOnSearchService.onSearch(subscriber!!, context, it)
                    }
                    Either.Right(ProtocolAckResponse(context, ResponseMessage.ack()))
            }
    }


}
