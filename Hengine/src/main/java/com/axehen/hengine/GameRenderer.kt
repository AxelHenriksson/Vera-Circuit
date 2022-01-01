package com.axehen.hengine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.opengl.GLES31
import android.opengl.GLES31.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLSurfaceView
import android.util.Log
import java.lang.RuntimeException
import java.nio.ByteBuffer

import java.util.*
import kotlin.collections.HashMap


class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {

    fun add(mesh: Mesh) {
        newMeshes.add(mesh)
    }
    private val newMeshes: ArrayList<Mesh> = ArrayList()
    val meshes: ArrayList<Mesh> = ArrayList()

    // Singleton rendering object functions
    /**
     * Keeps track of loaded bitmaps, returns the existing bitmap if the resource has been loaded, loads it if not
     *
     * @param bitmapRes Bitmap resource ID
     * @return The decoded bitmap in a ByteBuffer
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
    fun getBitmapBuffer(bitmapRes: Int): ByteBuffer {
        return with (getBitmap(bitmapRes)) {
            ByteBuffer.allocateDirect(this.allocationByteCount).also {
                this.copyPixelsToBuffer(it)
                it.position(0)
            }
        }
    }

    fun loadShader(shader: Shader): Int {
        if (shaders.containsKey(shader)) return shaders[shader]!!

        // create empty OpenGL ES Program
        val id = glCreateProgram()
        Log.d(TAG, "getShaderProgram, program id: $id")

        val vertexShader: Int = loadShaderFromResource(context, GL_VERTEX_SHADER, shader.vertexShaderRes)
        Log.d(TAG, "getShaderProgram, vertexShader id: $vertexShader")
        val fragmentShader: Int = loadShaderFromResource(context, GL_FRAGMENT_SHADER, shader.fragmentShaderRes)
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



    //  Renderer functions
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background color
        glClearColor(1.0f, 0f, 0f, 1.0f)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LEQUAL)
        glDepthMask( true )

        //meshes.forEach { it.load() }
    }

    private val PI: Float = 3.1415F
    @Volatile
    var theta: Float = 45f   // Horizontal eyePos angle
    @Volatile
    var phi: Float = 45f  // Vertical eyePos angle
    //@Volatile
    //var eyePos: Vec3 = Vec3(0f, -3f, 1f)
    //@Volatile
    //var lookAt: Vec3 = Vec3(0f, 0f, 0.2f)

    //var zoom: Float = 1.0f

    override fun onDrawFrame(unused: GL10) {
//        while (newMeshes.isNotEmpty()) {
//            val mesh = newMeshes.removeFirst()
//            mesh.load()
//            meshes.add(mesh)
//        }
//
//        // Redraw background color
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
//
//        val eyePos = Vec3(6f*cos(theta*PI/180f)*sin(phi*PI/180f), 6f*sin(theta*PI/180f)*sin(phi*PI/180f), 0.5f + 6f*cos(phi*PI/180f))
//        // Set the camera position (View matrix)
//        Matrix.setLookAtM(viewMatrix, 0, eyePos.x, eyePos.y, eyePos.z, lookAt.x, lookAt.y, lookAt.z, 0f, 0f, 1f)
//        // Calculate projection matrix
//        Matrix.frustumM(projectionMatrix, 0, -ratio*zoom, ratio*zoom, -1f*zoom, 1f*zoom, 2f, 50f)
//
//        for (id in shaders.values) {
//
//            // Update camera position uniforms of all shaders //TODO: Move to seperate onCameraMoved method for optimization
//            glGetUniformLocation(id, "vCamPos").let { handle ->
//                FloatBuffer.allocate(3).let { buffer ->
//                    buffer.put(floatArrayOf(eyePos.x, eyePos.y, eyePos.z))
//                    buffer.position(0)
//                    glUniform3fv(handle,1, buffer)
//                }
//            }
//
//            glGetUniformLocation(id, "mView").let { handle ->
//                glUniformMatrix4fv(handle, 1, false, viewMatrix, 0)
//            }
//            glGetUniformLocation(id, "mProjection").let { handle ->
//                glUniformMatrix4fv(handle, 1, false, projectionMatrix, 0)
//            }
//        }
//
//        meshes.forEach {it.draw()}
    }
//
//    // vPMatrix is an abbreviation for "Model View ProjectionMAtrix"
//    //private val vPMatrix = FloatArray(16)
//    private val projectionMatrix = FloatArray(16)
//    private val viewMatrix = FloatArray(16)
//    private var ratio: Float = 1.0f
//
    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
//
//        ratio = width.toFloat() / height.toFloat()
//
//
//        // this projection matrix is applied to object coordinates in the onDrawFrame() method
//        Matrix.frustumM(projectionMatrix, 0, -ratio*zoom, ratio*zoom, -1f*zoom, 1f*zoom, 1f, 50f)
//        //Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 10f)
//
    }



    companion object {
        private const val TAG = "GameRenderer"

        /**
         * Creates and compiles an openGL shader into GLES memory from a string of shader code
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param shaderCode    Bare string containing the shaders code
         * @return The compiled shader's id
         */
        private fun loadShader(type: Int, shaderCode: String): Int {
            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            val shader = GLES31.glCreateShader(type)

            // add the source code to the shader and compile it
            GLES31.glShaderSource(shader, shaderCode)
            GLES31.glCompileShader(shader)

            Log.d(TAG, "loadShader on $shaderCode returns $shader with infolog: ${GLES31.glGetShaderInfoLog(shader)}")
            return shader
        }

        /**
         * Reads a shader text file from a resource ID and creates and compiles the shader into GLES memory
         * @param context       The current context, required to read resource
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param resId         Resource ID of the shader text file
         * @return The compiled shader's id
         */
        fun loadShaderFromResource(context: Context, type: Int, resId: Int): Int = loadShader(type, Utils.getStringFromResource(context, resId))
    }

}