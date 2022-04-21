package transit.ticketing.bpp.protocol.protocol.external.provider

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirm
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnInit
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnSearch

interface BapServiceClient {

  @POST("on_search")
  suspend fun onSearch(@Body request: ProtocolOnSearch): ProtocolAckResponse

  @POST("on_init")
  fun onInit(@Body request: ProtocolOnInit): Call<ProtocolAckResponse>

  @POST("on_confirm")
  fun onConfirm(@Body request: ProtocolOnConfirm): Call<ProtocolAckResponse>

  @POST("on_confirm")
  suspend fun onConfirmBap(@Body request: ProtocolOnConfirm): ProtocolAckResponse
}
