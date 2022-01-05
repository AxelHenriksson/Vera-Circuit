package com.axehen.hengine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.opengl.GLES31.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.FloatBuffer

import java.util.*
import kotlin.collections.HashMap


class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    var onDrawCallback: (() -> Unit)? = null

    /** Adds a drawable into the renderer's pipeline **/
    fun add(drawable: Drawable) { newDrawables.add(drawable) }
    fun addAll(vararg drawables: Drawable) { newDrawables.addAll(drawables) }


    // List of new drawables not yet added into the rendering pipeline
    private val newDrawables: ArrayList<Drawable> = ArrayList()
    // List of drawables in the rendering pipeline
    private val drawables: ArrayList<Drawable> = ArrayList()

    // Singleton rendering object functions
    /**
     * Loads a Shader object into GLES memory and returns its ID
     */
    fun loadShader(shader: Shader): Int {
        return if (shaders.containsKey(shader)) shaders[shader]!!
        else glCreateProgram().also { id ->

            val vertexShader: Int = Shader.loadShaderFromAsset(context, GL_VERTEX_SHADER, shader.shaderAsset + ".vert")
            checkCompileErrors(vertexShader, GL_VERTEX_SHADER)

            val fragmentShader: Int = Shader.loadShaderFromAsset(context, GL_FRAGMENT_SHADER, shader.shaderAsset + ".frag")
            checkCompileErrors(fragmentShader, GL_FRAGMENT_SHADER)

            glAttachShader(id, vertexShader)
            glAttachShader(id, fragmentShader)

            glLinkProgram(id)
            checkLinkErrors(id)

            shaders[shader] = id
        }
    }
    private val shaders: HashMap<Shader, Int> = HashMap()


    /** Updates view matrix and camPos uniforms in shaders from applicable instance variables */
    fun updateView() {
        val eyePos = lookFrom + lookAt
        // Calculate new viewMatrix
        Matrix.setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, 0f, 0f, 1f)

        // Enter eyePos into GLES:able buffer
        camPosBuffer.let { buffer ->
            buffer.put(floatArrayOf(eyePos.x, eyePos.y, eyePos.z))
            buffer.position(0)
        }
    }
    private val viewMatrix = FloatArray(16)
    @Volatile   //TODO: Check if Volatile is necessary
    var lookFrom = Vec3(0f, 0f, 1f)
    @Volatile   //TODO: Check if Volatile is necessary
    var lookAt: Vec3 = Vec3(0f, 0f, 0f)
    private val camPosBuffer: FloatBuffer = FloatBuffer.allocate(3).also { buffer ->
        buffer.put(floatArrayOf(lookFrom.x + lookAt.x, lookFrom.y + lookAt.y, lookFrom.z + lookAt.z))
        buffer.position(0)
    }

    private fun updateProjectionMatrix(width: Int, height: Int) {
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio*zoom, ratio*zoom, -1f*zoom, 1f*zoom, 1f, 50f)
        // Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 10f) // Orthographic projection matrix
    }
    private val projectionMatrix = FloatArray(16)
    @Volatile
    var zoom: Float = 1.0f



    //  Renderer functions
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background color
        glClearColor(1.0f, 0f, 0f, 1.0f)

        updateView()

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDepthMask( true )

        //drawables.forEach { it.load() }  There are no longer any drawables in 'drawables' on surface creation, only in 'newMeshes'
    }

    override fun onDrawFrame(unused: GL10) {
        while (newDrawables.isNotEmpty()) {
            val drawable = newDrawables.removeFirst()
            drawable.load()
            drawables.add(drawable)
        }

        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // TODO: See if it is possible to move uniform passing to a more seldom executed method
        shaders.values.forEach { id ->
            glUseProgram(id)
            glUniformMatrix4fv(glGetUniformLocation(id, "mProjection"), 1, false, projectionMatrix, 0)

            glUniform3fv(glGetUniformLocation(id, "vCamPos"), 1, camPosBuffer)
            glUniformMatrix4fv(glGetUniformLocation(id, "mView"), 1, false, viewMatrix, 0)
        }

        onDrawCallback?.invoke()

        drawables.forEach {it.draw()}
    }


    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        updateProjectionMatrix(width, height)
    }


    companion object {
        private const val TAG = "GameRenderer"

        private fun checkCompileErrors(id: Int, type: Int) {
            val compileStatus = IntArray(1)
            glGetShaderiv(id, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] != GL_TRUE) {
                glDeleteShader(id)
                throw RuntimeException(
                    "Could not compile ${if(type == GL_VERTEX_SHADER) "vertex shader" else if(type == GL_FRAGMENT_SHADER) "fragment shader" else "shader"}: "
                            + glGetShaderInfoLog(id)
                )
            }
        }
        private fun checkLinkErrors(id: Int) {
            val linkStatus = IntArray(1)
            glGetProgramiv(id, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                glDeleteProgram(id)
                throw RuntimeException(
                    "Could not link program: "
                            + glGetProgramInfoLog(id)
                )
            }
        }
    }

}