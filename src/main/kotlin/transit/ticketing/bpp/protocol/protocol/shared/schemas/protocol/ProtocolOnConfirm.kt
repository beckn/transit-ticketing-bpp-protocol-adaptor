package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProtocolOnConfirm @Default constructor(
    override val context: ProtocolContext? = null,
    val message: ProtocolOnConfirmMessage? = null,
    override val error: ProtocolError? = null
) : ProtocolResponse

data class ProtocolOnConfirmMessage @Default constructor(
    val order: ProtocolOrder? = null
)
