package com.axehen.hengine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.opengl.GLES31.*
import android.util.Log
import java.nio.ByteBuffer
import java.nio.IntBuffer


class Texture(bitmap: Bitmap, val uniform: String) {
    val buffer      =  getBitmapBuffer(bitmap)

    val format      = GL_RGBA
    val width       = bitmap.width
    val height      = bitmap.height

    var id: Int = -1


    /**
     * Loads texture into GPU memory
     * @param slot The texture slot (GL_TEXTURE0 + i) that the texture uses in GPU memory
     **/
    fun load(slot: Int) {

        //Generate texture ID
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

    companion object {
        fun getBitmapBuffer(bitmap: Bitmap): ByteBuffer {
            return with(bitmap) {
                ByteBuffer.allocateDirect(this.allocationByteCount).also {
                    this.copyPixelsToBuffer(it)
                    it.position(0)
                }
            }
        }
    }
}