package com.axehen.boscage

import com.axehen.hengine.*
import com.axehen.hengine.ModelImport.Companion.parseOBJMTL

class DevUtils {
    companion object {
        fun Game.genCharacter() = Character(
                position = Vec3(0f, 0f, 0f),
                rotation = Rotation(180f, 0f, 0f, 1f),
                radius =0.3f,
                speed = 0.09f,
                meshes = parseOBJMTL("models/character")
            )

        fun Game.genWorld(): Environment {

            val grassShader = Shader(
                renderer,
                shaderAsset = "shaders/textured",
                textures = arrayOf(
                    Texture(Utils.getBitmap(context, "textures/grass.png"), "tex0"),
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex_ao")
                )
            )

            val earthShader = Shader(
                renderer,
                shaderAsset = "shaders/textured",
                textures = arrayOf(
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex0"),
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex_ao")
                )
            )

            return Environment().also { env ->
                env.groundMesh =
                    StaticMesh(
                        position = Vec3(0f, 0f, 0f),
                        rotation = Rotation(0f, 0f, 0f, 1f),
                        arrayListOf(
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    -50f, -50f, 0f,
                                    50f, -50f, 0f,
                                    50f, 10f, 0f,
                                    -50f, 10f, 0f
                                ),
                                texCoords = floatArrayOf(
                                    -50f, -50f,
                                    50f, -50f,
                                    50f, 10f,
                                    -50f, 10f,
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = grassShader
                            ),
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    -50f, 10f, 0f,
                                    50f, 10f, 0f,
                                    50f, 11f, 1f,
                                    -50f, 11f, 1f
                                ),
                                texCoords = floatArrayOf(
                                    -50f, 0f,
                                    50f, 0f,
                                    50f, 1f,
                                    -50f, 1f,
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = earthShader
                            ),
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    -50f, 11f, 1f,
                                    50f, 11f, 1f,
                                    50f, 50f, 1f,
                                    -50f, 50f, 1f
                                ),
                                texCoords = floatArrayOf(
                                    -50f, 11f,
                                    50f, 11f,
                                    50f, 50f,
                                    -50f, 50f,
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = grassShader
                            )
                        )
                    )

                val houseMeshList = parseOBJMTL("models/house")

                env.objects.addAll(
                    arrayOf(
                        Environment.EnvironmentObject(
                            Vec3(-3f, 3f, 0f),
                            Rotation(180f, 0f, 0f, 1f),
                            houseMeshList,
                            Environment.SquareCollidable(1f, 45f)
                        ),
                        Environment.EnvironmentObject(
                            Vec3(3f, 3f, 0f),
                            Rotation(90f, 0f, 0f, 1f),
                            houseMeshList,
                            Environment.SquareCollidable(1f, 45f)
                        ),
                        Environment.EnvironmentObject(
                            Vec3(3f, -3f, 0f),
                            Rotation(90f, 0f, 0f, 1f),
                            houseMeshList,
                            Environment.SquareCollidable(1f, 45f)
                        ),
                        Environment.EnvironmentObject(
                            Vec3(-3f, -3f, 0f),
                            Rotation(180f, 0f, 0f, 1f),
                            houseMeshList,
                            Environment.SquareCollidable(1f, 45f)
                        ),
                    )
                )
            }
        }

        fun Game.genHouseInterior(): Environment {
            val grassShader = Shader(
                renderer,
                shaderAsset = "shaders/textured",
                textures = arrayOf(
                    Texture(Utils.getBitmap(context, "textures/grass.png"), "tex0"),
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex_ao")
                )
            )

            val earthShader = Shader(
                renderer,
                shaderAsset = "shaders/textured",
                textures = arrayOf(
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex0"),
                    Texture(Utils.getBitmap(context, "textures/earth.png"), "tex_ao")
                )
            )

            return Environment().also { env ->
                env.groundMesh =
                    StaticMesh(
                        position = Vec3(0f, 0f, 0f),
                        rotation = Rotation(45f, 0f, 0f, 1f),
                        arrayListOf(
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    -5f, -5f, 0f,
                                    5f, -5f, 0f,
                                    5f,  5f, 0f,
                                    -5f,  5f, 0f
                                ),
                                texCoords = floatArrayOf(
                                    -5f, -5f,
                                    5f, -5f,
                                    5f,  5f,
                                    -5f,  5f
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = grassShader
                            ),
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    -5f,  5f, 0f,
                                    5f,  5f, 0f,
                                    5f,  5f, 2f,
                                    -5f,  5f, 2f
                                ),
                                texCoords = floatArrayOf(
                                    0f, 0f,
                                    5f, 0f,
                                    5f,  1f,
                                    0f,  1f
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = earthShader
                            ),
                            Mesh(
                                vertexCoords = floatArrayOf(
                                    5f,   5f, 0f,
                                    5f,  -5f, 0f,
                                    5f,  -5f, 2f,
                                    5f,   5f, 2f
                                ),
                                texCoords = floatArrayOf(
                                    0f, 0f,
                                    5f, 0f,
                                    5f,  1f,
                                    0f,  1f
                                ),
                                drawOrder = intArrayOf(
                                    0, 1, 2,
                                    0, 2, 3
                                ),
                                shader = earthShader
                            )
                        )
                    )

                val characterMeshList = parseOBJMTL("models/character")

                env.objects.add(
                    Environment.EnvironmentObject(
                        Vec3(-1f, 1f, 0f),
                        Rotation(180f, 0f, 0f, 1f),
                        characterMeshList,
                        Environment.CircleCollidable(0.3f)
                    )
                )
            }
        }
    }
}