package transit.ticketing.bpp.protocol.errors.database


import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.errors.HttpError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage

sealed class DatabaseError : HttpError {
  val onWriteError = ProtocolError("BAP_006", "Error when writing to DB")
  val onReadError = ProtocolError("BAP_007", "Error when reading from DB")
  val notFoundError = ProtocolError("BAP_008", "No message with the given ID")
  val onDeleteError = ProtocolError("BAP_009", "Error when deleting from DB")

  object OnWrite : DatabaseError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = onWriteError
  }

  object OnRead : DatabaseError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = onReadError
  }

  object NotFound : DatabaseError() {
    override fun status(): HttpStatus = HttpStatus.NOT_FOUND

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = notFoundError
  }

  object OnDelete: DatabaseError() {
    override fun status(): HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    override fun message(): ResponseMessage = ResponseMessage.nack()

    override fun error(): ProtocolError = onDeleteError
  }
}