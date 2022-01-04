package com.axehen.hengine

open class CompoundMesh(var position: Vec3, val meshes: ArrayList<Mesh>) : Drawable {

    override fun load() {
        meshes.forEach{ it.load() }
    }
    override fun draw() {
        meshes.forEach{ it.draw(position) }
    }

}