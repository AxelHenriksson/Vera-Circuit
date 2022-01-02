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
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        glEnable( GL_DEPTH_TEST )
        glDepthFunc( GL_LEQUAL )
        glDepthMask( true )

        renderer = GameRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY

    }
}