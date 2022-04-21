package transit.ticketing.bpp.protocol.configurations

import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class TestDatabaseConfiguration {
  @Bean
  @Primary
  fun testDatabase(): MongoDatabase {
    val host = MongoContainer.instance.host
    val port = MongoContainer.instance.getMappedPort(MongoContainer.MONGODB_PORT)
    val connectionString = "mongodb://$host:$port"
    val client = KMongo.createClient(connectionString)
    return client.getDatabase("transit_protocol_test")
  }
}