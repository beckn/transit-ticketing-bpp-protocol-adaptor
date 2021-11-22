package transit.ticketing.bpp.protocol.errors.registry

import org.beckn.protocol.schemas.ProtocolError
import org.beckn.protocol.schemas.ResponseMessage
import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.errors.HttpError

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
