package de.tum.storemanager

import com.soywiz.korio.util.UUID
import de.tum.storemanager.data.Minutes
import de.tum.storemanager.data.SignInData
import de.tum.storemanager.data.StoreData
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.date.GMTDate
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.ThreadLocal


/**
 * Adapter to handle backend API and manage auth information.
 */
@ThreadLocal
internal object ClientApi {
    val endpoint = "http://andromeda.goma-cms.org:1337/"
    
    private var mockStore = Store(
        UUID.randomUUID(),
        "My Store",
        100,
        0.7,
        0,
        0,
        0,
        "FAKE"
    )

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json.nonstrict)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        HttpResponseValidator {
            validateResponse {
                when (it.status) {
                    HttpStatusCode.NotFound -> throw NotFound()
                    HttpStatusCode.Unauthorized -> throw Unauthorized()
                    HttpStatusCode.Forbidden -> throw Forbidden()
                    HttpStatusCode.BadRequest -> throw TooManyPeople()
                }

                if (!it.status.isSuccess()) {
                    when (it.call.request.url.encodedPath) {
                        else -> error("Bad status: ${it.status} Content: ${it.content}")
                    }
                }
            }
        }
    }

    suspend fun signIn(signInData: SignInData): AuthToken = client.post {
        apiUrl("signIn")
        body = signInData
    }

    suspend fun getMyStores(userId: AuthToken): MutableList<Store> {
        val result = client.get {
            apiUrl("api/store/all")
        } as? List<Store>

        return result?.toMutableList() ?: mutableListOf()
    }

    suspend fun getUpcomingReservationsForStore(jwtAuthToken: JWTAuthToken, selectedStoreUuid: UUID?): MutableSet<Reservation> {
        return mutableSetOf(
            Reservation(UUID.randomUUID(), GMTDate(), Minutes(15), Minutes(30))
        )
    }


    suspend fun incrementNumberInStore(jwtAuthToken: JWTAuthToken, selectedStoreUuid: UUID): Int = client.put {
        apiUrl("api/store/$selectedStoreUuid/incCurrentNo", jwtAuthToken)
    }

    suspend fun decrementNumberInStore(jwtAuthToken: JWTAuthToken, selectedStoreUuid: UUID): Int = client.put {
        apiUrl("api/store/$selectedStoreUuid/decCurrentNo", jwtAuthToken)
    }

    suspend fun checkReservationForBarcode(jwtAuthToken: JWTAuthToken, selectedStoreUuid: UUID, reservationId: String): Boolean {
        return try {
            client.put {
                apiUrl("api/reservation/$reservationId/checkin", jwtAuthToken)
                body = StoreData(selectedStoreUuid)
                contentType(ContentType.Application.Json)
            } as String
            true
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Get server time.
     */
    suspend fun getServerTime(): GMTDate = client.get<String> {
        apiUrl("time")
    }.let { response -> GMTDate(response.toLong()) }

    private fun HttpRequestBuilder.json() {
        contentType(ContentType.Application.Json)
    }

    private fun HttpRequestBuilder.apiUrl(path: String, userId: AuthToken? = null) {
        if (userId != null) {
            //header(HttpHeaders.Authorization, "Bearer $userId")
        }
        header(HttpHeaders.CacheControl, "no-cache")
        url {
            takeFrom(endpoint)
            encodedPath = path
        }
    }
}
