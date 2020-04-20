package de.tum.storemanager.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.barcode.Barcode
import com.google.firebase.analytics.FirebaseAnalytics
import com.nimmsta.androidframework.about.AboutActivity
import com.nimmsta.androidframework.device.setLayout
import com.nimmsta.androidframework.firmwareupgrade.SoftwareUpgradeActivity
import com.nimmsta.androidframework.framework.NIMMSTAServiceConnection
import com.nimmsta.sharedapi.device.NIMMSTADeviceDelegate
import com.nimmsta.sharedapi.exception.bluetooth.BluetoothDisconnectedException
import com.nimmsta.sharedapi.layout.ButtonClickEvent
import com.nimmsta.sharedapi.layout.button
import com.nimmsta.sharedapi.textprotocol.event.ScanEvent
import com.notbytes.barcode_reader.BarcodeReaderActivity
import de.tum.storemanager.R
import de.tum.storemanager.StoreManager
import de.tum.storemanager.StoreService
import de.tum.storemanager.TooManyPeople
import de.tum.storemanager.storage.ApplicationContext
import io.ktor.util.date.toJvmDate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.net.ConnectException
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), NIMMSTADeviceDelegate {
    private lateinit var errorsWatcher: Closeable
    private lateinit var selectedStoreWatcher: Closeable
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var serviceConnection: NIMMSTAServiceConnection? = null

    private val reservationMutex = Mutex()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = ApplicationContext(this, R.drawable.notification_icon)
        StoreManager.service = StoreService(context)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        errorsWatcher = StoreManager.service.errors.watch { cause ->
            when (cause) {
                is ConnectException -> {
                    Toast.makeText(this, "Failed to get data from server, please check your internet connection.", Toast.LENGTH_LONG).show()

                    serviceConnection?.currentDevice?.triggerSOS()
                }
                is TooManyPeople -> {
                    Toast.makeText(this, "Too many people. Please stop letting people in!", Toast.LENGTH_LONG).show()

                    serviceConnection?.currentDevice?.triggerSOS()
                }
            }
        }

        NIMMSTAServiceConnection.bindServiceToActivity(this).onSuccess {
            this.serviceConnection = it
            if(!it.isConnected) {
                it.displayConnectionActivity()
            }
        }.onError {
            Toast.makeText(this, "Could not connect to NIMSMTA service because $it", Toast.LENGTH_LONG).show()
        }

        selectedStoreWatcher = StoreManager.service.selectedStore.watch {
            if(it != null) {
                title = "$it"
            } else {
                title = "Select a store"
                selectStore()
            }

            number_allowed_walk_in?.text = "${it?.allowedWalkIn}"

            // disable plus button when no one is allowed anymore
            plusButton.isEnabled = it?.allowedWalkIn?.compareTo(0) == 1


            change_since_last_sync.text = "Change since last sync: ${it?.newWalkIn}"

            it?.createdAt?.let { syncDate ->
                val dateFormat = SimpleDateFormat.getDateTimeInstance()
                last_sync.text = "Last sync: ${dateFormat.format(syncDate.toJvmDate())}"
            }

            serviceConnection?.currentDevice?.setScreenInfoAsync(hashMapOf(
                "allow_in" to "Allow: ${it?.allowedWalkIn} New: ${it?.newWalkIn}",
                "scan_icon" to "scan_now",
                "reservation" to "Reservation: 0"
            ))

            customers_inside.text = "Customers inside: ${it?.currentNoPeopleInStore}"
            upcoming_reservations.text = "Upcoming Reservations: ${it?.numUpcomingReservations}"
        }

        setContentView(R.layout.activity_main)
        if(actionBar == null && supportActionBar == null) {
            setSupportActionBar(findViewById(R.id.toolbar))
        } else {
            findViewById<View>(R.id.toolbar).visibility = View.GONE
        }

        if (StoreManager.service.isFirstLaunch()) {
            // TODO: Show first launch activity
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == BARCODE_READER_REQUEST_CODE) {
            (data?.extras?.get(BarcodeReaderActivity.KEY_CAPTURED_BARCODE) as? Barcode)?.let {
                evaluateReservationBarcode(it.rawValue)
            }
        } else if(requestCode == STORE_RESULT) {
            data?.extras?.getString(EXTRA_STORE_ID)?.let {
                StoreManager.service.setStoreId(it)
            }
        }
    }

    fun evaluateReservationBarcode(barcode: String) {
        MainScope().launch {
            reservationMutex.withLock {
                if (StoreManager.service.checkReservationForBarcode(barcode)) {
                    StoreManager.service.newCustomer(true)

                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Reservation Allowed", Toast.LENGTH_LONG).show()
                        serviceConnection?.currentDevice?.api?.triggerLEDBurst(1, 1500, 1000, 0, 255, 0)
                    }
                } else {
                    runOnUiThread {
                        serviceConnection?.currentDevice?.triggerSOS()
                        Toast.makeText(this@MainActivity, "Reservation not Allowed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun selectStore() {
        val intent = Intent(this, StoreSelectActivity::class.java)
        startActivityForResult(intent, STORE_RESULT)
    }

    fun increaseNumber(v: View) {
        StoreManager.service.newCustomer()
    }

    fun decreaseNumber(v: View) {
        StoreManager.service.customerLeft()
    }

    override fun onDestroy() {
        errorsWatcher.close()
        selectedStoreWatcher.close()
        serviceConnection?.let {
            unbindService(it)
        }
        super.onDestroy()
    }

    fun checkReservation(view: View) {
        val intent = BarcodeReaderActivity.getLaunchIntent(
            this,
            true,
            false
        )
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(serviceConnection?.isConnected == true) {
            menu?.findItem(R.id.connect)?.isVisible = false
            menu?.findItem(R.id.disconnect)?.isVisible = true
        } else {
            menu?.findItem(R.id.connect)?.isVisible = true
            menu?.findItem(R.id.disconnect)?.isVisible = false
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.disconnect) {
            serviceConnection?.currentDevice?.closeConnection(false)
        }
        if(item?.itemId == R.id.connect) {
            serviceConnection?.displayConnectionActivity()
        }
        if(item?.itemId == R.id.firmware) {
            startActivity(Intent(this, SoftwareUpgradeActivity::class.java))
        }
        if(item?.itemId == R.id.about) {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        if(item?.itemId == R.id.select_store) {
            selectStore()
        }
        invalidateOptionsMenu()
        return super.onOptionsItemSelected(item)
    }

    override fun didConnectAndInit() {
        super.didConnectAndInit()

        serviceConnection?.currentDevice?.setLayout(R.raw.nimmsta_layout)?.onError {
            it.printStackTrace()
        }

        invalidateOptionsMenu()
    }

    override fun didDisconnect(reason: BluetoothDisconnectedException.Reason) {
        if(reason == BluetoothDisconnectedException.Reason.DEVICE_INITIATED) {
            serviceConnection?.displayConnectionActivity()
        }

        invalidateOptionsMenu()
    }

    override fun onError(throwable: Throwable): Boolean {
        Toast.makeText(this, "NIMMSTA Error: $throwable", Toast.LENGTH_LONG).show()
        return true
    }

    override fun didClickButton(sender: button?, event: ButtonClickEvent) {
        super.didClickButton(sender, event)

        when(sender?.name) {
            "decrement" -> StoreManager.service.customerLeft()
            "increment" -> StoreManager.service.newCustomer()
        }
    }

    override fun didScanBarcode(barcode: String, event: ScanEvent) {
        super.didScanBarcode(barcode, event)

        evaluateReservationBarcode(barcode)
    }

    companion object {
        const val BARCODE_READER_REQUEST_CODE = 5432;
        const val EXTRA_STORE_ID = "store_id"
        const val STORE_RESULT = 3412
    }
}
