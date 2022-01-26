package transit.ticketing.bpp.protocol.protocol.discovery.services

import arrow.core.Either
import arrow.core.Either.Left
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnSearch
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage
import transit.ticketing.bpp.protocol.protocol.status.services.BppClientStatusService


@Service
class BapOnSearchService @Autowired constructor(
  private val bapServiceFactory: BapServiceFactory,
  ) {
  private val log: Logger = LoggerFactory.getLogger(BppClientStatusService::class.java)

  suspend fun onSearch(subscriberDto: SubscriberDto, context: ProtocolContext, protocolRequest: ProtocolOnSearch)
  : Either<BppError,ProtocolAckResponse> {
      log.info("Initiating OnSearch : {}. Context: {}", subscriberDto)
    println("Thread  onSearch is : ${Thread.currentThread().name}")

      val clientService = bapServiceFactory.getBapClient(subscriberDto?.subscriber_url)
    return try{
      val httpResponse = clientService.onSearch(protocolRequest)
      if(httpResponse.error == null ){
        log.info("Successful onSearch Bap Response: {}" )
        Either.Right(httpResponse)
      }else{
        log.info("Failed onSearch Bap Response: {}" )
        Left(BppError.NullResponse)
      }
    }catch( e: Exception){
      log.info("exception invoked OnSearch BAP Response: {}", e.message)
      Left(BppError.Internal)
    }
  }
}