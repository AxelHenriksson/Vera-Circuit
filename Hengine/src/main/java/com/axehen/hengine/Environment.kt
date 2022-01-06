package com.axehen.hengine

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A class containing and handling the game environment i.e. ground mesh and all world objects
 */
class Environment : Drawable {

    /**
     * A world objects containing a mesh and collidable that handles its collisions
     */
    class EnvironmentObject(position: Vec3, rotation: Rotation, meshes: List<Mesh>, val collidable: Collidable): CompoundMesh(position, rotation, meshes) {
        fun getCollisionVector(charPos: Vec2, charRadius: Float): Vec2? {
            return collidable.getCollisionVector(charPos, charRadius, Vec2(position.x, position.y))
        }
    }

    interface Collidable {
        fun getCollisionVector(charPos: Vec2, charRadius: Float, objPos: Vec2): Vec2?
    }

    class CircleCollidable(private val radius: Float): Collidable {
        override fun getCollisionVector(charPos: Vec2, charRadius: Float, objPos: Vec2): Vec2? {
            return if((charPos - objPos).length() <= radius + charRadius) {
                (charPos - objPos).normalize()
            } else null
        }
    }
    
    class SquareCollidable(private val radius: Float, private val angle: Float): Collidable {
        override fun getCollisionVector(charPos: Vec2, charRadius: Float, objPos: Vec2): Vec2? {
            val u = Vec2(cos(angle*PI/180f).toFloat(), sin(angle*PI/180f).toFloat())
            val v = Vec2(cos((angle+90f)*PI/180f).toFloat(), sin((angle+90f)*PI/180f).toFloat())

            val delta = (charPos - objPos).let { Vec2(it.x, it.y) }
            val uProj = delta dot u
            val vProj = delta dot v
            return if(abs(uProj) > abs(vProj)) {
                if (abs(uProj) < (radius + charRadius)) {
                    if (uProj < 0)
                        -u
                    else
                        u
                } else null
            } else {
                if (abs(vProj) < (radius + charRadius)) {
                    if (vProj < 0)
                        -v
                    else
                        v
                } else null
            }
        }
    }

    val objects = ArrayList<EnvironmentObject>()
    var groundMesh: CompoundMesh? = null

    /**
     * Checks collisions between the character and every object in the environment
     * @return The exit vector of all occurring collisions
     */
    fun getCollisionVectors(charPos: Vec2, charRadius: Float): ArrayList<Vec2> {
        val vectorList = ArrayList<Vec2>()
        for (obj in objects) {
            obj.getCollisionVector(charPos, charRadius)?.let { vectorList.add(it) }
        }
        return vectorList
    }

    override fun load() {
        groundMesh?.load()
        objects.forEach { it.load() }
    }

    override fun draw() {
        groundMesh?.draw()
        objects.forEach{ it.draw() }
    }
}