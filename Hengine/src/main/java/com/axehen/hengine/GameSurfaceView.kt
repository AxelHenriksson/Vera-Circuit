package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.pow
import kotlin.math.sqrt

@Suppress("LeakingThis")
open class GameSurfaceView(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

    val renderer: GameRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        glEnable( GL_DEPTH_TEST )
        glDepthFunc( GL_LEQUAL )
        glDepthMask( true )

        renderer = GameRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY

    }

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var prevDist: Float = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val dist: Float = if (event.pointerCount == 2) sqrt((event.getX(0)-event.getX(1)).pow(2) + (event.getY(0)-event.getY(1)).pow(2)) else 0f

        //Log.d("GameSurfaceView",event.toString())
        when(event.action) {
            MotionEvent.ACTION_MOVE -> {

                val dx: Float = x - previousX
                val dy: Float = y - previousY
                val dDist: Float = dist - prevDist

                when (event.pointerCount) {
                    1 -> {
                        renderer.touchSwipe(dx / height.toFloat(), dy / height.toFloat())
                        requestRender()
                    }
                    2 -> {
                        renderer.touchPinch(dDist / height.toFloat())
                        requestRender()
                    }
                }

            }
        }
        previousX = x
        previousY = y
        prevDist = dist

        return true
    }
}