package com.example.agrotracker.admin

import androidx.lifecycle.ViewModel
import com.example.agrotracker.api.NetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class WorklistViewModel: ViewModel() {


    private val api by lazy{ NetworkService.instance?.agroTrackerApi}

    private val _uiAction = MutableSharedFlow<Actions>()
    val uiAction: SharedFlow<Actions> = _uiAction.asSharedFlow()

    private val _uiData = MutableStateFlow<Data?>(null)
    val uiData: StateFlow<Data?> = _uiData.asStateFlow()

     fun getWorklist() {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val worksResponse = api?.getWorklist()
                emit(worksResponse)
            }.catch { e ->
                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
            }.collect { worksResponse ->
                _uiData.value = Data.WorklistResponse(
                    works=worksResponse
                )
            }
        }
    }

    sealed class Data{
        class WorklistResponse(
            val works: List<com.example.agrotracker.api.responses.WorklistResponse>?=null
        ):Data()


    }
    sealed class Actions{
        class NavigateToWorkInfoFragment(val item: WorklistItemModel): Actions()
        class ShowToast(val message: String): Actions()
    }


}