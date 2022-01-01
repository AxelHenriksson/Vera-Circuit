package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.pow
import kotlin.math.sqrt

open class GameSurfaceView(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

    private val renderer: GameRenderer

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
                        //renderer.eyePos.x = (renderer.eyePos.x - renderer.eyePos.z * renderer.zoom * dx / height.toFloat()).coerceIn(renderer.lookAt.x - 2f, renderer.lookAt.x + 2f)
                        //renderer.eyePos.y = (renderer.eyePos.y + renderer.eyePos.z * renderer.zoom * dy / height.toFloat()).coerceIn(renderer.lookAt.y - 2f, renderer.lookAt.y + 2f)
                        //renderer.lookAt.x = (renderer.lookAt.x - renderer.eyePos.z * renderer.zoom * dx / height.toFloat()).coerceIn(-1f, 1f)
                        //renderer.lookAt.y = (renderer.lookAt.y + renderer.eyePos.z * renderer.zoom * dy / height.toFloat()).coerceIn(-1f, 1f)
                        //renderer.theta = renderer.theta - 180f*dx / height.toFloat()
                        //renderer.phi = (renderer.phi - 180f*dy / height.toFloat()).coerceIn(1f, 90f)
                        //requestRender()
                    }
                    2 -> {
                        //renderer.eyePos.x = (renderer.eyePos.x - 10f * dx/height.toFloat()).coerceIn(renderer.lookAt.x - 2f, renderer.lookAt.x + 2f)
                        //renderer.eyePos.y = (renderer.eyePos.y + 10f * dy/width.toFloat()).coerceIn(renderer.lookAt.y - 2f, renderer.lookAt.y + 2f)
                        //renderer.eyePos.z = (renderer.eyePos.z - 0.005f * dDist).coerceIn(0.5f, 6.5f)
                        //renderer.zoom = (renderer.zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
                        //requestRender()
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