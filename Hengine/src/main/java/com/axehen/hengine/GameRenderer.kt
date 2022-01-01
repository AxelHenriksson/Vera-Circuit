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

    /** Adds a mesh into the renderer's pipeline **/
    fun add(mesh: Mesh) { newMeshes.add(mesh) }
    fun addAll(vararg meshes: Mesh) { newMeshes.addAll(meshes) }


    // List of new meshes not yet added into the rendering pipeline
    private val newMeshes: ArrayList<Mesh> = ArrayList()
    // List of meshes in the rendering pipeline
    private val meshes: ArrayList<Mesh> = ArrayList()

    // Singleton rendering object functions
    /**
     * Keeps track of loaded bitmaps, returns the existing bitmap if the resource has been loaded, loads it if not
     * @param bitmapRes Bitmap resource ID
     * @return The decoded Bitmap object
     */
    fun getBitmap(bitmapRes: Int): Bitmap {
        return if (bitmaps.containsKey(bitmapRes) && bitmaps[bitmapRes] != null)
            bitmaps[bitmapRes]!!
        else {
            val opts = BitmapFactory.Options()
            opts.inScaled = false
            opts.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
            BitmapFactory.decodeResource(context.resources, bitmapRes, opts).also {
                bitmaps[bitmapRes] = it
                Log.d(TAG, "bitmap got with colorSpace: ${it.colorSpace}, width: ${it.width}, height: ${it.height}, allocationByteCount: ${it.allocationByteCount}, byteCount: ${it.byteCount}, turned into ByteBuffer")
            }
        }
    }
    private val bitmaps: HashMap<Int, Bitmap> = HashMap()

    /**
     * Returns a bytebuffer of a specified bitmap, either newly loaded or fetched from old
     * @param bitmapRes Bitmap resource ID
     * @return The decoded bitmap in a ByteBuffer
     */
    fun getBitmapBuffer(bitmapRes: Int): ByteBuffer {
        return with (getBitmap(bitmapRes)) {
            ByteBuffer.allocateDirect(this.allocationByteCount).also {
                this.copyPixelsToBuffer(it)
                it.position(0)
            }
        }
    }

    /**
     * Loads a Shader object into GLES memory and returns its ID
     */
    fun loadShader(shader: Shader): Int {
        if (shaders.containsKey(shader)) return shaders[shader]!!

        // create empty OpenGL ES Program
        val id = glCreateProgram()
        Log.d(TAG, "getShaderProgram, program id: $id")

        val vertexShader: Int = Shader.loadShaderFromResource(context, GL_VERTEX_SHADER, shader.vertexShaderRes)
        Log.d(TAG, "getShaderProgram, vertexShader id: $vertexShader")
        val fragmentShader: Int = Shader.loadShaderFromResource(context, GL_FRAGMENT_SHADER, shader.fragmentShaderRes)
        Log.d(TAG, "getShaderProgram, fragmentShader id: $fragmentShader")

        // add the vertex shader to program
        glAttachShader(id, vertexShader)
        Log.d(TAG, "Attaching vertexShader $vertexShader")

        // add the fragment shader to program
        glAttachShader(id, fragmentShader)
        Log.d(TAG, "Attaching fragmentShader $fragmentShader")

        // creates OpenGL ES program executables
        glLinkProgram(id)
        Log.d(TAG, "Linking program $id")

        val linkStatus = IntArray(1)
        glGetProgramiv(id, GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GL_TRUE) {
            glDeleteProgram(id)
            throw RuntimeException(
                "Could not link program: "
                        + glGetProgramInfoLog(id)
            )
        }
        shaders[shader] = id
        return id
    }
    private val shaders: HashMap<Shader, Int> = HashMap()


    // Touch responses
    /**
     * Renderer single finger move event response
     * @param dx change in finger x coordinate divided by screen resolution
     * @param dy change in finger y coordinate divided by screen resolution
     */
    fun touchSwipe(dx: Float, dy: Float) {
        eyePos.x = (eyePos.x - eyePos.z * zoom * dx)
        eyePos.y = (eyePos.y + eyePos.z * zoom * dy)
        lookAt.x = (lookAt.x - eyePos.z * zoom * dx)
        lookAt.y = (lookAt.y + eyePos.z * zoom * dy)
        updateView()
    }

    /**
     * Renderer two finger pinch event response
     * @param dDist change in finger distance divided by screen resolution
     */
    fun touchPinch(dDist: Float) {
        eyePos.z = (eyePos.z - 7*dDist).coerceIn(0.5f, 6.5f)
        updateView()
        //renderer.zoom = (renderer.zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
    }

    /** Updates view matrix and camPos uniforms in shaders from applicable instance variables */
    fun updateView() {
        // Calculate new viewMatrix
        Matrix.setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, 0f, 0f, 1f)

        // Enter eyePos into GLES:able buffer
        camPosBuffer.let { buffer ->
            buffer.put(floatArrayOf(eyePos.x, eyePos.y, eyePos.z))
            buffer.position(0)
        }

        // Update shader camera position and view matrix uniforms in all shaders
        // This was not able to replace the same procedure in onDrawFrame, thus it is useless. If possible, moving this call out of onDrawFrame could be beneficial
