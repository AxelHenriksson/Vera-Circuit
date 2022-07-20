package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    internal var userInterface: UserInterface? = null

    /** List of new drawables yet to be loaded into the rendering pipeline **/
    private val newDrawables: ArrayList<Drawable> = ArrayList()
    /** List of drawables loaded into the rendering pipeline and being drawn **/
    private val drawables: ArrayList<Drawable> = ArrayList()
    /** Loads all drawables in [newDrawables] and moves them into  [drawables] **/
    private fun loadNewDrawables() {
        while (newDrawables.isNotEmpty()) {
            val drawable = newDrawables.removeFirst()
            drawable.load()
            drawables.add(drawable)
            Log.d("Drawables", "Drawable loaded, drawables.size=${drawables.size}, newDrawables.size=${newDrawables.size}")
        }
    }

    /** Adds a drawable into the renderer's pipeline **/
    fun add(drawable: Drawable) { newDrawables.add(drawable) }
    fun addAll(vararg drawables: Drawable) { newDrawables.addAll(drawables) }
    fun addAll(drawables: List<Drawable>) { newDrawables.addAll(drawables) }
    fun remove(drawable: Drawable) { toRemove.add(drawable) }   // We should probably do something before we remove it so as not to leak memory
    private val toRemove = HashSet<Drawable>()

    /** Set of loaded shader IDs **/
    private val shaders = HashMap<Shader, Int>()


    /** Loads a Shader object into GLES memory and returns its ID **/
    internal fun loadShader(shader: Shader): Int {
        if(shaders.containsKey(shader)) return shaders[shader]!!

        val id = glCreateProgram()
        val vertexShader: Int = Shader.loadShaderFromAsset(context, GL_VERTEX_SHADER, shader.asset + ".vert")
        Shader.checkCompileErrors(vertexShader, GL_VERTEX_SHADER)

        val fragmentShader: Int = Shader.loadShaderFromAsset(context, GL_FRAGMENT_SHADER, shader.asset + ".frag")
        Shader.checkCompileErrors(fragmentShader, GL_FRAGMENT_SHADER)

        glAttachShader(id, vertexShader)
        glAttachShader(id, fragmentShader)

        glLinkProgram(id)
        Shader.checkLinkErrors(id)

        shaders[shader] = id
        return id
    }

    /** Updates view matrix and camPos uniforms in shaders from applicable instance variables */
    fun updateView() {
        val eyePos = lookFrom + lookAt
        // Calculate new viewMatrix
        Matrix.setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, 0f, 0f, 1f)
    }
    private val viewMatrix = FloatArray(16)
    //@Volatile   //TODO: Check if Volatile is necessary
    var lookFrom = Vec3(0f, 0f, 1f) //@Synchronized get @Synchronized set
    //@Volatile   //TODO: Check if Volatile is necessary
    var lookAt: Vec3 = Vec3(0f, 0f, 0f) //@Synchronized get @Synchronized set

    private fun updateProjectionMatrix(width: Int, height: Int) {
        val ratio = width.toFloat() / height.toFloat()
        val clipDistanceFactor = 10f    // TODO: Make this adjustable by implementation
        Matrix.frustumM(projectionMatrix, 0, -ratio*zoom*clipDistanceFactor/2, ratio*zoom*clipDistanceFactor/2, -1f*zoom*clipDistanceFactor/2, 1f*zoom*clipDistanceFactor/2, 0.1f*clipDistanceFactor, 50f*clipDistanceFactor)
        // Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 10f) // Orthographic projection matrix
    }
    private val projectionMatrix = FloatArray(16)
    //@Volatile
    var zoom: Float = 1.0f



    //  Renderer functions
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background color
        glClearColor(1.0f, 0f, 0f, 1.0f)

        updateView()

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDepthMask( true )

        userInterface?.load()
    }

    override fun onDrawFrame(unused: GL10) {

        drawables.removeAll(toRemove)
        toRemove.clear()
        loadNewDrawables()


        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val viewMatrix = this.viewMatrix.copyOf()
        val projectionMatrix = this.projectionMatrix.copyOf()
        val camPosBuffer = FloatBuffer.allocate(3).also {
            it.put(floatArrayOf(lookFrom.x + lookAt.x, lookFrom.y + lookAt.y, lookFrom.z + lookAt.z))
            it.position(0)
        }

        // TODO: See if it is possible to move uniform passing to a more seldom executed method
        for(id in shaders.values) {
            glUseProgram(id)
            glUniform3fv(glGetUniformLocation(id, "vCamPos"), 1, camPosBuffer)
            glUniformMatrix4fv(glGetUniformLocation(id, "mProjection"), 1, false, projectionMatrix, 0)
            glUniformMatrix4fv(glGetUniformLocation(id, "mView"), 1, false, viewMatrix, 0)
        }

        drawables.forEach {it.draw()}

        userInterface?.let {
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            it.draw()
            glDisable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
    }


    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        updateProjectionMatrix(width, height)
    }

}