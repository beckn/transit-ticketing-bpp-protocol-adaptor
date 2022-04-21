package transit.ticketing.bpp.protocol.errors


import org.springframework.http.HttpStatus
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolError
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ResponseMessage

interface HttpError {
  fun status(): HttpStatus
  fun message(): ResponseMessage
  fun error(): ProtocolError
}
