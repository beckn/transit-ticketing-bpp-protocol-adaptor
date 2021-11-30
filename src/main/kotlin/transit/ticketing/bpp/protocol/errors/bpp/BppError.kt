package transit.ticketing.bpp.protocol.errors.bpp


import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ResponseMessage

sealed class BppError : HttpError {
  val bppError = ProtocolError("BPP_011", "BPP returned error")
  val nullError = ProtocolError("BPP_012", "BPP returned null")
  val nackError = ProtocolError("BPP_013", "BPP returned nack")
  val pendingPaymentError = ProtocolError("BPP_015", "BPP hasn't received payment yet")
  val bppIdNotPresent = ProtocolError("BPP_016", "BPP Id is mandatory")
  val autheticationError = ProtocolError("BPP_401", "Invalid Credentials")
  val badRequestError = ProtocolError("BPP_404", "Bad Request")

  object Internal : BppError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = bppError
  }

  object Nack : BppError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = nackError
  }

  object NullResponse : BppError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = nullError
  }

  object PendingPayment : BppError() {
    override fun status(): HttpStatus = HttpStatus.BAD_REQUEST

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = pendingPaymentError
  }

  object BppIdNotPresent : BppError() {
    override fun status(): HttpStatus = HttpStatus.BAD_REQUEST

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = bppIdNotPresent
  }

  object AuthenticationError : BppError() {
    override fun status(): HttpStatus = HttpStatus.UNAUTHORIZED

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = autheticationError
  }

  object BadRequestError : BppError() {
    override fun status(): HttpStatus = HttpStatus.BAD_REQUEST

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = badRequestError
  }
}