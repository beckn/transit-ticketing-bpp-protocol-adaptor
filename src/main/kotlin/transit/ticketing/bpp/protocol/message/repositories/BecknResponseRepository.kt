package transit.ticketing.bpp.protocol.message.repositories

import com.mongodb.client.MongoCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import transit.ticketing.bpp.protocol.message.entities.BecknResponseDao

class BecknResponseRepository<R : BecknResponseDao>(
  val collection: MongoCollection<R>
) : GenericRepository<R>(collection) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)
}