package com.axehen.hengine

import android.content.Context
import android.opengl.GLES31.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import java.util.ArrayList

@Suppress("LeakingThis")
open class GameSurfaceView(context: Context, attr: AttributeSet) : GLSurfaceView(context) {

    val renderer: GameRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        glEnable( GL_DEPTH_TEST )
        glDepthFunc( GL_LEQUAL )
        glDepthMask( true )

        renderer = GameRenderer(context)

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY

    }

    private data class Vertex(val pos: Vec3, val texCoord: Vec2, val normal: Vec3)

    class NotFoundException(msg: String) : Exception(msg)

    fun parseOBJMTL(asset: String, scale: Float): List<Mesh> {
        val objString = Utils.getStringFromAsset(context, "$asset.obj")
        val mtlString = Utils.getStringFromAsset(context, "$asset.mtl")
        Log.d(TAG, "parsing obj $asset: \n $objString \n\n and mtl: \n $mtlString \n\n")

        val posList         = ArrayList<Vec3>()
        val texCoordList    = ArrayList<Vec2>()
        val normalList      = ArrayList<Vec3>()
        val faceList        = ArrayList<Array<Vertex>>()
        val meshList        = ArrayList<Mesh>()
        var activeMaterial  = ""

        for(line in objString.lines()) {

            line.split(" ").let { words ->
                when (words[0]) {
                    "v"  -> posList.add(
                        Vec3(
                            words[1].toFloat(),
                            words[2].toFloat(),
                            words[3].toFloat()
                        )
                    )
                    "vt" -> texCoordList.add(
                        Vec2(
                            words[1].toFloat(),
                            words[2].toFloat()
                        )
                    )
                    "vn" -> normalList.add(
                        Vec3(
                            words[1].toFloat(),
                            words[2].toFloat(),
                            words[3].toFloat()
                        )
                    )
                    "f"  -> {
                        faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                            val indices = words[it+1].split("/")
                            val texCoord = if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt()-1] else Vec2(
                                0f,
                                0f
                            )
                            Vertex(
                                posList[indices[0].toInt() - 1] * scale,
                                texCoord,
                                normalList[indices[2].toInt() - 1]
                            )
                        })
                    }
                    "usemtl" -> {
                        if (activeMaterial != "") {
                            meshList.add(
                                createMesh(
                                    faceList,
                                    getShaderFromMTL(mtlString, activeMaterial)
                                )
                            )
                            faceList.clear()
                        }
                        activeMaterial = words[1]
                    }
                    else -> {}
                }
            }
        }
        meshList.add(createMesh(faceList, getShaderFromMTL(mtlString, activeMaterial)))

        return meshList

        // Log.d(TAG, "${objString.lines().size} lines parsed in OBJ file, ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added")
    }

    private fun getShaderFromMTL(mtlString: String, activeMaterial: String): Shader {
        var mapKd = ""

        var inMaterial = false
        for(line in mtlString.lines()) {
            val words = line.split(" ")
            when (words[0]) {
                "newmtl" -> inMaterial = words[1] == activeMaterial
                "map_Kd" -> {
                    if (inMaterial) {
                        mapKd = words[1]
                        break
                    }
                }
            }
        }

        if (mapKd == "") throw NotFoundException("Either the material $activeMaterial or the texture map_Kd was not found in mtl file")

        return Shader(renderer,"shaders/mtl", arrayOf(
            Texture(renderer,mapKd, "texKd")
        ))
    }

    fun parseOBJ(asset: String, scale: Float, shader: Shader): Mesh {
        val objString = Utils.getStringFromAsset(context, "$asset.obj")

        val posList = ArrayList<Vec3>()
        val texCoordList    = ArrayList<Vec2>()
        val normalList      = ArrayList<Vec3>()
        val faceList        = ArrayList<Array<Vertex>>()

        for(line in objString.lines()) {

            line.split(" ").let { words ->
                when (words[0]) {
                    "v"  -> posList.add(Vec3(words[1].toFloat(), words[2].toFloat(), words[3].toFloat()))
                    "vt" -> texCoordList.add(Vec2(words[1].toFloat(), words[2].toFloat()))
                    "vn" -> normalList.add(Vec3(words[1].toFloat(), words[2].toFloat(), words[3].toFloat()))
                    "f"  -> {
                        faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                            val indices = words[it+1].split("/")
                            val texCoord = if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt()-1] else Vec2(0f, 0f)
                            Vertex(
                                posList[indices[0].toInt() - 1] * scale,
                                texCoord,
                                normalList[indices[2].toInt() - 1])
                        })
                    }
                    else -> {}
                }
            }
        }


        Log.d(TAG, "${objString.lines().size} lines parsed in OBJ file, ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added")

        val vertexCount = faceList.size * 3
        val vertexPositions = FloatArray(vertexCount * 3)
        val normals = FloatArray(vertexCount * 3)
        val texCoords = FloatArray(vertexCount * 2)
        val drawOrder = IntArray(vertexCount)

        var index = 0
        for(vertexArray in faceList) {
            for (vertex in vertexArray) {
                vertexPositions[3*index + 0] = vertex.pos.x
                vertexPositions[3*index + 1] = vertex.pos.y
                vertexPositions[3*index + 2] = vertex.pos.z

                normals[3*index + 0] = vertex.normal.x
                normals[3*index + 1] = vertex.normal.y
                normals[3*index + 2] = vertex.normal.z

                texCoords[2*index + 0] = vertex.texCoord.x
                texCoords[2*index + 1] = vertex.texCoord.y

                drawOrder[index] = index

                index++
            }
        }

        return Mesh(
            vertexCoords = vertexPositions,
            normals = normals,
            texCoords = texCoords,
            drawOrder = drawOrder,
            shader = shader
        )
    }

    private fun createMesh(faceList: List<Array<Vertex>>, shader: Shader): Mesh {
        val vertexCount = faceList.size * 3
        val vertexPositions = FloatArray(vertexCount * 3)
        val normals = FloatArray(vertexCount * 3)
        val texCoords = FloatArray(vertexCount * 2)
        val drawOrder = IntArray(vertexCount)

        var index = 0
        for(vertexArray in faceList) {
            for (vertex in vertexArray) {
                vertexPositions[3*index + 0] = vertex.pos.x
                vertexPositions[3*index + 1] = vertex.pos.y
                vertexPositions[3*index + 2] = vertex.pos.z

                normals[3*index + 0] = vertex.normal.x
                normals[3*index + 1] = vertex.normal.y
                normals[3*index + 2] = vertex.normal.z

                texCoords[2*index + 0] = vertex.texCoord.x
                texCoords[2*index + 1] = vertex.texCoord.y

                drawOrder[index] = index

                index++
            }
        }

        return Mesh(
            vertexCoords = vertexPositions,
            normals = normals,
            texCoords = texCoords,
            drawOrder = drawOrder,
            shader = shader
        )
    }

    companion object {
        private const val TAG = "hengine.GameSurfaceView.kt"
    }
}