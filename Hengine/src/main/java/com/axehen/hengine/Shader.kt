package com.axehen.hengine

import android.opengl.GLES31.*
import com.axehen.hengine.*


class Shader(val renderer: GameRenderer, val vertexShaderRes: Int, val fragmentShaderRes: Int, val cubeMap: Cubemap?, vararg var textures: Texture) {
    val id: Int by lazy { renderer.loadShader(this) }

    fun loadTextures() {

        for (i in textures.indices) {

            if (textures[i].id != -1) continue

            textures[i].load(GL_TEXTURE0 + i)

            glBindTexture(GL_TEXTURE_2D, i)

            // Insert texture into shader
            val textureUniformLocation = glGetUniformLocation(id, textures[i].uniform)

            glUseProgram(id)

            glUniform1i(textureUniformLocation, 0)
        }

        if (cubeMap != null) {
            //if (cubeMap.ID != -1) //TODO: Do something if the texture already exists

            cubeMap.load()

            glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMap.id)

            val cubeMapUniformLocation = glGetUniformLocation(id, "cubeMap")

            glUseProgram(id)

            glUniform1i(cubeMapUniformLocation, 0)


        }
    }

    fun bindTextures() {
        glUseProgram(id)

        for (i in textures.indices) {
            // Bind textures
            glActiveTexture(GL_TEXTURE0 + i)
            glBindTexture(GL_TEXTURE_2D, textures[i].id)
        }

        if (cubeMap != null) glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMap.id)
    }

}