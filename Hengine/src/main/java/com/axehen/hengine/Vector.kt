package com.axehen.hengine

import kotlin.math.sqrt


data class Vec3(var x: Float, var y: Float, var z: Float) {

    fun normalize() = this/this.length()
    fun length() = sqrt(x*x + y*y + z*z)
    operator fun div(other: Float) = Vec3(x/other, y/other, z/other)
}
data class Vec2(var x: Float, var y: Float) {
    fun normalize() = this/this.length()
    fun length() = sqrt(x*x + y*y)
    operator fun div(other: Float) = Vec2(x/other, y/other)
}
data class Rotation(var a: Float, var x: Float, var y: Float, var z: Float)