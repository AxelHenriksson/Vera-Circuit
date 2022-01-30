package com.axehen.hengine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder



class Utils {

    companion object {

        fun getThemeColor(context: Context, resId: Int): Color {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(resId, typedValue, true)
            return Color.valueOf(typedValue.data)
        }

        @Throws(Exception::class)
        private fun convertStreamToString(`is`: InputStream?): String {
            val reader = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            return sb.toString()
        }

        @Throws(Exception::class)
        fun getStringFromResource(context: Context, resId: Int): String {
            val fin = context.resources.openRawResource(resId)
            val ret = convertStreamToString(fin)
            //Make sure you close all streams.
            fin.close()
            return ret
        }

        @Throws(Exception::class)
        fun getStringFromAsset(context: Context, asset: String): String {
            val fin = context.assets.open(asset)
            val ret = convertStreamToString(fin)
            //Make sure you close all streams.
            fin.close()
            return ret
        }

        fun getBitmap(context: Context, asset: String): Bitmap {
            //val opts = BitmapFactory.Options()
            //opts.inScaled = false
            //opts.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
            return BitmapFactory.decodeStream(context.assets.open(asset) ).flip(1f, -1f).also {
                Log.d("Bitmaps", "bitmap got with colorSpace: ${it.colorSpace}, width: ${it.width}, height: ${it.height}, allocationByteCount: ${it.allocationByteCount}, byteCount: ${it.byteCount}")
            }
        }
        private fun Bitmap.flip(x: Float, y: Float): Bitmap {
            return Bitmap.createBitmap(this, 0, 0, width, height, android.graphics.Matrix().also { it.preScale(x, y) }, true)
        }

    }
}