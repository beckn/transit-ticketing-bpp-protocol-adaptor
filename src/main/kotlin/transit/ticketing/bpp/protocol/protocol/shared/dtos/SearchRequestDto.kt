package transit.ticketing.bpp.protocol.protocol.shared.dtos

import org.beckn.protocol.schemas.Default

data class SearchRequestDto @Default constructor(
  val context: ClientContext,
  val message: SearchRequestMessageDto,
)

data class SearchRequestMessageDto @Default constructor(
  val criteria: SearchCriteria
)

data class SearchCriteria @Default constructor(
  val searchString: String? = null,
  val deliveryLocation: String? = null,
  val providerId: String? = null,
  val categoryId: String? = null,
  val pickupLocation: String? = null
)
