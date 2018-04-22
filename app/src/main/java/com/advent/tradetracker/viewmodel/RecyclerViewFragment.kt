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
import com.advent.tradetracker.R
import com.advent.tradetracker.model.Stock
import com.advent.tradetracker.model.StockInterface
import com.advent.tradetracker.view.OnStartDragListener
import com.advent.tradetracker.view.RecyclingStockAdapter
import com.advent.tradetracker.view.SimpleItemTouchHelperCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}. Also initializes dataset with dbFunction
 */
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

        /**
         * See explanation in the "else" clause for why we don't do this memory release anymore.
         * It is replaced by the size() check on CompositeDisposable in onAttach

        if (callBackMainActivity == null) Log.d(TAG, "callBackMainActivity==null onResume")
        else {
            val sizeBefore = callBackMainActivity?.getCompositeDisposable()?.size()
            callBackMainActivity?.getCompositeDisposable()?.add(subscriptionToStockListUpdates!!)
            val sizeAfter = callBackMainActivity?.getCompositeDisposable()?.size()
            Log.v("RecyclerViewFragment", "onResume: added composite subscription; CD $sizeBefore -> $sizeAfter")

            /**
             * Sometimes the database will change when the RecyclerView is "paused"
             * So we need to check when we get back, if we are in sync.
             * This froze the app so we'll just not unsubscribe...


            val mostUpdatedList = callBackMainActivity?.getFlowingStockList()?.blockingLast()
            if (mostUpdatedList != null) {
                recyclingStockAdapter.refresh(mostUpdatedList)
            }
            */
        }
        */

        LocalBroadcastManager.getInstance(this.context!!.applicationContext)
                .registerReceiver(currentPriceReceiver, IntentFilter("PRICEUPDATE"))
    }

    override fun onPause() {
        super.onPause()

        /**
         * see explanation in onResume for why we don't do this anymore

        if (callBackMainActivity == null) Log.d(TAG, "callBackMainActivity==null onPause")
        else {
            val sizeBefore = callBackMainActivity?.getCompositeDisposable()?.size()
            callBackMainActivity?.getCompositeDisposable()?.remove(subscriptionToStockListUpdates!!)
            val sizeAfter = callBackMainActivity?.getCompositeDisposable()?.size()
            Log.v("RecyclerViewFragment", "onPause: removed composite subscription; CD $sizeBefore -> $sizeAfter")
        }
        */

        LocalBroadcastManager.getInstance(this.context!!.applicationContext)
                .unregisterReceiver(currentPriceReceiver)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context == null) Timber.d("context==null onAttach")
        try {
            if (context is MainActivity) {
                this.callBackMainActivity = context
                Timber.v( "callBackMainActivity assigned in onAttach")

                subscriptionToStockListUpdates = callBackMainActivity?.getFlowingStockList()
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe(
                                { stockList -> recyclingStockAdapter.refresh(stockList)},
                                { throwable -> Timber.d(throwable.message)}
                        )

                /**
                 * The logic here needs to change if Updaten subscribes to flowable, which it should!
                 */
                val subscriptionCount = callBackMainActivity?.getCompositeDisposable()?.size()
                if (subscriptionCount == 0) {

                    callBackMainActivity?.getCompositeDisposable()?.add(subscriptionToStockListUpdates!!)
                    val newSubscriptionCount = callBackMainActivity?.getCompositeDisposable()?.size()
                    if (newSubscriptionCount == 1) {
                        Timber.v( "Successfully attached subscriptionToStockListUpdates bc has 0")
                    } else if (newSubscriptionCount == 0) {
                        Timber.d( "I could not subscribe to stock list updates; changes might not be reflected in UI!")
                    }
                } else {
                    Timber.v( "Not subscribing to StockListUpdates bc already has one subscription (but may not be ours!)")
                }

            } else {
                Timber.d("context.applicationContext isn't MainActivity but rather $context")
            }
        } catch (classCastException: ClassCastException) {
            throw ClassCastException("Activity must implement StockInterface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.recycler_view_frag, container, false)

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
        outState.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType)
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
                    val stockId = intent.getLongExtra("stockid", -666)
                    val price = intent.getDoubleExtra("currentprice", -666.0)
                    val time = intent.getStringExtra("time") ?: "not found"
                    Timber.v("Received price update of $stockId as $price at $time")
                    recyclingStockAdapter.setCurrentPrice(stockId, price, time)
                }
            }
        }
    }
}