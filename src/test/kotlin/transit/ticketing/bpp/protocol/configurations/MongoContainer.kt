package transit.ticketing.bpp.protocol.configurations

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName


object MongoContainer {

  val instance by lazy { start() }

  const val MONGODB_PORT = 27017

  private fun start(): MongoDBContainer {
    val container = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
      .waitingFor(Wait.forListeningPort())
      .withExposedPorts(MONGODB_PORT)
    container.start()
    return container
  }
}