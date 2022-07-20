package com.axehen.hengine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


data class Vec3(var x: Float, var y: Float, var z: Float) {
    fun normalize() = this/this.length()
    fun length() = sqrt(x*x + y*y + z*z)

    constructor(v: Vec2, z: Float): this(v.x, v.y, z)

    infix fun deflect(prohibitor: Vec3): Vec3 {
        return if((this dot prohibitor) < 0)  this - prohibitor * (this dot (prohibitor.normalize()))
        else this
    }

    operator fun div(other: Float)      = Vec3(x/other, y/other, z/other)
    operator fun times(other: Float)    = Vec3(x*other, y*other, z*other)
    operator fun plus(other: Vec3)      = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3)     = Vec3(x-other.x, y-other.y, z-other.z)
    operator fun unaryMinus()           = Vec3(-x, -y, -z)
    infix fun dot(other: Vec3)          = x*other.x + y*other.y + z*other.z
}

data class Vec2(var x: Float, var y: Float) {
    fun normalize() = this/this.length()
    fun length() = sqrt(x*x + y*y)

    /**
     * Eliminates the vector components parallel and reverse to the prohibitor vector
     * @param prohibitor
     */
    infix fun deflect(prohibitor: Vec2): Vec2 {
        return if((this dot prohibitor) < 0)  this - prohibitor * (this dot (prohibitor.normalize()))
        else this
    }
    operator fun div(other: Float)          = Vec2(x/other, y/other)
    operator fun times(other: Float)        = Vec2(x*other, y*other)
    operator fun plus(other: Vec2)          = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2)         = Vec2(x-other.x, y-other.y)
    operator fun unaryMinus()               = Vec2(-x, -y)
    infix fun dot(other: Vec2)              = x*other.x + y*other.y
}
data class Rotation(var a: Float, var x: Float, var y: Float, var z: Float)