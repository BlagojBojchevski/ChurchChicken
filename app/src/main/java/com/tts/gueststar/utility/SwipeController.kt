package com.tts.gueststar.utility

import android.annotation.SuppressLint
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import android.view.MotionEvent

internal class SwipeController(private val swipeControllerInter: SwipeControllerInter) : ItemTouchHelper.Callback() {
    private var swipeBack = false

    interface SwipeControllerInter {
        fun left()

        fun right()
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, LEFT or RIGHT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(c: Canvas,
                             recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {

        if (actionState == ACTION_STATE_SWIPE) {
            if (dX > 0) {
                // then swiping right.
                swipeControllerInter.right()
            } else if (dX < 0) {
                // then swiping left.
                swipeControllerInter.left()
            }
            // If dX == 0 then at at start position.
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

        if (actionState == ACTION_STATE_SWIPE) {
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(c: Canvas,
                                 recyclerView: RecyclerView,
                                 viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float,
                                 actionState: Int, isCurrentlyActive: Boolean) {

        recyclerView.setOnTouchListener { v, event ->
            swipeBack = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            false
        }
    }

}