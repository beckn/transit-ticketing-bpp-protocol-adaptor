package transit.ticketing.bpp.protocol.protocol.external.provider

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolAckResponse
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnConfirm
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnInit
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolOnSearch

interface BapServiceClient {

  @POST("on_search")
  fun onSearch(@Body request: ProtocolOnSearch): Call<ProtocolAckResponse>

  @POST("on_init")
  fun onInit(@Body request: ProtocolOnInit): Call<ProtocolAckResponse>

  @POST("on_confirm")
  fun onConfirm(@Body request: ProtocolOnConfirm): Call<ProtocolAckResponse>

}
