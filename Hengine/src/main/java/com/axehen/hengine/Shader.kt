package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.util.Log
import com.axehen.hengine.*


class Shader(private val renderer: GameRenderer, val shaderAsset: String, val cubeMap: Cubemap?, var textures: Array<Texture>?) {
    val id: Int by lazy { renderer.loadShader(this) }

    constructor(renderer: GameRenderer, shaderAsset: String, textures: Array<Texture>) : this(renderer, shaderAsset, null, textures)

    fun loadTextures() {

        if (textures != null) {

            for (i in textures!!.indices) {

                //if (textures!![i].id != -1) continue

                textures!![i].load(GL_TEXTURE0 + i)

                glBindTexture(GL_TEXTURE_2D, i)

                // Insert texture into shader
                val textureUniformLocation = glGetUniformLocation(id, textures!![i].uniform)

                glUseProgram(id)

                glUniform1i(textureUniformLocation, 0)
            }
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

        if (textures != null)
            for (i in textures!!.indices) {
                glActiveTexture(GL_TEXTURE0 + i)
                glBindTexture(GL_TEXTURE_2D, textures!![i].id)
            }

        if (cubeMap != null) glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMap.id)
    }


    override fun hashCode(): Int =
        shaderAsset.hashCode() + (cubeMap?.hashCode() ?: 0) + (textures?.hashCode() ?: 0)

    companion object {
        private const val TAG = "Shader.kt"

        /**
         * Creates and compiles an openGL shader into GLES memory from a string of shader code
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param shaderCode    Bare string containing the shaders code
         * @return The compiled shader's id
         */
        private fun loadShader(type: Int, shaderCode: String): Int {
            val shader = glCreateShader(type)

            // add the source code to the shader and compile it
            glShaderSource(shader, shaderCode)
            glCompileShader(shader)

            Log.d(TAG, "loadShader on $shaderCode returns $shader with infolog: ${glGetShaderInfoLog(shader)}")
            return shader
        }

        /**
         * Reads a shader text file from an asset file and creates and compiles the shader into GLES memory
         * @param context       The current context, required to read resource
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param assetFileName      Name of the shader text file
         * @return              The compiled shader's id
         */
        fun loadShaderFromAsset(context: Context, type: Int, assetFileName: String): Int = loadShader(type, Utils.getStringFromAsset(context, assetFileName))
    }

}