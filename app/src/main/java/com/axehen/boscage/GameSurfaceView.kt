package com.axehen.boscage

import android.content.Context
import android.util.AttributeSet
import com.axehen.hengine.*

class GameSurfaceView(context: Context, attr: AttributeSet): com.axehen.hengine.GameSurfaceView(context, attr) {

    init {

        val yokohama = Cubemap(
            renderer, intArrayOf(
                R.drawable.yoko_posx, R.drawable.yoko_negx,
                R.drawable.yoko_posy, R.drawable.yoko_negy,
                R.drawable.yoko_posz, R.drawable.yoko_negz
            ), uniform = "cubeMap"
        )

        val mirrorShader = Shader(
            renderer = renderer,
            vertexShaderRes = R.raw.mirror_vert,
            fragmentShaderRes = R.raw.mirror_frag,
            cubeMap = yokohama
        )
        val grassShader = Shader(
            renderer = renderer,
            vertexShaderRes = R.raw.textured_vert,
            fragmentShaderRes = R.raw.textured_frag,
            textures = arrayOf(
                Texture(renderer, R.drawable.grass, "tex0")
            )
        )


        renderer.addAll(
            Cube(
                position = Vec3(0f, 0f, 0f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = grassShader
            ),
            Cube(
                position = Vec3(0f, 2f, 0f),
                v1 = Vec3(-0.5f, -0.5f, 0f),
                v2 = Vec3( 0.5f,  0.5f, 0.5f),
                shader = grassShader
            )
        )
    }

}