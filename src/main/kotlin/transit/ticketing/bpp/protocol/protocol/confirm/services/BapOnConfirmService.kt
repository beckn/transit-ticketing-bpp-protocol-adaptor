package transit.ticketing.bpp.protocol.protocol.confirm.services

import arrow.core.Either
import arrow.core.Either.Left
import kotlinx.coroutines.*
import okhttp3.Request
import okio.Timeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.errors.bpp.BppError
import transit.ticketing.bpp.protocol.protocol.external.domains.Subscriber
import transit.ticketing.bpp.protocol.protocol.external.hasBody
import transit.ticketing.bpp.protocol.protocol.external.isInternalServerError
import transit.ticketing.bpp.protocol.protocol.external.registry.SubscriberDto
import transit.ticketing.bpp.protocol.protocol.external.provider.BapServiceFactory
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*


@Service
class BapOnConfirmService @Autowired constructor(
  private val bapServiceFactory: BapServiceFactory,
  ) {
  private val log: Logger = LoggerFactory.getLogger(BapOnConfirmService::class.java)

    suspend fun postOnConfirm(
    subscriberDto: SubscriberDto, context: ProtocolContext,
    request: ProtocolOnConfirm
  ): Either<HttpError, ProtocolAckResponse> {
      return Either.catch {
        log.info("Initiating Search using gateway: {}. Context: {} $subscriberDto, $context")
        val clientService = bapServiceFactory.getBapClient(subscriberDto.subscriber_url)
        val job: Deferred<ProtocolAckResponse> = CoroutineScope(Dispatchers.Default).async {
          clientService.onConfirmBap(request)
        }
        val resp = job.await()
        return if (resp.error == null) {
          Either.Right(ProtocolAckResponse(context = context, message = ResponseMessage.ack()))
        } else {
          log.error("Error when initiating search ${resp.error}")
          return Left(BppError.NullResponse)
        }
      }.mapLeft {
        log.error("Error when initiating search  $it")
        return Left(BppError.Internal)
      }
  }
}