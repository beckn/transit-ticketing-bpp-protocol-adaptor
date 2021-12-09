package transit.ticketing.bpp.protocol.configurations

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollectionOfName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import transit.ticketing.bpp.protocol.message.entities.OnConfirmDao
import transit.ticketing.bpp.protocol.message.entities.OnOrderStatusDao
import transit.ticketing.bpp.protocol.message.repositories.BecknResponseRepository

@Configuration
class DatabaseConfiguration @Autowired constructor(
  @Value("\${database.mongo.url}") private val connectionString: String,
  @Value("\${database.mongo.name}") private val databaseName: String
) {
  @Bean
  fun database(): MongoDatabase {
    val settings = MongoClientSettings.builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    val client = KMongo.createClient(settings)
    return client.getDatabase(databaseName)
  }
  @Bean
  fun createConfirmOrderDb(@Autowired database: MongoDatabase): BecknResponseRepository<OnConfirmDao> =
    BecknResponseRepository(database.getCollectionOfName("confirm"))

  @Bean
  fun createOrderStatusrDb(@Autowired database: MongoDatabase): BecknResponseRepository<OnOrderStatusDao> =
    BecknResponseRepository(database.getCollectionOfName("status"))

}
