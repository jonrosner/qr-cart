package de.tum.storemanager.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nimmsta.androidframework.bluetooth.BLEScanResult
import com.nimmsta.sharedapi.device.NIMMSTADeviceScannerListener
import de.tum.storemanager.R
import de.tum.storemanager.Store
import de.tum.storemanager.StoreManager
import java.io.Closeable
import java.util.*
import kotlin.collections.HashMap

/**
 * Adapter for StoreList
 *
 * @author Daniel Gruber
 */
class StoreListAdapter(private val context: Context) : RecyclerView.Adapter<StoreListAdapter.ViewHolder>() {
    private val mLeDevices: LinkedList<NIMMSTADeviceScannerListener.ScanRecord> = LinkedList()
    private val mLeDevicesExisting = HashMap<String, NIMMSTADeviceScannerListener.ScanRecord>()

    private var stores: List<Store> = listOf()
    private val storeWatcher: Closeable

    class ViewHolder(
        val linearLayout: LinearLayout
    ) : RecyclerView.ViewHolder(linearLayout) {
        val storeName: TextView = linearLayout.findViewById(R.id.store_name)
        val storeAddress: TextView = linearLayout.findViewById(R.id.store_address)
        val storeFullness: TextView = linearLayout.findViewById(R.id.store_fullness)
    }

    init {
        storeWatcher = StoreManager.service.stores.watch {
            stores = it
            notifyDataSetChanged()
        }
    }

    fun destroy() {
        storeWatcher.close()
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.listitem_store, parent, false) as LinearLayout

        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return stores.size
    }

    fun getStore(position: Int): Store? {
        return stores.getOrNull(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        stores.getOrNull(position)?.let { store ->
            holder.storeName.text = store.displayName
            holder.storeAddress.text = store.address
            holder.storeFullness.text = "${store.currentNoPeopleInStore} / " + store.maximumPeople
        }
    }
}
