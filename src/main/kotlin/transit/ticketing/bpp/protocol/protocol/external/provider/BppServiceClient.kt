package transit.ticketing.bpp.protocol.protocol.external.provider

import org.beckn.protocol.schemas.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BppClient {
  @POST("search")
  fun search(@Body request: ProtocolSearchRequest): Call<ProtocolAckResponse>

  @POST("init")
  fun init(@Body request: ProtocolInitRequest): Call<ProtocolAckResponse>

  @POST("confirm")
  fun confirm(@Body request: ProtocolConfirmRequest): Call<ProtocolAckResponse>

  @POST("cancel")
  fun cancel(@Body request: ProtocolCancelRequest): Call<ProtocolAckResponse>
}
