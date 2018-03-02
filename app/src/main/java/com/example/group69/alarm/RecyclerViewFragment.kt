package com.example.group69.alarm

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.example.group69.alarm.RecyclingStockAdapter

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class RecyclerViewFragment : Fragment() {

    final val TAG = "RecyclerViewFragment"
    final val KEY_LAYOUT_MANAGER = "layoutManager"
    final val SPAN_COUNT = 2
    final val DATASET_COUNT = 60

    enum class LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected lateinit var mCurrentLayoutManagerType: LayoutManagerType
    protected lateinit var mLinearLayoutRadioButton: RadioButton
    protected lateinit var mGridLayoutRadioButton: RadioButton

    protected lateinit var mRecyclerView: RecyclerView
    public lateinit var mAdapter: RecyclingStockAdapter
    protected lateinit var mLayoutManager: RecyclerView.LayoutManager
    protected lateinit var mDataset: List<Stock>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataset()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var rootView = inflater?.inflate(R.layout.recycler_view_frag, container, false)
        rootView?.setTag(TAG)

        mRecyclerView = rootView!!.findViewById(R.id.recyclerView)

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = LinearLayoutManager(getActivity())

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = savedInstanceState?.getSerializable(KEY_LAYOUT_MANAGER) as LayoutManagerType
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType)

        mAdapter = RecyclingStockAdapter(mDataset)

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter)

        mLinearLayoutRadioButton = rootView?.findViewById(R.id.linear_layout_rb) as RadioButton
        mLinearLayoutRadioButton.setOnClickListener {
            view -> setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER)
        }

        mGridLayoutRadioButton = rootView?.findViewById(R.id.grid_layout_rb) as RadioButton
        mGridLayoutRadioButton.setOnClickListener {
            view -> setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER)
        }

        return rootView
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public fun setRecyclerViewLayoutManager(layoutManagerType: LayoutManagerType) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            val lm = mRecyclerView.layoutManager as LinearLayoutManager
            scrollPosition = lm.findFirstCompletelyVisibleItemPosition()
        }

        when (layoutManagerType) {
            LayoutManagerType.GRID_LAYOUT_MANAGER -> {
                mLayoutManager = GridLayoutManager (getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
            }
            LayoutManagerType.LINEAR_LAYOUT_MANAGER -> {
                mLayoutManager = LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
            }
            else -> {
                mLayoutManager = LinearLayoutManager (getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
            }
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType)
        super.onSaveInstanceState(outState)
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private fun initDataset() {
        mDataset = listOf(Stock(1337, "BABA", 4.20, true, false, false),
                Stock(666, "HAHA", 4.20, true, false, false),
                Stock(1337, "NANA", 4.20, true, false, false))
    }

}