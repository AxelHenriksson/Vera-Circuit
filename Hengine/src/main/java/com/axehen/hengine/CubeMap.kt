package com.axehen.hengine

import android.graphics.Bitmap
import android.opengl.GLES31.*
import com.axehen.hengine.Texture.Companion.getBitmapBuffer
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * @param bitmaps An array of 6 face bitmaps
 * @param uniform The shader uniform to which hte cubeMap is mapped
 */
class CubeMap(bitmaps: Array<Bitmap>, val uniform: String) {
    private val buffers: Array<ByteBuffer> = Array(6) { getBitmapBuffer(bitmaps[it]) }

    var id      = -1
    private val format  = GL_RGBA
    private val width   = bitmaps[0].width
    private val height  = bitmaps[0].height

    init {
        // Validate the provided bitmap set
        if (bitmaps.size != 6) throw IllegalArgumentException("6 bitmaps are required when initializing a CubeMap, however ${bitmaps.size} were provided")
        if(bitmaps[0].height != bitmaps[0].width)  throw IllegalArgumentException("CubeMap bitmaps are not square")
        for(i in 0 until bitmaps.size-1) {
            val bmp1 = bitmaps[i]
            val bmp2 = bitmaps[i+1]
            if(bmp1.width  != bmp2.width)  throw IllegalArgumentException("CubeMap bitmaps are not the same width")
            if(bmp1.height != bmp2.height) throw IllegalArgumentException("CubeMap bitmaps are not the same height")
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

        for(i in buffers.indices) {

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