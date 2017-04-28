package io.rapid.benchmark

import android.os.Bundle
import cz.kinst.jakub.viewmodelbinding.ViewModelActivity
import io.rapid.benchmark.databinding.ActivityMainBinding


class MainActivity : ViewModelActivity<ActivityMainBinding, MainViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setupViewModel(R.layout.activity_main, MainViewModel::class.java)
        super.onCreate(savedInstanceState)
    }
}
