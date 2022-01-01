package com.axehen.hengine

import android.graphics.Bitmap
import android.opengl.GLES31.*
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * @param renderer The game renderer in which the cubemap is used
 * @param faceResources Array of 6 face resource IDs in the order: posX, negX, posY, negY, posZ, negZ
 * @param uniform The shader uniform to which hte cubemap is mapped
 */
data class Cubemap(val renderer: GameRenderer, val faceResources: IntArray, val uniform: String) {
    val bitmaps: Array<Bitmap> = Array(6) { renderer.getBitmap(faceResources[it]) }
    val buffers: Array<ByteBuffer> = Array(6) { renderer.getBitmapBuffer(faceResources[it]) }

    var id: Int = -1
    val format: Int = GL_RGBA
    val width by lazy { bitmaps[0].width }
    val height by lazy { bitmaps[0].height }

    init {
        if(bitmaps[0].height != bitmaps[0].width)  throw IllegalArgumentException("Cubemap bitmaps are not square")
        for(i in 0 until bitmaps.size-1) {
            val bmp1 = bitmaps[i]
            val bmp2 = bitmaps[i+1]
            if(bmp1.width  != bmp2.width)  throw IllegalArgumentException("Cubemap bitmaps are not the same width")
            if(bmp1.height != bmp2.height) throw IllegalArgumentException("Cubemap bitmaps are not the same height")
        }
    }

    fun load() {
        with(IntBuffer.allocate(1)) {
            glGenTextures(1, this)
            id = this[0]
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP, id)

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR)

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE)

        for(i in faceResources.indices) {

            glTexImage2D(
                GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                0,
                format,
                width,
                height,
                0,
                format,
                GL_UNSIGNED_BYTE,
                buffers[i]
            )
        }

    }
}