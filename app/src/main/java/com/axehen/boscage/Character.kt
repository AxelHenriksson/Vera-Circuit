package com.axehen.boscage

import com.axehen.hengine.CompoundMesh
import com.axehen.hengine.Mesh
import com.axehen.hengine.Rotation
import com.axehen.hengine.Vec3

class Character(position: Vec3, rotation: Rotation, val radius: Float, var speed: Float = 0.25f, meshes: List<Mesh>, ): CompoundMesh(position, rotation, meshes) {
    var velocity = Vec3(0f, 0f, 0f)
}