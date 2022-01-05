package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.axehen.hengine.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class GameSurfaceView(context: Context, attr: AttributeSet): com.axehen.hengine.GameSurfaceView(context, attr) {

    private val character = Character(Vec3(0f, 0f, 0f), Rotation(180f, 0f, 0f, 1f), parseOBJMTL( "models/character"))

    init {

        renderer.lookAt = Vec3(0f, 0f, 0.5f)
        renderer.lookFrom = Vec3(0f, -7f, 7f)    // Offset from lookAt from which to look
        renderer.zoom = 0.2f

        renderer.onDrawCallback =  { onDrawCallback() }

        val grassShader = Shader(
            renderer = renderer,
            shaderAsset = "shaders/textured",
            textures = arrayOf(
                Texture(getBitmap("textures/grass.png"), "tex0"),
                Texture(getBitmap("textures/earth.png"), "tex_ao")
            )
        )
        val earthShader = Shader(
            renderer = renderer,
            shaderAsset = "shaders/textured",
            textures = arrayOf(
                Texture(getBitmap("textures/earth.png"), "tex0"),
                Texture(getBitmap("textures/earth.png"), "tex_ao")
            )
        )

        renderer.add(character)

        val houseMeshList = parseOBJMTL( "models/house")

        renderer.addAll(
            CompoundMesh(Vec3(-3f, 3f, 0f), Rotation(180f, 0f, 0f, 1f), houseMeshList),
            CompoundMesh(Vec3(3f, 3f, 0f), Rotation(90f, 0f, 0f, 1f), houseMeshList),
            CompoundMesh(Vec3(3f, -3f, 0f), Rotation(90f, 0f, 0f, 1f), houseMeshList),
            CompoundMesh(Vec3(-3f, -3f, 0f), Rotation(180f, 0f, 0f, 1f), houseMeshList),
        )

        renderer.add(
            CompoundMesh(
                position = Vec3(0f, 0f, 0f),
                rotation = Rotation(0f, 0f, 0f, 1f),
                arrayListOf(
                    Mesh(
                    vertexCoords = floatArrayOf(
                        -50f, -50f, 0f,
                         50f, -50f, 0f,
                         50f,   10f, 0f,
                        -50f,   10f, 0f
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
                         50f,   10f,
                        -50f,   10f,
                    ),
                    drawOrder = intArrayOf(
                        0, 1, 2,
                        0, 2, 3
                    ),
                    shader = grassShader
                ),
                    Mesh(
                        vertexCoords = floatArrayOf(
                            -50f, 10f, 0f,
                            50f, 10f, 0f,
                            50f, 11f, 1f,
                            -50f, 11f, 1f
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
                            -50f,  11f, 1f,
                            50f,  11f, 1f,
                            50f, 50f, 1f,
                            -50f, 50f, 1f
                        ),
                        normals = floatArrayOf(
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                        ),
                        texCoords = floatArrayOf(
                            -50f,  11f,
                            50f,  11f,
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
                rotation = Rotation(45f, 0f, 0f, 1f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = earthShader
            )
        )
    }


    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var prevDist: Float = 0f

    private var stickPos: Vec2 = Vec2(0f, 0f)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val dist: Float = if (event.pointerCount == 2) sqrt((event.getX(0)-event.getX(1)).pow(2) + (event.getY(0)-event.getY(1)).pow(2)) else 0f

        //Log.d("GameSurfaceView",event.toString())
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderMode = RENDERMODE_CONTINUOUSLY
            }
            MotionEvent.ACTION_UP -> {
                stickPos.x = 0f
                stickPos.y = 0f
                character.velocity = Vec3(0f, 0f, 0f)
                renderMode = RENDERMODE_WHEN_DIRTY
            }
            MotionEvent.ACTION_MOVE -> {

                val dx: Float = x - previousX
                val dy: Float = y - previousY
                val dDist: Float = dist - prevDist

                when (event.pointerCount) {
                    1 -> {
                        //touchSwipe(dx / height.toFloat(), dy / height.toFloat())

                        val radius = 0.25f
                        stickPos.x += dx/(radius*height.toFloat())
                        stickPos.y -= dy/(radius*height.toFloat())
                        if (stickPos.length() > 1f) stickPos = stickPos.normalize()

                        val speed = 0.125f
                        character.rotation = Rotation(-90+atan2(stickPos.y, stickPos.x)*180f/PI.toFloat(), 0f, 0f, 1f)
                        character.velocity = Vec3(speed * stickPos.x , speed * stickPos.y, 0f)

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
//    /**
//     * Renderer single finger move event response
//     * @param dx change in finger x coordinate divided by screen resolution
//     * @param dy change in finger y coordinate divided by screen resolution
//     */
//    private fun touchSwipe(dx: Float, dy: Float) {
//        with(renderer) {
//            eyePos.x = (eyePos.x - 2 * eyePos.z * zoom * dx)
//            eyePos.y = (eyePos.y + 2 * eyePos.z * zoom * dy)
//            lookAt.x = (lookAt.x - 2 * eyePos.z * zoom * dx)
//            lookAt.y = (lookAt.y + 2 * eyePos.z * zoom * dy)
//            updateView()
//        }
//    }


    private fun onDrawCallback() {
        with(renderer) {
            //eyePos.x += speed * stickPos.x
            //eyePos.y -= speed * stickPos.y

            character.position += character.velocity
            character.position.z = elevationAt(character.position.x, character.position.y)

            lookAt = character.position



            updateView()
        }
    }

    /**
     * Returns the maps elevation at a specific coordinate
     */
    private fun elevationAt(x: Float, y: Float): Float {
        return (y-10f).coerceIn(0f, 1f)
    }

    /**
     * Renderer two finger pinch event response
     * @param dDist change in finger distance divided by screen resolution
     */
    private fun touchPinch(dDist: Float) {
        with (renderer) {
            lookFrom.z = (lookFrom.z - 7 * dDist).coerceIn(3f, 14f)
            updateView()
            // zoom = (zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
        }
    }

    companion object {
        private const val TAG = "boscage.GameSurfaceView.kt"
    }

}