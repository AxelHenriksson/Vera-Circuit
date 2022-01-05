package com.axehen.hengine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.TypedValue
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder



class Utils {

    companion object {
        private const val TAG = "Utils.kt"

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
        fun getStringFromAsset(context: Context, shaderAsset: String): String {
            val fin = context.assets.open(shaderAsset)
            val ret = convertStreamToString(fin)
            //Make sure you close all streams.
            fin.close()
            return ret
        }

    }
}