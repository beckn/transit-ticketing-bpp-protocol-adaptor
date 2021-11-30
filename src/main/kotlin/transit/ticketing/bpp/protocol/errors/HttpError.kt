package transit.ticketing.bpp.protocol.errors


import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ResponseMessage

interface HttpError {
  fun status(): HttpStatus
  fun message(): ResponseMessage
  fun error(): ProtocolError
}
