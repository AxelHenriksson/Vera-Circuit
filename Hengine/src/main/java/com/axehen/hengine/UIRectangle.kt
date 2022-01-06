package com.axehen.hengine

import android.opengl.GLES31
import android.opengl.Matrix
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * @param dimensions    The UIRectangle x/y dimensions in inches
 * @param margins       The UIRectangle x/y margins in inches
 * @param anchor        The UIRectangles anchor (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)
 */
class UIRectangle(var dimensions: Vec2, var margins: Vec2, var anchor: UIAnchor, private val shader: Shader) {

    // Create a basic plane spanning the screen that is later translated and scaled in the shader
    private val vertexCoords = floatArrayOf(
        -1f, -1f, 0f,
         1f, -1f, 0f,
         1f,  1f, 0f,
        -1f,  1f, 0f)
    private val drawOrder = intArrayOf(
        0, 1, 2,
        0, 2, 3,)


    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private val drawListBuffer: IntBuffer =
        // (# of coordinate values * 4 bytes per int)
        ByteBuffer.allocateDirect(drawOrder.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }


    init {
        if (vertexCoords.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex coordinate count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (drawOrder.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Draw order count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
    }


    fun load() {
        shader.loadTextures()
    }


    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    /**
     * @param screenDimensions The screen dimensions in inches
     */
    fun draw(screenDimensions: Vec2) {
        shader.bindTextures()

        // get handle to vertex shader's vPosition member. positionHandle is later used to disable its attribute array
        val positionHandle = GLES31.glGetAttribLocation(shader.id, "vPosition").also { handle ->
            GLES31.glEnableVertexAttribArray(handle)
            GLES31.glVertexAttribPointer(
                handle,
                3,
                GLES31.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        // Create a matrix to scale and move UIRectangle
        FloatArray(16).let { matrix ->
            Matrix.setIdentityM(matrix, 0)
            when (anchor) {
                UIAnchor.TOP_LEFT       -> Matrix.translateM(matrix, 0, -1f + 2*( margins.x+dimensions.x/2)/screenDimensions.x,  1f - 2*(margins.y+dimensions.y/2)/screenDimensions.y, 0f)
                UIAnchor.TOP_RIGHT      -> Matrix.translateM(matrix, 0,  1f + 2*(-margins.x-dimensions.x/2)/screenDimensions.x,  1f - 2*(margins.y+dimensions.y/2)/screenDimensions.y, 0f)
                UIAnchor.BOTTOM_LEFT    -> Matrix.translateM(matrix, 0, -1f + 2*( margins.x+dimensions.x/2)/screenDimensions.x, -1f + 2*(margins.y+dimensions.y/2)/screenDimensions.y, 0f)
                UIAnchor.BOTTOM_RIGHT   -> Matrix.translateM(matrix, 0,  1f + 2*(-margins.x-dimensions.x/2)/screenDimensions.x, -1f + 2*(margins.y+dimensions.y/2)/screenDimensions.y, 0f)
            }
            Matrix.scaleM(matrix, 0, dimensions.x/screenDimensions.x, dimensions.y/screenDimensions.y, 0f)
            GLES31.glUniformMatrix4fv(
                GLES31.glGetUniformLocation(shader.id, "mTransform"),
                1,
                false,
                matrix,
                0
            )
        }


        // Draw the triangle
        GLES31.glDrawElements(
            GLES31.GL_TRIANGLES,
            drawOrder.size,
            GLES31.GL_UNSIGNED_INT,
            drawListBuffer
        )

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        private val TAG = "hengine.Mesh.kt"
        private const val COORDS_PER_VERTEX = 3

        enum class UIAnchor {
            TOP_LEFT,
            TOP_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT,
        }
    }
}