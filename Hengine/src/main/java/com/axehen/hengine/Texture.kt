package com.axehen.hengine

import android.graphics.Bitmap
import android.opengl.GLES31.*
import com.axehen.hengine.GameRenderer
import java.nio.ByteBuffer
import java.nio.IntBuffer


data class Texture(val renderer: GameRenderer, val assetName: String, val uniform: String) {
    val bitmap: Bitmap by lazy { renderer.getBitmap(assetName) }
    val buffer: ByteBuffer by lazy { renderer.getBitmapBuffer(assetName) }

    val format: Int = GL_RGBA
    val width by lazy { bitmap.width }
    val height by lazy { bitmap.height }

    var id: Int = -1


    /**
     * Loads texture into GPU memory
     * @param slot The texture slot (GL_TEXTURE0 + i) that the texture uses in GPU memory
     **/
    fun load(slot: Int) {

        //Generate texture IDs
        with(IntBuffer.allocate(1)) {
            glGenTextures(1, this)
            id = this[0]
        }

        glActiveTexture(slot)

        glBindTexture(GL_TEXTURE_2D, id)

        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MIN_FILTER,
            GL_NEAREST
        )
        glTexParameteri(
            GL_TEXTURE_2D,
            GL_TEXTURE_MAG_FILTER,
            GL_NEAREST
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            format,
            width,
            height,
            0,
            format,
            GL_UNSIGNED_BYTE,
            buffer
        )

        glGenerateMipmap(GL_TEXTURE_2D)
    }
}