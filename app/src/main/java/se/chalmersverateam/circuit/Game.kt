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

    // Engine variables
    override var tickPeriod: Long = 1000 / 60

    // Game variables
    private val cameraSpeed = 0.5f
    private var charPos = Vec3(13.94f, 11.26f, 0.5f)

    // Initialization variables
    private val charLookFrom = Vec3(0f, -2f, 2f)
    private val charZoom = 0.1f



    init {
        renderer.lookAt = charPos
        renderer.lookFrom = charLookFrom    // Offset from lookAt from which to look
        renderer.zoom = charZoom

        setUI {
            UserInterface(
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

        loadDrawable {
            StaticMesh(
                Vec3(10f, 10f, 0f),
                Rotation(0f, 0f, 0f, 1f),
                parseOBJMTL("models/Nogaro_Earth")
            )
        }

    }


    override fun onTick() {
        charPos += Vec3(touchStickPos * cameraSpeed, 0f)
        renderer.lookAt = charPos
        renderer.updateView()
        requestRender()
    }


    private var touchPreviousX: Float = 0f
    private var touchPreviousY: Float = 0f
    private var touchPrevDist: Float = 0f

    private var touchStickPos = Vec2(0f, 0f)

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
                touchStickPos.x = 0f
                touchStickPos.y = 0f
                //renderMode = RENDERMODE_WHEN_DIRTY
            }
            MotionEvent.ACTION_MOVE -> {

                val dx: Float = x - touchPreviousX
                val dy: Float = y - touchPreviousY
                val dDist: Float = dist - touchPrevDist

                when (event.pointerCount) {
                    1 -> {
                        //touchSwipe(dx / height.toFloat(), dy / height.toFloat())

                        val radius = 0.25f
                        touchStickPos.x += dx/(radius*height.toFloat())
                        touchStickPos.y -= dy/(radius*height.toFloat())
                        if (touchStickPos.length() > 1f) touchStickPos = touchStickPos.normalize()
                    }
                    2 -> {
                        touchPinch(dDist / height.toFloat())
                        requestRender()
                    }
                }

            }
        }
        touchPreviousX = x
        touchPreviousY = y
        touchPrevDist = dist
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