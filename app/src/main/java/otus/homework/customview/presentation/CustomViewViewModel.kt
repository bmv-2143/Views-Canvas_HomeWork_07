package otus.homework.customview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import otus.homework.customview.R
import otus.homework.customview.data.CustomViewRepository
import otus.homework.customview.data.Payload
import javax.inject.Inject

@HiltViewModel
class CustomViewViewModel @Inject constructor(
    private val repository: CustomViewRepository,
    dataLoadDispatcher: CoroutineDispatcher) : ViewModel() {

    private val _payloads = MutableStateFlow<List<Payload>>(emptyList())
    val payloads = _payloads.asStateFlow()

    init {
        viewModelScope.launch(dataLoadDispatcher) {
            _payloads.value = repository.loadPayloads(R.raw.payload)
        }
    }

}

