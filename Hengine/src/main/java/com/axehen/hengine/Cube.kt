package com.axehen.hengine

import com.axehen.hengine.Shader
import kotlin.math.max
import kotlin.math.min

class Cube(position: Vec3, v1: Vec3, v2: Vec3, shader: Shader)
    : Mesh(
    position = position,
    vertexCoords = floatArrayOf(
        min(v1.x,v2.x), min(v1.y,v2.y), max(v1.z,v2.z),
        min(v1.x,v2.x), max(v1.y,v2.y), max(v1.z,v2.z),
        max(v1.x,v2.x), max(v1.y,v2.y), max(v1.z,v2.z),
        max(v1.x,v2.x), min(v1.y,v2.y), max(v1.z,v2.z),
        min(v1.x,v2.x), min(v1.y,v2.y), min(v1.z,v2.z),
        min(v1.x,v2.x), max(v1.y,v2.y), min(v1.z,v2.z),
        max(v1.x,v2.x), max(v1.y,v2.y), min(v1.z,v2.z),
        max(v1.x,v2.x), min(v1.y,v2.y), min(v1.z,v2.z)
    ),
    normals = floatArrayOf(     // Normals provided here are non-normalized interpolations of the plane normals. Separate vertices for each face are required for flat shading
        -1f, -1f,  1f,
        -1f,  1f,  1f,
         1f,  1f,  1f,
         1f, -1f,  1f,
        -1f, -1f, -1f,
        -1f,  1f, -1f,
         1f,  1f, -1f,
         1f, -1f, -1f
    ),
    texCoords = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 1f,
        1f, 0f,
        1f, 1f,
        1f, 0f,
        0f, 0f,
        0f, 1f
    ),
    drawOrder = intArrayOf(
        0, 1, 2, 0, 2, 3,
        0, 1, 5, 0, 5, 4,
        1, 2, 6, 1, 6, 5,
        2, 3, 7, 2, 7, 6,
        3, 0, 4, 3, 4, 7,
        4, 5, 6, 4, 6, 7),
    color = floatArrayOf(1f, 1f, 1f, 1f),
    shader = shader)