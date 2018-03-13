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
import android.widget.RadioButton
import android.support.v7.widget.helper.ItemTouchHelper
import com.advent.group69.tradetracker.R.id.recyclerView





/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}. Also initializes dataset with dbFunction
 */
class RecyclerViewFragment : Fragment(), OnStartDragListener {

    private val TAG = "RecyclerViewFragment"
    private val KEY_LAYOUT_MANAGER = "layoutManager"
    private val SPAN_COUNT = 2
    private val DATASET_COUNT = 60

    enum class LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected lateinit var mCurrentLayoutManagerType: LayoutManagerType
    protected lateinit var mLinearLayoutRadioButton: RadioButton
    protected lateinit var mGridLayoutRadioButton: RadioButton

    protected lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: RecyclingStockAdapter
    protected lateinit var mLayoutManager: RecyclerView.LayoutManager
    protected var mDataset: List<Stock> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataset()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.recycler_view_frag, container, false)
        rootView?.tag = TAG

        mRecyclerView = rootView!!.findViewById(R.id.recyclerView)

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = LinearLayoutManager(activity)

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType
        } else {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context)
            val gridlayoutPref = sharedPref.getBoolean(resources.getString(R.string.gridlayout_key), false)

            mCurrentLayoutManagerType = if (gridlayoutPref) LayoutManagerType.GRID_LAYOUT_MANAGER else LayoutManagerType.LINEAR_LAYOUT_MANAGER
        }

        mRecyclerView.setHasFixedSize(true)
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType)

        mAdapter = RecyclingStockAdapter(mDataset, this)

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.adapter = mAdapter
        return rootView
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    fun setRecyclerViewLayoutManager(layoutManagerType: LayoutManagerType) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.layoutManager != null) {
            val lm = mRecyclerView.layoutManager as LinearLayoutManager
            scrollPosition = lm.findFirstCompletelyVisibleItemPosition()
        }

        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                mLayoutManager = GridLayoutManager (activity, SPAN_COUNT)
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                mLayoutManager = LinearLayoutManager(activity)
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
            }
        }

        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.scrollToPosition(scrollPosition)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType)
        super.onSaveInstanceState(outState)
    }

    /**
     * Generates Stocks for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private fun initDataset() {
        //dbsBound would be true only when the recyclerview is recreated after hitting "back"
        mDataset = dbFunctions.getStocklistFromDB()
    }

    private var mItemTouchHelper: ItemTouchHelper? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = SimpleItemTouchHelperCallback(mAdapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(mRecyclerView)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }

}