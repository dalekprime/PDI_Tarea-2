package models

import javafx.scene.paint.Color

data class Pixel(var r: Int, var g: Int, var b: Int, var a: Int = 255){
    fun toColor(): Color = Color.rgb(r, g, b, a / 255.0)

    companion object {
        fun fromColor(color: Color): Pixel {
            return Pixel(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
                (color.opacity * 255).toInt()
            )
        }
    }
}