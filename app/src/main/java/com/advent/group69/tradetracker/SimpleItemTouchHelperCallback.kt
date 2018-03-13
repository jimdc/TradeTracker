package com.advent.group69.tradetracker

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags
import android.support.v7.widget.helper.ItemTouchHelper


class SimpleItemTouchHelperCallback(private val mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
               target: RecyclerView.ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition())
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemDismiss(viewHolder, viewHolder.getAdapterPosition())
    }

}