import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import feri.um.leaflink.Event
import feri.um.leaflink.Park
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList

object MongoDBClient {
    private const val CONNECTION_STRING = "mongodb+srv://leafadmin:leaf123@leafy.gnw7mw8.mongodb.net/?retryWrites=true&w=majority&appName=leafy"
    private const val DATABASE_NAME = "leafCollection"

    private var mongoClient: MongoClient? = null
    private var database = mongoClient?.getDatabase(DATABASE_NAME)

    suspend fun initializeClient() {
        try {
            mongoClient = MongoClient.create(ConnectionString(CONNECTION_STRING))
            database = mongoClient?.getDatabase(DATABASE_NAME)
        } catch (e: Exception) {
            // Handle MongoDB initialization failure
            throw e
        }
    }

    suspend fun getEvents(): List<Event> {
        val collection = database?.getCollection<Event>("events")
        return collection?.find()?.toList() ?: emptyList()
    }

    suspend fun getParks(): List<Park> {
        val collection = database?.getCollection<Park>("parks")
        return collection?.find()?.toList() ?: emptyList()
    }
}
