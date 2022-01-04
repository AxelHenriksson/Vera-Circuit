package com.axehen.hengine

import android.opengl.GLES31.*
import android.opengl.Matrix
import java.lang.IllegalArgumentException
import java.nio.*
import java.nio.ByteBuffer.allocateDirect


open class Mesh(
    vertexCoords: FloatArray,
    normals: FloatArray,
    texCoords: FloatArray,
    val drawOrder: IntArray,
    val shader: Shader
) {



    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        allocateDirect(vertexCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var normalBuffer: FloatBuffer =
        // (number of normal values * 4 bytes per float)
        allocateDirect(normals.size * 4).run {

            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(normals)
                position(0)
            }
        }

    private val texCoordBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        allocateDirect(texCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }

    private val drawListBuffer: IntBuffer =
        // (# of coordinate values * 4 bytes per int)
        allocateDirect(drawOrder.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }


    init {
        if (vertexCoords.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex coordinate count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (normals.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Vertex normal vector count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
        if (drawOrder.size % COORDS_PER_VERTEX != 0) throw IllegalArgumentException("Draw order count is not divisible by coordsPerVertex (${COORDS_PER_VERTEX})")
    }


    fun load() {
        shader.loadTextures()
    }

    private val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    fun draw(position: Vec3) {
        shader.bindTextures()

        // Create model matrix and insert it into the mModel uniform of the mesh's shader
        FloatArray(16).let { modelMatrix ->
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, position.x, position.y, position.z)

            glUniformMatrix4fv( glGetUniformLocation(shader.id, "mModel") , 1, false, modelMatrix, 0)
        }

        // get handle to vertex shader's vPosition member. positionHandle is later used to disable its attribute array
        val positionHandle = glGetAttribLocation(shader.id, "vPosition").also { handle ->

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(handle)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                handle,
                3,
                GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        // get handle to vertex shader's vNormal member. normalHandle is later used to disable its attribute array
        val normalHandle = glGetAttribLocation(shader.id, "vNormal").also { handle ->
            glEnableVertexAttribArray(handle)       // Enable a handle to the data
            glVertexAttribPointer(              // Prepare the normal data
                handle,
                3,
                GL_FLOAT,
                false,
                vertexStride,
                normalBuffer
            )
        }

        // get handle to vertex shader's vTexCoord member. texCoordsHandle is later used to disable its attribute array
        val texCoordsHandle = glGetAttribLocation(shader.id, "vTexCoord").also { handle ->

            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(handle)

            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                handle,
                2,
                GL_FLOAT,
                false,
                2 * 4,  // Two texCoord coordinates per vertex, Four bytes per coordinate float
                texCoordBuffer
            )
        }

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, drawOrder.size, GL_UNSIGNED_INT, drawListBuffer)

        // Disable vertex array
        glDisableVertexAttribArray(positionHandle)
        glDisableVertexAttribArray(normalHandle)
        glDisableVertexAttribArray(texCoordsHandle)
    }

    companion object {
        private val TAG = "hengine.Mesh.kt"
        private const val COORDS_PER_VERTEX = 3
    }
}