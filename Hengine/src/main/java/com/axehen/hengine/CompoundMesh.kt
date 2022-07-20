package com.axehen.hengine

open class CompoundMesh(private val meshes: List<Mesh>) {

    fun load() {
        meshes.forEach{ it.load() }
    }
    fun draw(position: Vec3, rotation: Rotation) {
        meshes.forEach{ it.draw(position, rotation) }
    }
}

open class StaticMesh(protected val position: Vec3, protected val rotation: Rotation, meshes: List<Mesh>) : CompoundMesh(meshes), Drawable {
    override fun draw() {
        super.draw(position, rotation)
    }
}

// TODO: Implement thread safety, Volatile and Synchronized have been removed for testing purposes
open class DynamicMesh(//@Synchronized get @Synchronized set
    var position: Vec3, //@Synchronized get @Synchronized set
    var rotation: Rotation, meshes: List<Mesh>) : CompoundMesh(meshes), Drawable {


    override fun draw() {
        super.draw(position, rotation)
    }

}