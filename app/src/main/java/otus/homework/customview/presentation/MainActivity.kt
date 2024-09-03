package otus.homework.customview.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.utils.TAG

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<CustomViewViewModel>()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observePayloads()
        setPayloadCategorySelectionListener()
    }

    private fun observePayloads() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.payloads.collect {
                    binding.pieChartView.setPayloads(it)
                }
            }
        }
    }

    private fun setPayloadCategorySelectionListener() {
        binding.pieChartView.setSelectionListener { selectedCategoryId ->
            Log.d(TAG, "Selected category id: $selectedCategoryId")
        }
    }
}