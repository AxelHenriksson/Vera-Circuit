package com.axehen.hengine

class UserInterface(var screenDimensions: Vec2): Drawable{

    val elements = ArrayList<UIRectangle>()


    override fun load() {
        elements.forEach { it.load() }
    }

    override fun draw() {
        elements.forEach { it.draw(screenDimensions) }
    }

}