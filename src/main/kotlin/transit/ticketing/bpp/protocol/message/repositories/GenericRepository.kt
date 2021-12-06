package transit.ticketing.bpp.protocol.message.repositories

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.*
import transit.ticketing.bpp.protocol.Open

@Open
class GenericRepository<R : Any>(private val collection: MongoCollection<R>) {

  companion object {
    inline fun <reified T : Any> create(database: MongoDatabase): GenericRepository<T> {
      return GenericRepository(database.getCollection())
    }

    inline fun <reified T : Any> create(database: MongoDatabase, collectionName: String): GenericRepository<T> {
      return GenericRepository(database.getCollectionOfName(collectionName))
    }
  }

  fun size(): Long = collection.countDocuments()

  fun insertMany(documents: List<R>) = collection.insertMany(documents)

  fun insertOne(document: R): R {
    collection.insertOne(document)
    return document;
  }

  fun findAll(query: Bson) = collection.find(query).toList()

  fun findOne(query: Bson) = collection.findOne(query)

  fun all() = collection.find().toList()

  fun clear() = collection.deleteMany(Document())

  fun updateOneByQuery(query: Bson, entity: R, options: UpdateOptions) = collection.updateOne(query,entity,options)

  fun deleteOneById(id: String) = collection.deleteOne(id)

}
