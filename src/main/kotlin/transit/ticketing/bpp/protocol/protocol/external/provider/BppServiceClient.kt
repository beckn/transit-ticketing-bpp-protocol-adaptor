package transit.ticketing.bpp.protocol.protocol.external.provider

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.BlockBookResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.ClientConfirmRequest
import transit.ticketing.bpp.protocol.protocol.shared.schemas.client.SearchResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.*

interface BppServiceClient {
  @GET("protocol/search_by_gps")
  fun search(@Query("origin") origin: String,@Query("destination") destination: String ): Call<SearchResponse>

  @POST("block_ticket")
  fun blockTicket(@Body request: ClientConfirmRequest): Call<BlockBookResponse>

  @POST("book_ticket")
  fun bookTicket(@Body request: ProtocolConfirmRequest): Call<BlockBookResponse>

  @POST("status")
  fun status(@Body request: ProtocolOrderStatusRequest): Call<ProtocolOnOrderStatus>

}
