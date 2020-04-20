package de.tum.storemanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nimmsta.androidframework.device.RecyclerItemClickListener
import de.tum.storemanager.R
import de.tum.storemanager.StoreManager
import kotlinx.android.synthetic.main.activity_select_store.*

class StoreSelectActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_select_store)
        if(actionBar == null && supportActionBar == null) {
            setSupportActionBar(findViewById(R.id.toolbar))
        } else {
            findViewById<View>(R.id.toolbar).visibility = View.GONE
        }
        title = "Select Store"

        storeListView.adapter = StoreListAdapter(this)

        swipe_container.setOnRefreshListener {
            StoreManager.service.update()
        }
        storeListView.layoutManager = LinearLayoutManager(this)
        storeListView.isEnabled = true

        storeListView.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                storeListView,
                object :
                    RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        (storeListView.adapter as? StoreListAdapter)?.getStore(position)?.let {
                            val intent = Intent()
                            intent.putExtra(MainActivity.EXTRA_STORE_ID, it.id.toString())
                            setResult(MainActivity.STORE_RESULT, intent)
                            finish()
                        }
                    }

                    override fun onItemLongClick(view: View?, position: Int) {

                    }

                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        (storeListView.adapter as? StoreListAdapter)?.destroy()
    }
}
