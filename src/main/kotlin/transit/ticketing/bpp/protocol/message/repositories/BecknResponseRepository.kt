package transit.ticketing.bpp.protocol.message.repositories

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import org.bson.conversions.Bson
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import transit.ticketing.bpp.protocol.message.entities.BecknResponseDao
import transit.ticketing.bpp.protocol.protocol.shared.schemas.protocol.ProtocolContext

class BecknResponseRepository<R : BecknResponseDao>(
  val collection: MongoCollection<R>
) : GenericRepository<R>(collection) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun findManyByUserId(id: String, skip: Int = 0 , limit :Int = 10): List<R> =
    collection.find(BecknResponseDao::context / ProtocolContext::transactionId eq id).limit(limit).skip(skip).toList()

  fun findById(id: String): R? {
    return findOne(BecknResponseDao::context / ProtocolContext::transactionId eq id)
  }

  fun updateByIdQuery(id: Bson, requestData: R, updateOptions: UpdateOptions): UpdateResult {
    return updateOneByQuery(
      id,
      requestData,
      updateOptions
    )
  }
}