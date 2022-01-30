package com.axehen.boscage

import com.axehen.hengine.*
import java.util.concurrent.atomic.AtomicReference

class Character(position: Vec3, rotation: Rotation, var radius: Float, var speed: Float = 0.25f, meshes: List<Mesh>): DynamicMesh(position, rotation, meshes) {
    var velocity = Vec2(0f, 0f)
}