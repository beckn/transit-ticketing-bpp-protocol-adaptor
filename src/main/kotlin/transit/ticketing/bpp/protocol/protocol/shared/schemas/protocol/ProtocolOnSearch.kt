package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProtocolOnSearch @Default constructor(
    override val context: ProtocolContext? = null,
    val message: ProtocolOnSearchMessage?,
    override val error: ProtocolError? = null,
) : ProtocolResponse

data class ProtocolOnSearchMessage @Default constructor(
    val catalog: ProtocolCatalog? = null
)