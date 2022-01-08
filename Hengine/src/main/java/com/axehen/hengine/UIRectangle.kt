package com.axehen.hengine

import android.opengl.GLES31
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
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
open class UIRectangle(var dimensions: Vec2, var margins: Vec2, var anchor: UIAnchor, protected val shader: Shader) {
    interface UICollidable {
        fun isWithin(buttonOrigin: Vec2, touchPos: Vec2): Boolean
    }
    class CircleUICollidable(private val radius: Float): UICollidable {
        override fun isWithin(buttonOrigin: Vec2, touchPos: Vec2): Boolean = (buttonOrigin - touchPos).length() <= radius
    }
    class UIButton(dimensions: Vec2, margins: Vec2, anchor: UIAnchor, shader: Shader, val collidable: UICollidable, val action: (pressed: Int) -> Unit): UIRectangle(dimensions, margins, anchor, shader) {
        var isPressed = false

        /**
         * @param x X touch coordinate in inches
         * @param y Y touch coordinate in inches
         * @return true if the touch was on the element, false otherwise
         */
        fun touch(touchPos: Vec2, screenDimensions: Vec2, event: MotionEvent): Boolean {
            Log.d("UIButton", "button.touch() called")
            return if(collidable.isWithin(getOrigin(screenDimensions), touchPos)) {
                when(event.action) {
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "isPressed set to false")
                        isPressed = false
                    }
                    MotionEvent.ACTION_DOWN -> {

                        Log.d(TAG, "isPressed set to true")
                        isPressed = true
                    }
                }
                action.invoke(event.action)
                true
            } else
                false
        }

        override fun draw(screenDimensions: Vec2) {
            GLES31.glUseProgram(shader.id)
            GLES31.glUniform1i(GLES31.glGetUniformLocation(shader.id, "isPressed"), if(isPressed) 1 else 0)
            super.draw(screenDimensions)
        }
    }

    /**
     * @return the UIRectangles origin in inches
     */
    protected fun getOrigin(screenDimensions: Vec2): Vec2 {
        return when (anchor) {
            UIAnchor.TOP_LEFT       -> Vec2(                     margins.x+dimensions.x/2,  screenDimensions.y - margins.y - dimensions.y/2)
            UIAnchor.TOP_RIGHT      -> Vec2(screenDimensions.x - margins.x-dimensions.x/2,  screenDimensions.y - margins.y - dimensions.y/2)
            UIAnchor.BOTTOM_LEFT    -> Vec2(                     margins.x+dimensions.x/2,                       margins.y + dimensions.y/2)
            UIAnchor.BOTTOM_RIGHT   -> Vec2(screenDimensions.x - margins.x-dimensions.x/2,                       margins.y + dimensions.y/2)
            UIAnchor.TOP_MIDDLE     -> Vec2(screenDimensions.x/2f + margins.x,                                         screenDimensions.y - margins.y - dimensions.y/2)
            UIAnchor.BOTTOM_MIDDLE  -> Vec2(screenDimensions.x/2f + margins.x,                                         margins.y + dimensions.y/2)
            UIAnchor.LEFT_MIDDLE    -> Vec2(margins.x + dimensions.x/2,                        screenDimensions.y/2f + margins.y)
            UIAnchor.RIGHT_MIDDLE   -> Vec2(screenDimensions.x - margins.x-dimensions.x/2,     screenDimensions.y/2f + margins.y)
        }
    }

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
    open fun draw(screenDimensions: Vec2) {
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
            val xMargin = margins.x*2f/screenDimensions.x   // Margin (offset) in opengl coordinates (-1f to 1f)
            val yMargin = margins.y*2f/screenDimensions.y
            val xRadius = dimensions.x/screenDimensions.x   // Distance from center of UIRect to edge in opengl coordinates (-1f to 1f)
            val yRadius = dimensions.y/screenDimensions.y

            when (anchor) {
                UIAnchor.TOP_LEFT       -> Matrix.translateM(matrix, 0, -1f + xRadius + xMargin,  1f - yRadius - yMargin, 0f)
                UIAnchor.TOP_RIGHT      -> Matrix.translateM(matrix, 0,  1f - xRadius - xMargin,  1f - yRadius - yMargin, 0f)
                UIAnchor.BOTTOM_LEFT    -> Matrix.translateM(matrix, 0, -1f + xRadius + xMargin, -1f + yRadius + yMargin, 0f)
                UIAnchor.BOTTOM_RIGHT   -> Matrix.translateM(matrix, 0,  1f - xRadius - xMargin, -1f + yRadius + yMargin, 0f)
                UIAnchor.BOTTOM_MIDDLE  -> Matrix.translateM(matrix, 0,  xMargin,                   -1f + yRadius + yMargin, 0f)
                UIAnchor.TOP_MIDDLE     -> Matrix.translateM(matrix, 0,  xMargin,                    1f - yRadius - yMargin, 0f)
                UIAnchor.LEFT_MIDDLE    -> Matrix.translateM(matrix, 0,  -1f + xRadius + xMargin,    yMargin, 0f)
                UIAnchor.RIGHT_MIDDLE   -> Matrix.translateM(matrix, 0,   1f - xRadius - xMargin,    yMargin, 0f)
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
            TOP_MIDDLE,
            LEFT_MIDDLE,
            RIGHT_MIDDLE,
            BOTTOM_MIDDLE
        }
    }
}