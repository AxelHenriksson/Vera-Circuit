package se.chalmersverateam.circuit

import android.content.Context

import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.axehen.hengine.*
import com.axehen.hengine.ModelImport.Companion.parseOBJMTL
import com.axehen.hengine.Utils.Companion.getBitmap
import kotlin.math.*
import com.axehen.hengine.Vec2

class Game(context: Context, attr: AttributeSet): com.axehen.hengine.AbstractGame(context, attr) {

    override fun initUI(): UserInterface {
        return UserInterface(
            this
        ).also { ui ->
            ui.scale = 150f

            val startButtonShader = Shader(
                renderer,
                "shaders/ui",
                arrayListOf(
                    AbstractTexture.Texture(
                        getBitmap(
                            context,
                            "textures/ButtonIcon-GCN-Start-Pause.png"
                        ), "tex0"
                    ),
                    AbstractTexture.Texture(
                        getBitmap(
                            context,
                            "textures/ButtonIcon-GCN-Start-Pause_pressed.png"
                        ), "texPressed"
                    )
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
    }

    override fun initLevel(): List<Drawable> {
        return arrayListOf(
            StaticMesh(
                Vec3(10f, 10f, 0f),
                Rotation(0f, 0f, 0f, 1f),
                parseOBJMTL("models/Nogaro_Earth")
            )
        )
    }

    init {
        Log.d("GameInit", "Init of Game.kt started")
        renderer.lookAt = Vec3(13.94f, 11.26f, 0.1f)
        renderer.lookFrom = Vec3(0f, -2f, 2f)    // Offset from lookAt from which to look
        renderer.zoom = 0.1f

        tickPeriod = 1000 / 60

    }

    private val speed = 0.5f
    override fun onTick() {
        charPos += Vec3(stickPos * speed, 0f)
        Log.d("Game.kt", "charPos = $charPos")
        renderer.lookAt = charPos
        renderer.updateView()
        requestRender()
    }


    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var prevDist: Float = 0f

    private var stickPos = Vec2(0f, 0f)

    private var charPos = Vec3(13.94f, 11.26f, 0.5f)

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

    val zoomSpeed = 3.0f

    /**
     * Renderer two finger pinch event response
     * @param dDist change in finger distance divided by screen resolution
     */
    private fun touchPinch(dDist: Float) {
        with (renderer) {
            lookFrom.z = (lookFrom.z - zoomSpeed * dDist).coerceIn(0.02f, 1f)
            updateView()
            // zoom = (zoom - 0.0005f * dDist).coerceIn(0.1f, 1.0f)
        }
    }

}