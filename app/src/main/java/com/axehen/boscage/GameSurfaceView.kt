package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.axehen.hengine.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class GameSurfaceView(context: Context, attr: AttributeSet): com.axehen.hengine.GameSurfaceView(context, attr) {

    private val character = Character(
        position = Vec3(0f, 0f, 0f),
        rotation = Rotation(180f, 0f, 0f, 1f),
        radius =0.3f,
        speed = 0.09f,
        meshes = parseOBJMTL( "models/character"))

    private val grassShader = Shader(
        renderer = renderer,
        shaderAsset = "shaders/textured",
        textures = arrayOf(
            Texture(getBitmap("textures/grass.png"), "tex0"),
            Texture(getBitmap("textures/earth.png"), "tex_ao")
        )
    )
    private val earthShader = Shader(
        renderer = renderer,
        shaderAsset = "shaders/textured",
        textures = arrayOf(
            Texture(getBitmap("textures/earth.png"), "tex0"),
            Texture(getBitmap("textures/earth.png"), "tex_ao")
        )
    )

    private var world = Environment().also { env ->
        env.groundMesh =
            CompoundMesh(
                position = Vec3(0f, 0f, 0f),
                rotation = Rotation(0f, 0f, 0f, 1f),
                arrayListOf(
                    Mesh(
                        vertexCoords = floatArrayOf(
                            -50f, -50f, 0f,
                            50f, -50f, 0f,
                            50f, 10f, 0f,
                            -50f, 10f, 0f
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
                            50f, 10f,
                            -50f, 10f,
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
                            -50f, 11f, 1f,
                            50f, 11f, 1f,
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
                            -50f, 11f,
                            50f, 11f,
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

        val houseMeshList = parseOBJMTL("models/house")
        env.objects.addAll(
            arrayOf(
                Environment.EnvironmentObject(
                    Vec3(-3f, 3f, 0f),
                    Rotation(180f, 0f, 0f, 1f),
                    houseMeshList,
                    Environment.SquareCollidable(1f, 45f)
                ),
                Environment.EnvironmentObject(
                    Vec3(3f, 3f, 0f),
                    Rotation(90f, 0f, 0f, 1f),
                    houseMeshList,
                    Environment.SquareCollidable(1f, 45f)
                ),
                Environment.EnvironmentObject(
                    Vec3(3f, -3f, 0f),
                    Rotation(90f, 0f, 0f, 1f),
                    houseMeshList,
                    Environment.SquareCollidable(1f, 45f)
                ),
                Environment.EnvironmentObject(
                    Vec3(-3f, -3f, 0f),
                    Rotation(180f, 0f, 0f, 1f),
                    houseMeshList,
                    Environment.SquareCollidable(1f, 45f)
                ),
            )
        )
    }
    private var inside = false
    private var houseInterior = Environment().also { env ->
        env.groundMesh =
            CompoundMesh(
                position = Vec3(0f, 0f, 0f),
                rotation = Rotation(45f, 0f, 0f, 1f),
                arrayListOf(
                    Mesh(
                        vertexCoords = floatArrayOf(
                            -5f, -5f, 0f,
                             5f, -5f, 0f,
                             5f,  5f, 0f,
                            -5f,  5f, 0f
                        ),
                        normals = floatArrayOf(
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                            0f, 0f, 1f,
                        ),
                        texCoords = floatArrayOf(
                            -5f, -5f,
                            5f, -5f,
                            5f,  5f,
                            -5f,  5f
                        ),
                        drawOrder = intArrayOf(
                            0, 1, 2,
                            0, 2, 3
                        ),
                        shader = grassShader
                    ),
                    Mesh(
                        vertexCoords = floatArrayOf(
                            -5f,  5f, 0f,
                             5f,  5f, 0f,
                             5f,  5f, 2f,
                            -5f,  5f, 2f
                        ),
                        normals = floatArrayOf(
                            0f, -1f, 0f,
                            0f, -1f, 0f,
                            0f, -1f, 0f,
                            0f, -1f, 0f,
                        ),
                        texCoords = floatArrayOf(
                            0f, 0f,
                            5f, 0f,
                            5f,  1f,
                            0f,  1f
                        ),
                        drawOrder = intArrayOf(
                            0, 1, 2,
                            0, 2, 3
                        ),
                        shader = earthShader
                    ),
                    Mesh(
                        vertexCoords = floatArrayOf(
                            5f,   5f, 0f,
                            5f,  -5f, 0f,
                            5f,  -5f, 2f,
                            5f,   5f, 2f
                        ),
                        normals = floatArrayOf(
                            -1f, 0f, 0f,
                            -1f, 0f, 0f,
                            -1f, 0f, 0f,
                            -1f, 0f, 0f,
                        ),
                        texCoords = floatArrayOf(
                            0f, 0f,
                            5f, 0f,
                            5f,  1f,
                            0f,  1f
                        ),
                        drawOrder = intArrayOf(
                            0, 1, 2,
                            0, 2, 3
                        ),
                        shader = earthShader
                    )
                )
        )

        val characterMeshList = parseOBJMTL("models/character")
        env.objects.add(
            Environment.EnvironmentObject(
                Vec3(-1f, 1f, 0f),
                Rotation(180f, 0f, 0f, 1f),
                characterMeshList,
                Environment.CircleCollidable(0.3f)
            )
        )
    }

    private var environment = world

    private var userInterface = UserInterface(
        screenDimensions = Vec2(resources.displayMetrics.widthPixels.toFloat()/resources.displayMetrics.xdpi, resources.displayMetrics.heightPixels.toFloat()/resources.displayMetrics.ydpi),
        screenResolution = Vec2(resources.displayMetrics.widthPixels.toFloat(), resources.displayMetrics.heightPixels.toFloat())
    ).also { ui ->
        val aButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap("textures/ButtonIcon-GCN-A.png"), "tex0"),
                Texture(getBitmap("textures/ButtonIcon-GCN-A_pressed.png"), "texPressed")
            )
        )
        val bButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap("textures/ButtonIcon-GCN-B.png"), "tex0"),
                Texture(getBitmap("textures/ButtonIcon-GCN-B_pressed.png"), "texPressed")
            )
        )
        val yButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap("textures/ButtonIcon-GCN-Y.png"), "tex0"),
                Texture(getBitmap("textures/ButtonIcon-GCN-Y_pressed.png"), "texPressed")
            )
        )
        val xButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap("textures/ButtonIcon-GCN-X.png"), "tex0"),
                Texture(getBitmap("textures/ButtonIcon-GCN-X_pressed.png"), "texPressed")
            )
        )
        val startButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap("textures/ButtonIcon-GCN-Start-Pause.png"), "tex0"),
                Texture(getBitmap("textures/ButtonIcon-GCN-Start-Pause_pressed.png"), "texPressed")
            )
        )

        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f / 2.54f, 2f / 2.54f),
            margins = Vec2(-0.4f / 2.54f, 1.3f / 2.54f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = xButtonShader,
            collidable = UIRectangle.CircleUICollidable((1f / 2f) * 1f / 2.54f),
            action = {
                Log.d(TAG, "xButtonPressed")
                character.speed += 0.01f
            }
        )
        )
        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f / 2.54f, 2f / 2.54f),
            margins = Vec2(1f / 2.54f, 2.05f / 2.54f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = yButtonShader,
            collidable = UIRectangle.CircleUICollidable((1f / 2f) * 1f / 2.54f),
            action = {
                Log.d(TAG, "yButtonPressed")
                character.speed -= 0.01f
            }
        )
        )
        ui.buttons.add(
            UIRectangle.UIButton(
                dimensions = Vec2(2f / 2.54f, 2f / 2.54f),
                margins = Vec2(0.7f / 2.54f, 0.9f / 2.54f),
                anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
                shader = aButtonShader,
                collidable = UIRectangle.CircleUICollidable((3f / 4f) * 1f / 2.54f),
                action = { action ->
                    Log.d(TAG, "aButtonPressed")
                    if(action == MotionEvent.ACTION_DOWN) {
                        if (inside) {
                            renderer.remove(houseInterior)
                            renderer.add(world)
                            environment = world
                            inside = false
                        } else {
                            renderer.remove(world)
                            renderer.add(houseInterior)
                            environment = houseInterior
                            inside = true
                        }
                    }
                }
            )
        )
        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f / 2.54f, 2f / 2.54f),
            margins = Vec2(1.7f / 2.54f, -0.1f / 2.54f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = bButtonShader,
            collidable = UIRectangle.CircleUICollidable((1f / 2f) * 1f / 2.54f),
            action = {
                Log.d(TAG, "bButtonPressed")
            }
        )
        )
        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f / 2.54f, 2f / 2.54f),
            margins = Vec2(0f / 2.54f, 0f / 2.54f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_MIDDLE,
            shader = startButtonShader,
            collidable = UIRectangle.CircleUICollidable((1f / 2f) * 1f / 2.54f),
            action = {
                Log.d(TAG, "startButtonPressed")
            }
        )
        )
    }

    init {

        renderer.lookAt = Vec3(0f, 0f, 0.5f)
        renderer.lookFrom = Vec3(0f, -7f, 7f)    // Offset from lookAt from which to look
        renderer.zoom = 0.2f

        renderer.onDrawCallback =  { onDrawCallback() }

        renderer.add(character)
        renderer.add(world)
        renderer.userInterface = userInterface

    }


    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var prevDist: Float = 0f

    private var stickPos: Vec2 = Vec2(0f, 0f)

    override fun onTouchWorld(event: MotionEvent) {
        Log.d(TAG, "onTouchWorld called")
        val x = event.x
        val y = event.y
        val dist: Float = if (event.pointerCount == 2) sqrt((event.getX(0)-event.getX(1)).pow(2) + (event.getY(0)-event.getY(1)).pow(2)) else 0f

        //Log.d("GameSurfaceView",event.toString())
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                //renderMode = RENDERMODE_CONTINUOUSLY
            }
            MotionEvent.ACTION_UP -> {
                stickPos.x = 0f
                stickPos.y = 0f
                character.velocity = Vec3(0f, 0f, 0f)
                //renderMode = RENDERMODE_WHEN_DIRTY
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

                        character.rotation = Rotation(-90+atan2(stickPos.y, stickPos.x)*180f/PI.toFloat(), 0f, 0f, 1f)
                        character.velocity = Vec3(character.speed * stickPos.x , character.speed * stickPos.y, 0f)

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
    }


    private fun onDrawCallback() {
        with(renderer) {
            //eyePos.x += speed * stickPos.x
            //eyePos.y -= speed * stickPos.y

            var endVelocity = Vec2(character.velocity.x, character.velocity.y)

            val collisionVectors = environment.getCollisionVectors(Vec2(character.position.x, character.position.y), character.radius)
            if (collisionVectors.size > 0) {
                for (collisionVector in collisionVectors)
                    endVelocity = endVelocity.deflect(collisionVector)
            }

            character.position += Vec3(endVelocity, 0f)
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