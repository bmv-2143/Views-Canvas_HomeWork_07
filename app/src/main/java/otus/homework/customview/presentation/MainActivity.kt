package otus.homework.customview.presentation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import otus.homework.customview.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<CustomViewViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        observePayloads()
    }

    private fun observePayloads() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.payloads.collect {
                    // todo: update custom view
                }
            }
        }
    }
}