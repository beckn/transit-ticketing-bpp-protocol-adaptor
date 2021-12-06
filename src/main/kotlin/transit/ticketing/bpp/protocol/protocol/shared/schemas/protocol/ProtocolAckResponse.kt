package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

data class ProtocolAckResponse(
    override val context: ProtocolContext?,
    val message: ResponseMessage,
    override val error: ProtocolError? = null,
) : ProtocolResponse
