package se.chalmersverateam.circuit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import androidx.core.os.postDelayed
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById<FrameLayout>(R.id.main_frame)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }




        Log.d("GameInit", "val gameFragment = GameFragment() called")
        val gameFragment = GameFragment()
        Handler(mainLooper).post {
            Log.d("GameInit", "Inside mainLooper loop")
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, gameFragment)
                .commit()
            Log.d("GameInit", "Fragment swap committed")
        }
        Log.d("GameInit", "val gameFragment = GameFragment() passed")

    }

}