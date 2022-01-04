package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.axehen.hengine.*
import kotlin.math.pow
import kotlin.math.sqrt

class GameSurfaceView(context: Context, attr: AttributeSet): com.axehen.hengine.GameSurfaceView(context, attr) {

    init {

        renderer.lookAt = Vec3(0f, 0f, 0.5f)
        renderer.eyePos = Vec3(0f, -4f, 7.5f)
        renderer.zoom = 0.4f

        val grassShader = Shader(
            renderer = renderer,
            shaderAsset = "shaders/textured",
            textures = arrayOf(
                Texture(renderer, "textures/grass.png", "tex0"),
                Texture(renderer, "textures/earth.png", "tex_ao")
            )
        )
        val earthShader = Shader(
            renderer = renderer,
            shaderAsset = "shaders/textured",
            textures = arrayOf(
                Texture(renderer, "textures/earth.png", "tex0"),
                Texture(renderer, "textures/earth.png", "tex_ao")
            )
        )


        renderer.add(
            parseOBJMTL( "models/house", Vec3(8f, -8f, 0f), 4f)
        )

        renderer.add(
            CompoundMesh(
                position = Vec3(0f, 0f, 0f),
                arrayListOf(
                    Mesh(
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
                    shader = grassShader
                ),
                    Mesh(
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
                        shader = earthShader
                    ),
                    Mesh(
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
                        shader = grassShader
                    )
            )
        )
        )


        renderer.addAll(
            Cube(
                position = Vec3(0.5f, -2.5f, 0f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = earthShader
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
            eyePos.z = (eyePos.z - 7 * dDist).coerceIn(3f, 6f)
            updateView()
            // zoom = (zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
        }
    }

}