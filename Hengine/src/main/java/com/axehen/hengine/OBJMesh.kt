package com.axehen.hengine

import android.content.Context
import android.util.Log

class OBJMesh {


    companion object {
        private const val TAG = "OBJMesh.kt"

        fun parseOBJ(context: Context, objRes: Int, position: Vec3, scale: Float, shader: Shader): Mesh {

            val objString = Utils.getStringFromResource(context, objRes)

            data class Position(val x: Float, val y: Float, val z: Float) {
                operator fun times(factor: Float) = Position(factor * x, factor * y, factor * z)
            }
            data class TexCoord(val u: Float, val v: Float)
            data class Normal(val x: Float, val y: Float, val z: Float)
            data class Vertex(val pos: Position, val texCoord: TexCoord, val normal: Normal)

            val posList = ArrayList<Position>()
            val texCoordList    = ArrayList<TexCoord>()
            val normalList      = ArrayList<Normal>()
            val faceList        = ArrayList<Array<Vertex>>()

            for(line in objString.lines()) {

                with (line.split(" ")) {
                    when (this[0]) {
                        "v"  -> posList.add(Position(this[1].toFloat(), this[2].toFloat(), this[3].toFloat()))
                        "vt" -> texCoordList.add(TexCoord(this[1].toFloat(), this[2].toFloat()))
                        "vn" -> normalList.add(Normal(this[1].toFloat(), this[2].toFloat(), this[3].toFloat()))
                        "f"  -> {
                                faceList.add(Array(this.size - 1) {
                                    val indices = this[it+1].split("/")
                                    val texCoord = if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt()-1] else TexCoord(0f, 0f)
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

            var index: Int = 0
            for(vertexArray in faceList) {
                for (vertex in vertexArray) {
                    vertexPositions[3*index + 0] = vertex.pos.x
                    vertexPositions[3*index + 1] = vertex.pos.y
                    vertexPositions[3*index + 2] = vertex.pos.z

                    normals[3*index + 0] = vertex.normal.x
                    normals[3*index + 1] = vertex.normal.y
                    normals[3*index + 2] = vertex.normal.z

                    texCoords[2*index + 0] = vertex.texCoord.u
                    texCoords[2*index + 1] = vertex.texCoord.v

                    drawOrder[index] = index

                    index++
                }
            }

            return Mesh(
                position = position,
                vertexCoords = vertexPositions,
                normals = normals,
                texCoords = texCoords,
                drawOrder = drawOrder,
                color = floatArrayOf(1f, 1f, 1f, 1f),
                shader = shader
            )


        }
    }


}