//        for (id in shaders.values) {
//            glUniform3fv(glGetUniformLocation(id, "vCamPos"), 1, camPosBuffer)
//            glUniformMatrix4fv(glGetUniformLocation(id, "mView"), 1, false, viewMatrix, 0)
//        }
    }
    private val viewMatrix = FloatArray(16)
    @Volatile   //TODO: Check if Volatile is necessary
    var eyePos: Vec3 = Vec3(0f, -1f, 5f)
    @Volatile   //TODO: Check if Volatile is necessary
    var lookAt: Vec3 = Vec3(0f, 0f, 0.2f)
    private val camPosBuffer: FloatBuffer = FloatBuffer.allocate(3).also { buffer ->
        buffer.put(floatArrayOf(eyePos.x, eyePos.y, eyePos.z))
        buffer.position(0)
    }

    private fun updateProjectionMatrix(width: Int, height: Int) {
        // Update the projection matrix with new ratio and zoom
        val ratio = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio*zoom, ratio*zoom, -1f*zoom, 1f*zoom, 1f, 50f)
        //Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 10f) // Orthographic projection matrix

        // Pass the new projection matrix to the proj. matrix uniform of all shaders
        // This call was not able to replace the same call in onDrawFrame, thus we probably should remove it
//        for (id in shaders.values)
//            glUniformMatrix4fv(glGetUniformLocation(id, "mProjection"), 1, false, projectionMatrix, 0)
    }
    private val projectionMatrix = FloatArray(16)
    @Volatile
    var zoom: Float = 1.0f



    //  Renderer functions
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background color
        glClearColor(1.0f, 0f, 0f, 1.0f)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDepthMask( true )

        updateView()

        //meshes.forEach { it.load() }  There are no longer any meshes in 'meshes' on surface creation, only in 'newMeshes'
    }

    override fun onDrawFrame(unused: GL10) {
        while (newMeshes.isNotEmpty()) {
            val mesh = newMeshes.removeFirst()
            mesh.load()
            meshes.add(mesh)
        }

        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // TODO: Remove or move, we do not want to recalculate matrices each frame, we may want to pass matrices to uniforms each frame tho
        shaders.values.forEach { id ->
            glUniformMatrix4fv(glGetUniformLocation(id, "mProjection"), 1, false, projectionMatrix, 0)


            glUniform3fv(glGetUniformLocation(id, "vCamPos"), 1, camPosBuffer)
            glUniformMatrix4fv(glGetUniformLocation(id, "mView"), 1, false, viewMatrix, 0)
        }


        meshes.forEach {it.draw()}
    }


    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        updateProjectionMatrix(width, height)
    }


    companion object {
        private const val TAG = "GameRenderer"
    }

}