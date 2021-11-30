package transit.ticketing.bpp.protocol.protocol.external.provider

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import transit.ticketing.bpp.protocol.protocol.shared.schemas.*

interface BppClient {
  @GET("stations")
  fun search(@Body request: ProtocolSearchRequest): Call<ProtocolOnSearch>

  @POST("init")
  fun init(@Body request: ProtocolInitRequest): Call<ProtocolOnInit>

  @POST("confirm")
  fun confirm(@Body request: ProtocolConfirmRequest): Call<ProtocolOnConfirm>

  @POST("status")
  fun status(@Body request: ProtocolOrderStatusRequest): Call<ProtocolOnOrderStatus>

  @POST("on_search")
  fun onSearch(@Body request: ProtocolOnSearch): Call<ProtocolAckResponse>

}
