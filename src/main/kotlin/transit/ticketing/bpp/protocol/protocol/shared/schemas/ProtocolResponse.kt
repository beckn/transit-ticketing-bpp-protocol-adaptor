package transit.ticketing.bpp.protocol.protocol.shared.schemas

import kotlinx.serialization.Serializable

interface ProtocolResponse {
    val context: ProtocolContext?
    val error: ProtocolError?
}

data class ResponseMessage @Default constructor(val ack: ProtocolAck) {
    companion object {
        fun ack(): ResponseMessage = ResponseMessage(ProtocolAck(ResponseStatus.ACK))
        fun nack(): ResponseMessage = ResponseMessage(ProtocolAck(ResponseStatus.NACK))
    }
}
@Serializable
enum class ResponseStatus(
    val status: String
) {
    ACK("ACK"),
    NACK("NACK");
}

