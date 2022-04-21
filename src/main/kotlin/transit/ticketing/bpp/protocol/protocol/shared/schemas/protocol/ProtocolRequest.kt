package transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol

interface ProtocolRequest {
    val context: ProtocolContext
}

data class ProtocolTrackRequest @Default constructor(
    override val context: ProtocolContext,
    val message: ProtocolTrackRequestMessage
) : ProtocolRequest

data class ProtocolTrackRequestMessage @Default constructor(
    val orderId: String,
    val callbackUrl: String? = null,
)

data class ProtocolSupportRequest @Default constructor(
    override val context: ProtocolContext,
    val message: ProtocolSupportRequestMessage
) : ProtocolRequest

data class ProtocolSupportRequestMessage @Default constructor(
    val refId: String
)

data class ProtocolRatingRequest @Default constructor(
    override val context: ProtocolContext,
    val message: ProtocolRatingRequestMessage
) : ProtocolRequest

data class ProtocolRatingRequestMessage @Default constructor(
    val id: String,
    val value: Int
)

data class ProtocolGetPolicyRequest @Default constructor(
    override val context: ProtocolContext
) : ProtocolRequest

data class ProtocolCancelRequest @Default constructor(
    override val context: ProtocolContext,
    val message: ProtocolCancelRequestMessage
) : ProtocolRequest

data class ProtocolCancelRequestMessage @Default constructor(
    val orderId: String,
    val cancellationReasonId: String
)
