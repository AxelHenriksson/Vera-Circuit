package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent

@Suppress("LeakingThis")
abstract class AbstractGame(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

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
        renderMode = RENDERMODE_WHEN_DIRTY  // TODO: Turn this back to RENDERMODE_WHEN_DIRTY

        Thread {
            renderer.userInterface = initUI()
        }.start()

        Thread {
            renderer.addAll(initLevel())
        }.start()

    }

    protected open fun initUI(): UserInterface {
        return UserInterface(this)
    }

    protected open fun initLevel(): List<Drawable> {
        return arrayListOf()
    }

    // Game Loop
    protected var tickPeriod: Long = 100  // Tick preiod in milliseconds: 100 => 10 updates a second
    protected abstract fun onTick()

    val mainHandler by lazy { Handler(Looper.getMainLooper()) }

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

    protected fun add(drawable: Drawable) {
        renderer.add(drawable)
    }

}