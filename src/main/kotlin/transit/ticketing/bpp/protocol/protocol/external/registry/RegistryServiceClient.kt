package transit.ticketing.bpp.protocol.protocol.external.registry

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistryClient {
  @POST("lookup")
  fun lookup(@Body request: SubscriberLookupRequest): Call<List<SubscriberDto>>
}