package transit.ticketing.bpp.protocol.protocol.shared.schemas

data class ProtocolAckResponse(
    override val context: ProtocolContext?,
    val message: ResponseMessage,
    override val error: ProtocolError? = null,
) : ProtocolResponse
