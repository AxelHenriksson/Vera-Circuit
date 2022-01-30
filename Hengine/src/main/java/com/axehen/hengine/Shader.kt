package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.util.Log


class Shader(private val renderer: GameRenderer, val asset: String, private val cubeMap: CubeMap?, private var textures: Array<Texture>?) {
    val id: Int by lazy { renderer.loadShader(this) }

    constructor(renderer: GameRenderer, shaderAsset: String, textures: Array<Texture>) : this(renderer, shaderAsset, null, textures)
    constructor(renderer: GameRenderer, shaderAsset: String) : this(renderer, shaderAsset, null, null)

    fun loadTextures() {
        if (textures != null) {

            for (i in textures!!.indices) {

                //if (textures!![i].id != -1) continue

                textures!![i].load(GL_TEXTURE0 + i)

                glBindTexture(GL_TEXTURE_2D, i)

                // Insert texture into shader
                val textureUniformLocation = glGetUniformLocation(id, textures!![i].uniform)

                glUseProgram(id)

                glUniform1i(textureUniformLocation, i)
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
        asset.hashCode() + (cubeMap?.hashCode() ?: 0) + (textures?.hashCode() ?: 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Shader

        if (asset != other.asset) return false
        if (cubeMap != other.cubeMap) return false
        if (textures != null) {
            if (other.textures == null) return false
            if (!textures.contentEquals(other.textures)) return false
        } else if (other.textures != null) return false

        return true
    }

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

            Log.d(TAG, "loadShader on \n$shaderCode\n returns shaderID: $shader ${glGetShaderInfoLog(shader).let {if (it.isNotEmpty()) "with infolog: $it" else ""}}")
            return shader
        }

        /**
         * Reads a shader text file from an asset file and creates and compiles the shader into GLES memory
         * @param context       The current context, required to read resource
         * @param type          GLES shader type, typically GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
         * @param asset         Name of the shader .vert and .frag files without extension
         * @return              The compiled shader's id
         */
        fun loadShaderFromAsset(context: Context, type: Int, asset: String): Int = loadShader(type, Utils.getStringFromAsset(context, asset))

        fun checkCompileErrors(shaderId: Int, type: Int) {
            val compileStatus = IntArray(1)
            glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] != GL_TRUE) {
                glDeleteShader(shaderId)
                throw RuntimeException(
                    "Could not compile ${if(type == GL_VERTEX_SHADER) "vertex shader" else if(type == GL_FRAGMENT_SHADER) "fragment shader" else "shader"}: "
                            + glGetShaderInfoLog(shaderId)
                )
            }
        }
        fun checkLinkErrors(programId: Int) {
            val linkStatus = IntArray(1)
            glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GL_TRUE) {
                glDeleteProgram(programId)
                throw RuntimeException(
                    "Could not link program: "
                            + glGetProgramInfoLog(programId)
                )
            }
        }
    }

}