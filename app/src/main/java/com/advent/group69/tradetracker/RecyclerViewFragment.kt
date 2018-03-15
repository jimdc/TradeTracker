package com.advent.group69.tradetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.helper.ItemTouchHelper
import com.advent.group69.tradetracker.model.Stock
import com.advent.group69.tradetracker.view.OnStartDragListener
import com.advent.group69.tradetracker.view.RecyclingStockAdapter
import com.advent.group69.tradetracker.view.SimpleItemTouchHelperCallback


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataset()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.recycler_view_frag, container, false)
        rootView?.tag = TAG

        recyclerView = rootView!!.findViewById(R.id.recyclerView)

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        layoutManager = LinearLayoutManager(activity)

        currentLayoutManagerType = if (savedInstanceState != null) {
            // Restore saved layout manager type.
            savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType
        } else {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context)
            val gridlayoutPref = sharedPref.getBoolean(resources.getString(R.string.gridlayout_key), false)
            if (gridlayoutPref) LayoutManagerType.GRID_LAYOUT_MANAGER else LayoutManagerType.LINEAR_LAYOUT_MANAGER
        }

        recyclerView.setHasFixedSize(true)
        setRecyclerViewLayoutManager(currentLayoutManagerType)

        recyclingStockAdapter = RecyclingStockAdapter(dataSet, this, this.context)

        // Set CustomAdapter as the adapter for RecyclerView.
        recyclerView.adapter = recyclingStockAdapter
        return rootView
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

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType)
        super.onSaveInstanceState(outState)
    }

    /**
     * Generates Stocks for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private fun initDataset() {
        //dbsBound would be true only when the recyclerview is recreated after hitting "back"
        //dataSet = dbFunctions.getStockList()
    }

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = SimpleItemTouchHelperCallback(recyclingStockAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

}