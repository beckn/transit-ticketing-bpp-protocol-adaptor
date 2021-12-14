package transit.ticketing.bpp.protocol.protocol.external.provider

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import transit.ticketing.bpp.protocol.protocol.shared.dtos.ConfirmRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.dtos.OrderStatusRequestDto
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.BlockBookResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.ClientBlockTicketRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.ClientBookTicketRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.SearchResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*

interface BppServiceClient {
  @GET("protocol/search_by_gps")
  fun search(@Query("origin") origin: String,@Query("destination") destination: String ): Call<SearchResponse>

  @POST("block_ticket")
  fun blockTicket(@Body request: ClientBlockTicketRequest): Call<BlockBookResponse>

  @POST("book_ticket")
  fun bookTicket(@Body request: ClientBookTicketRequest): Call<BlockBookResponse>

  @POST("status")
  fun status(@Body request: OrderStatusRequestDto): Call<ProtocolOnOrderStatus>

}
