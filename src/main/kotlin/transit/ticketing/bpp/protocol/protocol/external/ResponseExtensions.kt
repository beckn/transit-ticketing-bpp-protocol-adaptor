package transit.ticketing.bpp.protocol.protocol.external

import arrow.core.Either
import org.springframework.http.HttpStatus
import retrofit2.Response
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.ResponseStatus
import java.util.function.Predicate


fun <T> Response<T>.rightIf(cond: Predicate<Response<T>>) =
  if (cond.test(this)) Either.Right(this) else Either.Left(this)

fun <T> Response<T>.isInternalServerError() = this.code() == HttpStatus.INTERNAL_SERVER_ERROR.value()

fun <T> Response<T>.hasBody() = this.body() != null

fun <T : ProtocolAckResponse> Response<T>.isAckNegative() = this.body()!!.message.ack.status == ResponseStatus.NACK