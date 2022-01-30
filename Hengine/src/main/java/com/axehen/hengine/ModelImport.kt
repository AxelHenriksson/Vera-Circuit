package com.axehen.hengine

import android.util.Log
import java.util.ArrayList

class ModelImport {

    companion object {

        private data class Vertex(val pos: Vec3, val texCoord: Vec2, val normal: Vec3)

        class NotFoundException(msg: String) : Exception(msg)

        fun AbstractGame.parseOBJMTL(asset: String): List<Mesh> {
            val objString = Utils.getStringFromAsset(context, "$asset.obj")
            val mtlString = Utils.getStringFromAsset(context, "$asset.mtl")
            // Log.d("ModelImport", "parsing obj : \n $objString \n\n and mtl: \n $mtlString \n\n")

            // Split the obj file string into one string per object. Disregard the first element as that is above the first object declaration
            val oStrings = objString.split("\\no [^\\s]+".toRegex()).let {
                it.subList(1, it.size)
            }

            val posList = ArrayList<Vec3>()
            val texCoordList = ArrayList<Vec2>()
            val normalList = ArrayList<Vec3>()
            val faceList = ArrayList<Array<Vertex>>()
            var activeMaterial = ""
            val meshList = ArrayList<Mesh>()

            for (oString in oStrings) {
                for (line in oString.lines()) {

                    line.split(" ").let { words ->
                        when (words[0]) {
                            "v" -> posList.add(
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
                            "f" -> {
                                faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                                    val indices = words[it + 1].split("/")
                                    val texCoord =
                                        if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt() - 1] else Vec2(
                                            0f,
                                            0f
                                        )
                                    Vertex(
                                        posList[indices[0].toInt() - 1],
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
            }
            return meshList
            // Log.d(TAG, "${objString.lines().size} lines parsed in OBJ file, ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added")
        }

        private fun AbstractGame.getShaderFromMTL(mtlString: String, activeMaterial: String): Shader {
            var mapKd = ""

            var inMaterial = false
            for (line in mtlString.lines()) {
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

            return Shader(
                renderer,
                "shaders/mtl",
                arrayOf(
                    Texture(
                        bitmap = Utils.getBitmap(context, mapKd),
                        uniform = "texKd"
                    )
                )
            )
        }

        fun AbstractGame.parseOBJ(asset: String, scale: Float, shader: Shader): Mesh {
            val objString = Utils.getStringFromAsset(context, "$asset.obj")

            val posList = ArrayList<Vec3>()
            val texCoordList = ArrayList<Vec2>()
            val normalList = ArrayList<Vec3>()
            val faceList = ArrayList<Array<Vertex>>()

            for (line in objString.lines()) {

                line.split(" ").let { words ->
                    when (words[0]) {
                        "v" -> posList.add(
                            Vec3(
                                words[1].toFloat(),
                                words[2].toFloat(),
                                words[3].toFloat()
                            )
                        )
                        "vt" -> texCoordList.add(Vec2(words[1].toFloat(), words[2].toFloat()))
                        "vn" -> normalList.add(
                            Vec3(
                                words[1].toFloat(),
                                words[2].toFloat(),
                                words[3].toFloat()
                            )
                        )
                        "f" -> {
                            faceList.add(Array(/*words.size - 1*/ 3) {      // With size = 3 we only add triangles, leaving holes in the mesh if there are more than 3 vertices per face. Having size = words.size - 1, i.e. add all vertices of every face, means we get an error further down. As long as we triangulate the mesh before import we should not get holes in the mesh by using size 3.
                                val indices = words[it + 1].split("/")
                                val texCoord =
                                    if (indices[1].isNotEmpty()) texCoordList[indices[1].toInt() - 1] else Vec2(
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
                        else -> {}
                    }
                }
            }


            Log.d("ModelImport", "${objString.lines().size} lines parsed in OBJ file, ${posList.size} positions added, ${texCoordList.size} texCoords added, ${normalList.size} normals added, ${faceList.size} faces added")

            return createMesh(faceList, shader)
        }

        private fun createMesh(faceList: List<Array<Vertex>>, shader: Shader): Mesh {
            val vertexCount = faceList.size * 3
            val vertexPositions = FloatArray(vertexCount * 3)
            val normals = FloatArray(vertexCount * 3)
            val texCoords = FloatArray(vertexCount * 2)
            val drawOrder = IntArray(vertexCount)

            var index = 0
            for (vertexArray in faceList) {
                for (vertex in vertexArray) {
                    vertexPositions[3 * index + 0] = vertex.pos.x
                    vertexPositions[3 * index + 1] = vertex.pos.y
                    vertexPositions[3 * index + 2] = vertex.pos.z

                    normals[3 * index + 0] = vertex.normal.x
                    normals[3 * index + 1] = vertex.normal.y
                    normals[3 * index + 2] = vertex.normal.z

                    texCoords[2 * index + 0] = vertex.texCoord.x
                    texCoords[2 * index + 1] = vertex.texCoord.y

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

    }
}