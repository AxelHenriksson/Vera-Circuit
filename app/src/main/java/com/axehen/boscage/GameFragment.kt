package com.axehen.boscage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class GameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onResume() {
        super.onResume()

        (view as Game).startTick()
    }

    override fun onPause() {
        super.onPause()
        (view as Game).stopTick()
    }

}