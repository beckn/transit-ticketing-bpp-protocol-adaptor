package transit.ticketing.bpp.protocol.protocol.common.factories

import com.github.tomakehurst.wiremock.WireMockServer

object MockProtocolBap {

  val instance by lazy { start() }

  fun withResetInstance(): WireMockServer {
    instance.resetAll()
    return instance;
  }

  private fun start(): WireMockServer {
    val wireMockServer = WireMockServer(4011)
    wireMockServer.start()
    return wireMockServer
  }
}