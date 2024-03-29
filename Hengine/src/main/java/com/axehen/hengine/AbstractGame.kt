package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent


// parameter context and attr is required to use AbstractGame extending classes as android Views
abstract class AbstractGame(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

    protected val renderer: GameRenderer

    val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    protected abstract var tickPeriod: Long  // Tick time period in milliseconds: 100 => 10 updates a second


    init {
        // Create an OpenGL ES 3.0 context
        this.setEGLContextClientVersion(3)
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        glEnable( GL_DEPTH_TEST )
        glDepthFunc( GL_LEQUAL )
        glDepthMask( true )

        renderer = GameRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY

    }

    protected fun setUI(userInterfaceLambda: () -> UserInterface) {
        Thread {
            renderer.add(userInterfaceLambda.invoke())
        }.start()
    }

    protected fun loadDrawable(drawableLoadLambda: () -> Drawable) {
        Thread {
            renderer.add(drawableLoadLambda.invoke())
        }.start()
    }

    // Game Loop
    protected abstract fun onTick()

    private val tickRunnable = object : Runnable {
        override fun run() {
            onTick()
            mainHandler.postDelayed(this, tickPeriod)
        }
    }

    fun startTick() {
        mainHandler.post(tickRunnable)
    }
    fun stopTick() {
        mainHandler.removeCallbacks(tickRunnable)
    }

    // Touch Handling
    abstract fun onTouchWorld(event: MotionEvent)
    override fun onTouchEvent(event: MotionEvent): Boolean {

        // If the UI consumes the touch, do nothing and return true, if it does not, "touch" the world
        renderer.userInterface?.let { ui ->
            if (!ui.getTouched(event))
                onTouchWorld(event)
        }

        return true
    }



}