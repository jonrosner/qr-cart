package de.tum.storemanager

import com.soywiz.korio.util.UUID
import de.tum.storemanager.storage.ApplicationContext
import de.tum.storemanager.storage.ApplicationStorage
import de.tum.storemanager.storage.invoke
import de.tum.storemanager.storage.live
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.serializer
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.ThreadLocal
import kotlin.time.ExperimentalTime

@ThreadLocal
@UseExperimental(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class StoreService(val context: ApplicationContext) : CoroutineScope {
    private val exceptionHandler = object : CoroutineExceptionHandler {
        override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

        override fun handleException(context: CoroutineContext, exception: Throwable) {
            _errors.offer(exception)
        }
    }

    private val _errors = ConflatedBroadcastChannel<Throwable>()
    val errors = _errors.wrap()

    override val coroutineContext: CoroutineContext = dispatcher() + SupervisorJob() + exceptionHandler

    private var serverTime = GMTDate()
    private var requestTime = GMTDate()

    private val storage: ApplicationStorage = ApplicationStorage(context)
    private var authToken: String? by storage(String.serializer().nullable) { "HALLO WELT" }
    private var firstLaunch: Boolean by storage(Boolean.serializer()) { true }
    private var notificationsAllowed: Boolean by storage(Boolean.serializer()) { false }
    private var selectedStoreUuidString: String? by storage(String.serializer().nullable) { null }
    private var selectedStoreUuid: UUID?
        get() = if(selectedStoreUuidString != null) UUID.invoke(selectedStoreUuidString!!) else null
        set(value) {
            selectedStoreUuidString = value?.toString()
            updateSelectedStore()
        }

    private var _firstLaunch: Boolean = firstLaunch

    /**
     * Stores
     */
    private val _stores by storage.live { mutableListOf<Store>() }
    val stores = _stores.wrap()

    private val _loggedIn by storage.live { true }
    val loggedIn = _loggedIn.wrap()

    private val _selectedStore by storage.live<Store?> { null }
    val selectedStore = _selectedStore.wrap()

    private val _storeReservations by storage.live { mutableSetOf<Reservation>() }
    val reservations = _storeReservations.wrap()

    /**
     * ------------------------------
     * Representation.
     * ------------------------------
     */

    init {
        selectedStore.watch {
            if(selectedStoreUuid?.toString() != it?.id?.toString()) {
                selectedStoreUuid = it?.id
            }

            _storeReservations.offer(mutableSetOf())
        }

        stores.watch {
            updateSelectedStore()
        }

        selectedStoreUuidString = "254bd894-d5ec-4c6a-aa6c-23df2bc6fb6d"

        launch {
            while (true) {
                try {
                    update()
                } catch (cause: Throwable) {
                    _errors.offer(cause)
                }
                delay(10 * 1000)
            }
        }
    }

    private fun updateSelectedStore() {
        val stores = _stores.value
        if(stores.size > 0) {
            selectedStoreUuid?.let { selected ->
                _selectedStore.offer(
                    stores.find {
                        it.id.toString() == selected.toString()
                    }
                )
            }
        }
    }

    /**
     * Returns true if app is launched first time.
     */
    fun isFirstLaunch(): Boolean {
        val result = _firstLaunch
        if (result) {
            firstLaunch = false
        }

        return result
    }

    fun update() {
        launch {
            try {
                _stores.offer(ClientApi.getMyStores(JWTAuthToken(authToken!!)))

                if(_stores.value.size == 1) {
                    _selectedStore.offer(_stores.value.first())
                }

                listReservationsForStore()
            } catch (t: Forbidden) {
                _loggedIn.offer(false)
            } catch (t: Throwable) {
                _errors.offer(t)
            }
        }
    }

    fun newCustomer(withReservation: Boolean = false) {
        _selectedStore.value?.let {
            if(it.allowedWalkIn == 0) {
                _errors.offer(TooManyPeople())
            }

            it.newWalkIn += 1
            _selectedStore.offer(it)

            launch {
                try {
                    ClientApi.incrementNumberInStore(JWTAuthToken(authToken!!), selectedStoreUuid!!)
                } catch (t: Throwable) {
                    _errors.offer(t)
                }
            }
        }
    }

    fun customerLeft() {
        _selectedStore.value?.let {
            it.newWalkIn -= 1
            _selectedStore.offer(it)

            launch {
                try {
                    ClientApi.decrementNumberInStore(JWTAuthToken(authToken!!), selectedStoreUuid!!)
                } catch (t: Throwable) {
                    _errors.offer(t)
                }
            }
        }
    }

    fun setStoreId(storeId: String) {
        selectedStoreUuid = UUID(storeId)
    }

    private suspend fun listReservationsForStore() {
        _storeReservations.offer(ClientApi.getUpcomingReservationsForStore(JWTAuthToken(authToken!!), selectedStoreUuid))
    }

    suspend fun checkReservationForBarcode(barcode: String): Boolean {
        selectedStoreUuid?.let { storeId ->
            return ClientApi.checkReservationForBarcode(JWTAuthToken(authToken!!), storeId, barcode)
        }

        return false
    }
}
