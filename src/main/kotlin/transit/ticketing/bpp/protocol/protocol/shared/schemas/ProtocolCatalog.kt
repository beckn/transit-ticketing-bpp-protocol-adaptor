package transit.ticketing.bpp.protocol.protocol.shared.schemas

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ProtocolCatalog @Default constructor(
    @JsonProperty("bpp/descriptor") val bppDescriptor: ProtocolDescriptor? = null,
    @JsonProperty("bpp/providers") val bppProviders: List<ProtocolProviderCatalog>? = null,
    val id: String? = null,
    val exp: LocalDateTime? = null
)

data class ProtocolProviderCatalog @Default constructor(
    val id: String? = null,
    val descriptor: ProtocolDescriptor? = null,
    val locations: List<ProtocolLocation>? = null,
//    val categories: List<ProtocolCategory>? = null,
    val items: List<ProtocolItem>? = null,
    val tags: Map<String, String>? = null,
    val exp: LocalDateTime? = null,
    val matched: Boolean? = null,
    val fulfillments: List<ProtocolFulfillment>? = null,
)

data class ProtocolDescriptor @Default constructor(
    val name: String?,
    val code: String? = null,
    val symbol: String? = null,
    val shortDesc: String? = null,
    val longDesc: String? = null,
    val images: List<String>? = null,
    val audio: String? = null,
    @JsonProperty("3d_render") val threeDRender: String? = null
)