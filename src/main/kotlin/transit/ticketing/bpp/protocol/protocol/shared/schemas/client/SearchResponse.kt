package transit.ticketing.bpp.protocol.protocol.shared.schemas.client


data class SearchResponse (
    val trip : Trip,
    val availability : List<Availability>,
    val location : List<Location>?=null
)