package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.axehen.boscage.DevUtils.Companion.genCharacter
import com.axehen.boscage.DevUtils.Companion.genHouseInterior
import com.axehen.boscage.DevUtils.Companion.genWorld
import com.axehen.hengine.*
import com.axehen.hengine.ModelImport.Companion.parseOBJMTL
import com.axehen.hengine.Utils.Companion.getBitmap
import kotlin.math.*

class Game(context: Context, attr: AttributeSet): com.axehen.hengine.AbstractGame(context, attr) {

    private val character = genCharacter()

    private val world = genWorld()

    private var inside = false
    private var houseInterior = genHouseInterior()

    private var environment = world

    private var movingCharacter = genCharacter().also{ it.position = Vec3(8f, 0f, 0f) }

    private var ui = UserInterface(
        this
    ).also { ui ->
        ui.scale = 150f

        val aButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-A.png"), "tex0"),
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-A_pressed.png"), "texPressed")
            )
        )

        val bButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-B.png"), "tex0"),
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-B_pressed.png"), "texPressed")
            )
        )

        val yButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-Y.png"), "tex0"),
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-Y_pressed.png"), "texPressed")
            )
        )

        val xButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-X.png"), "tex0"),
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-X_pressed.png"), "texPressed")
            )
        )

        val startButtonShader = Shader(
            renderer,
            "shaders/ui",
            arrayOf(
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-Start-Pause.png"), "tex0"),
                Texture(getBitmap(context, "textures/ButtonIcon-GCN-Start-Pause_pressed.png"), "texPressed")
            )
        )

        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f, 2f),
            margins = Vec2(-0.4f, 1.3f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = xButtonShader,
            collidable = UIRectangle.CircleUICollidable(1f / 2f),
            action = {
                Log.d("UserInterface.Button", "xButtonPressed")
                character.speed += 0.01f
            }
        )
        )
        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f, 2f),
            margins = Vec2(1f, 2.05f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = yButtonShader,
            collidable = UIRectangle.CircleUICollidable(1f / 2f),
            action = {
                Log.d("UserInterface.Button", "yButtonPressed")
                character.speed -= 0.01f
            }
        )
        )
        ui.buttons.add(
            UIRectangle.UIButton(
                dimensions = Vec2(2f, 2f),
                margins = Vec2(0.7f, 0.9f),
                anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
                shader = aButtonShader,
                collidable = UIRectangle.CircleUICollidable((3f / 4f)),
                action = { action ->
                    Log.d("UserInterface.Button", "aButtonPressed")
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
            dimensions = Vec2(2f, 2f),
            margins = Vec2(1.7f, -0.1f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_RIGHT,
            shader = bButtonShader,
            collidable = UIRectangle.CircleUICollidable(1f / 2f),
            action = {
                Log.d("UserInterface.Button", "bButtonPressed")
            }
        )
        )
        ui.buttons.add(UIRectangle.UIButton(
            dimensions = Vec2(2f, 2f),
            margins = Vec2(0f, 0f),
            anchor = UIRectangle.Companion.UIAnchor.BOTTOM_MIDDLE,
            shader = startButtonShader,
            collidable = UIRectangle.CircleUICollidable(1f / 2f),
            action = {
                Log.d("UserInterface.Button", "startButtonPressed")
            }
        )
        )
    }



    init {

        renderer.lookAt = Vec3(0f, 0f, 0.5f)
        renderer.lookFrom = Vec3(0f, -7f, 7f)    // Offset from lookAt from which to look
        renderer.zoom = 0.2f

        tickPeriod = 1000/60

        renderer.add(character)
        renderer.add(world)
        renderer.add(movingCharacter)
        setUserInterface(ui)

    }


    private var movTheta = 0.0

    override fun onTick() {
        movTheta += ((tickPeriod / 1000.0) * PI/32.0)

        val x = 8*cos(movTheta).toFloat()
        val y = 8*sin(movTheta).toFloat()

        movingCharacter.position = Vec3(x, y, elevationAt(x, y))

        moveCharacter()

        requestRender()
    }


    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var prevDist: Float = 0f

    private var stickPos: Vec2 = Vec2(0f, 0f)

    override fun onTouchWorld(event: MotionEvent) {
        Log.d("UserInterface","onTouchWorld called with event: $event")

        val x = event.x
        val y = event.y
        val dist: Float = if (event.pointerCount == 2) sqrt((event.getX(0)-event.getX(1)).pow(2) + (event.getY(0)-event.getY(1)).pow(2)) else 0f

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                //renderMode = RENDERMODE_CONTINUOUSLY
            }
            MotionEvent.ACTION_UP -> {
                stickPos.x = 0f
                stickPos.y = 0f
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

    private fun moveCharacter() {

        character.rotation = Rotation(-90+atan2(stickPos.y, stickPos.x)*180f/PI.toFloat(), 0f, 0f, 1f)
        character.velocity = Vec2(character.speed * stickPos.x , character.speed * stickPos.y)

        with(renderer) {
            //eyePos.x += speed * stickPos.x
            //eyePos.y -= speed * stickPos.y

            val collisionVectors = environment.getCollisionVectors(Vec2(character.position.x, character.position.y), character.radius)
            if (collisionVectors.size > 0) {
                for (collisionVector in collisionVectors)
                    character.velocity = character.velocity.deflect(collisionVector)
            }

            character.position += Vec3(character.velocity, 0f)
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

}