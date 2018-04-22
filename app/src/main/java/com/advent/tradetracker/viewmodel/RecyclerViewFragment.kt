package com.advent.tradetracker.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import com.advent.group69.tradetracker.R
import com.advent.group69.tradetracker.model.Stock
import com.advent.group69.tradetracker.model.StockInterface
import com.advent.group69.tradetracker.view.OnStartDragListener
import com.advent.group69.tradetracker.view.RecyclingStockAdapter
import com.advent.group69.tradetracker.view.SimpleItemTouchHelperCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}. Also initializes dataset with dbFunction
 */
private const val TAG = "RecyclerViewFragment"
private const val KEY_LAYOUT_MANAGER = "layoutManager"
private const val SPAN_COUNT = 2

class RecyclerViewFragment : Fragment(), OnStartDragListener {

    enum class LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private lateinit var currentLayoutManagerType: LayoutManagerType

    private lateinit var recyclerView: RecyclerView
    lateinit var recyclingStockAdapter: RecyclingStockAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var dataSet: List<Stock> = emptyList()

    private var callBackMainActivity: StockInterface? = null
    private var subscriptionToStockListUpdates: Disposable? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onResume() {
        super.onResume()

        if (callBackMainActivity == null) Log.d(TAG, "callBackMainActivity==null onResume")
        else callBackMainActivity?.getCompositeDisposable()?.remove(subscriptionToStockListUpdates!!)

        LocalBroadcastManager.getInstance(this.context!!.applicationContext)
                .registerReceiver(currentPriceReceiver, IntentFilter("PRICEUPDATE"))
    }

    override fun onPause() {
        super.onPause()

        if (callBackMainActivity == null) Log.d(TAG, "callBackMainActivity==null onPause")
        callBackMainActivity?.getCompositeDisposable()?.add(subscriptionToStockListUpdates!!)

        LocalBroadcastManager.getInstance(this.context!!.applicationContext)
                .unregisterReceiver(currentPriceReceiver)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context == null) Log.d(TAG, "context==null onAttach")
        try {
            if (context is MainActivity) {
                this.callBackMainActivity = context
                Log.v(TAG, "callBackMainActivity assigned in onAttach")

                subscriptionToStockListUpdates = callBackMainActivity?.getFlowingStockList()
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe(
                                { stockList -> recyclingStockAdapter.refresh(stockList)},
                                { throwable -> Log.d("Disposable::fail", throwable.message)}
                        )
            } else {
                Log.d(TAG, "context.applicationContext isn't MainActivity but rather $context")
            }
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("Activity must implement StockInterface")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.recycler_view_frag, container, false)
        rootView?.tag = TAG

        recyclerView = rootView!!.findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(activity)

        currentLayoutManagerType = if (savedInstanceState != null) {
            savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType
        } else {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context)
            val gridlayoutPref = sharedPref.getBoolean(resources.getString(R.string.gridlayout_key), false)
            if (gridlayoutPref) LayoutManagerType.GRID_LAYOUT_MANAGER else LayoutManagerType.LINEAR_LAYOUT_MANAGER
        }

        recyclerView.setHasFixedSize(true)
        setRecyclerViewLayoutManager(currentLayoutManagerType)

        recyclingStockAdapter = RecyclingStockAdapter(dataSet, this, this.context)
        recyclerView.adapter = recyclingStockAdapter

        return rootView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState?.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType)
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = SimpleItemTouchHelperCallback(recyclingStockAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    private fun setRecyclerViewLayoutManager(layoutManagerType: LayoutManagerType) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val lm = recyclerView.layoutManager as LinearLayoutManager
            scrollPosition = lm.findFirstCompletelyVisibleItemPosition()
        }

        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                layoutManager = GridLayoutManager(activity, SPAN_COUNT)
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                layoutManager = LinearLayoutManager(activity)
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(scrollPosition)
    }

    private val currentPriceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "PRICEUPDATE" -> {
                    val stockId = intent?.getLongExtra("stockid", -666)
                    val price = intent?.getDoubleExtra("currentprice", -666.0)
                    //val time = intent?.getStringExtra("time") ?: "not found"
                    //Log.v(TAG, "Received price update of $stockId as $price at $time")
                    //recyclingStockAdapter.setCurrentPrice(stockId!!, price!!, time!!)
                }
            }
        }
    }
}