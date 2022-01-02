package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.axehen.hengine.*
import kotlin.math.pow
import kotlin.math.sqrt

class GameSurfaceView(context: Context, attr: AttributeSet): com.axehen.hengine.GameSurfaceView(context, attr) {

    init {


        val grassShader = Shader(
            renderer = renderer,
            vertexShaderRes = R.raw.textured_vert,
            fragmentShaderRes = R.raw.textured_frag,
            textures = arrayOf(
                Texture(renderer, R.drawable.grass, "tex0")
            )
        )
        val earthShader = Shader(
            renderer = renderer,
            vertexShaderRes = R.raw.textured_vert,
            fragmentShaderRes = R.raw.textured_frag,
            textures = arrayOf(
                Texture(renderer, R.drawable.earth, "tex0")
            )
        )



        renderer.add(
            Mesh(
                position = Vec3(0f, 0f, 0f),
                vertexCoords = floatArrayOf(
                    -50f, -50f, 0f,
                     50f, -50f, 0f,
                     50f,   0f, 0f,
                    -50f,   0f, 0f
                ),
                normals = floatArrayOf(
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                ),
                texCoords = floatArrayOf(
                    -50f, -50f,
                     50f, -50f,
                     50f,   0f,
                    -50f,   0f,
                ),
                drawOrder = intArrayOf(
                    0, 1, 2,
                    0, 2, 3
                ),
                color = floatArrayOf(1f, 1f, 1f, 1f),
                shader = grassShader
            )
        )

        renderer.add(
            Mesh(
                position = Vec3(0f, 0f, 0f),
                vertexCoords = floatArrayOf(
                    -50f, 0f, 0f,
                    50f, 0f, 0f,
                    50f, 0f, 1f,
                    -50f, 0f, 1f
                ),
                normals = floatArrayOf(
                    0f, -1f, 0f,
                    0f, -1f, 0f,
                    0f, -1f, 0f,
                    0f, -1f, 0f,
                ),
                texCoords = floatArrayOf(
                    -50f, 0f,
                    50f, 0f,
                    50f, 1f,
                    -50f, 1f,
                ),
                drawOrder = intArrayOf(
                    0, 1, 2,
                    0, 2, 3
                ),
                color = floatArrayOf(1f, 1f, 1f, 1f),
                shader = earthShader
            )
        )

        renderer.add(
            Mesh(
                position = Vec3(0f, 0f, 1f),
                vertexCoords = floatArrayOf(
                    -50f,  0f, 0f,
                     50f,  0f, 0f,
                     50f, 50f, 0f,
                    -50f, 50f, 0f
                ),
                normals = floatArrayOf(
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                    0f, 0f, 1f,
                ),
                texCoords = floatArrayOf(
                    -50f,  0f,
                     50f,  0f,
                     50f, 50f,
                    -50f, 50f,
                ),
                drawOrder = intArrayOf(
                    0, 1, 2,
                    0, 2, 3
                ),
                color = floatArrayOf(1f, 1f, 1f, 1f),
                shader = grassShader
            )
        )




        renderer.addAll(
            Cube(
                position = Vec3(0.5f, -2.5f, 0f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = earthShader
            ),
            Cube(
                position = Vec3(0.5f, 2.5f, 0f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = grassShader
            )
        )
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
                        touchSwipe(dx / height.toFloat(), dy / height.toFloat())
                        requestRender()
                    }
                    2 -> {
                        touchPinch(dDist / height.toFloat())
                        requestRender()
                    }
                }

            }
        }
        previousX = x
        previousY = y
        prevDist = dist

        return true
    }
    // Touch responses
    /**
     * Renderer single finger move event response
     * @param dx change in finger x coordinate divided by screen resolution
     * @param dy change in finger y coordinate divided by screen resolution
     */
    private fun touchSwipe(dx: Float, dy: Float) {
        with(renderer) {
            eyePos.x = (eyePos.x - 2 * eyePos.z * zoom * dx)
            eyePos.y = (eyePos.y + 2 * eyePos.z * zoom * dy)
            lookAt.x = (lookAt.x - 2 * eyePos.z * zoom * dx)
            lookAt.y = (lookAt.y + 2 * eyePos.z * zoom * dy)
            updateView()
        }
    }

    /**
     * Renderer two finger pinch event response
     * @param dDist change in finger distance divided by screen resolution
     */
    private fun touchPinch(dDist: Float) {
        with (renderer) {
            eyePos.z = (eyePos.z - 7 * dDist).coerceIn(2f, 6f)
            updateView()
            // zoom = (zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
        }
    }

}