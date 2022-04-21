package transit.ticketing.bpp.protocol.message.entities

import com.fasterxml.jackson.annotation.JsonProperty
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.Default

data class ProtocolItemDao @Default constructor(
    val id: String? = null,
    val fulfillmentId: String? = null,
    val descriptor: ProtocolDescriptorDao? = null,
    val price: ProtocolPriceDao? = null,
    val quantity: ProtocolItemQuantityAllocatedDao
)

data class ProtocolDescriptorDao @Default constructor(
    val name: String?= null,
    val code: String? = null,
    val symbol: String? = null,
    val shortDesc: String? = null,
    val longDesc: String? = null,
    val images: List<String>? = null,
    val audio: String? = null,
    @JsonProperty("3d_render") val threeDRender: String? = null
)