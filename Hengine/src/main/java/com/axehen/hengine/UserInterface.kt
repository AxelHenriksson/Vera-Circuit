package com.axehen.hengine

import android.util.Log
import android.view.MotionEvent

class UserInterface(private val game: AbstractGame): Drawable{

    val elements = ArrayList<UIRectangle>()
    val buttons = ArrayList<UIRectangle.UIButton>()

    var scale = 1f

    override fun load() {
        elements.forEach { it.load() }
        buttons.forEach { it.load() }
    }

    override fun draw() {
        elements.forEach { it.draw(game.width / scale, game.height / scale) }
        buttons.forEach { it.draw(game.width / scale, game.height / scale) }
    }


    /**
     * @return The button that was touched, if any was, otherwise null
     */
    fun getTouched(event: MotionEvent): Boolean {
        //Log.d(TAG, "UserInterface touchEvent() called with xInches=$xInches, yInches=$yInches")

        for (button in buttons) {
            //Log.d(TAG, "Button tested")
            if (button.touch(event, game.width, game.height, scale)) {
                //Log.d(TAG, "button touched at xInches=$xInches, yInches=$yInches")
                    when(event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isHoldingButton = true
                        }
                        MotionEvent.ACTION_UP -> {
                            isHoldingButton = false
                        }
                    }
                game.requestRender() // Request new render so that the button press is visible
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