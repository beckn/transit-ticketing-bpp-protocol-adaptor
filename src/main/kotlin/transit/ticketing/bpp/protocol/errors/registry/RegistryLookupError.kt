package transit.ticketing.bpp.protocol.errors.registry


import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage

sealed class RegistryLookupError : HttpError {
  val registryError = ProtocolError("BPP_001", "Registry lookup returned error")
  val noSubscribersFoundError = ProtocolError("BPP_002", "Registry lookup did not return any Subscribers")

  object Internal : RegistryLookupError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = registryError
  }

  object NoSubscriberFound : RegistryLookupError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = noSubscribersFoundError
  }
}
