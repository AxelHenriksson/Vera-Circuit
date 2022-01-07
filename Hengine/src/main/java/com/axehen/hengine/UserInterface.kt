package com.axehen.hengine

import android.util.Log
import android.view.MotionEvent

class UserInterface(var screenDimensions: Vec2, var screenResolution: Vec2): Drawable{

    val elements = ArrayList<UIRectangle>()
    val buttons = ArrayList<UIRectangle.UIButton>()


    override fun load() {
        elements.forEach { it.load() }
        buttons.forEach { it.load() }
    }

    override fun draw() {
        elements.forEach { it.draw(screenDimensions) }
        buttons.forEach { it.draw(screenDimensions) }
    }


    /**
     * @return The button that was touched, if any was, otherwise null
     */
    fun getTouched(event: MotionEvent): Boolean {
        val xInches = event.x*screenDimensions.x/screenResolution.x
        val yInches = screenDimensions.y*(1 - event.y/screenResolution.y)
        //Log.d(TAG, "UserInterface touchEvent() called with xInches=$xInches, yInches=$yInches")

        Log.d(TAG, "touched: xInches=$xInches, yInches=$yInches")

        for (button in buttons) {
            //Log.d(TAG, "Button tested")
            if (button.touch(Vec2(xInches, yInches), screenDimensions, event)) {
                //Log.d(TAG, "button touched at xInches=$xInches, yInches=$yInches")
                    when(event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isHoldingButton = true
                            button.isPressed = true
                        }
                        MotionEvent.ACTION_UP -> {
                            isHoldingButton = false
                            button.isPressed = false
                        }
                    }
                return true
            }
        }
        // No new event was registered on the UI, but the UI consumes the event if a button is being held meaning we return isHoldingButton
        return isHoldingButton
    }
    private var isHoldingButton = false

    companion object {
        private const val TAG = "hengine.UserInterface.kt"
    }
}