package com.example.agrotracker.operator


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkParameterValuesRequest
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

class EndWorkFormViewModel : ViewModel() {

    private val api by lazy { NetworkService.instance?.agroTrackerApi }

    private var workId: Int? = null

    private var workTypeId: Int? = null

    private val _uiData = MutableStateFlow<EndWorkFormViewModel.Data?>(null)
    val uiData: StateFlow<EndWorkFormViewModel.Data?> = _uiData.asStateFlow()

    private val _uiAction = MutableSharedFlow<EndWorkFormViewModel.Actions>()
    val uiAction: SharedFlow<EndWorkFormViewModel.Actions> = _uiAction.asSharedFlow()


    private fun insertWorkParameterValues(
        workId: Int,
        fuel: Int, secondParameterValue: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val insertWorkParameterValuesResponse = api?.insertWorkParameterValues(
                    InsertWorkParameterValuesRequest(workId, fuel, secondParameterValue)
                )
                emit(insertWorkParameterValuesResponse)
            }.catch { e ->
                _uiAction.emit(EndWorkFormViewModel.Actions.ShowToast(e.message.orEmpty()))
            }.collect { insertWorkParameterValuesResponse ->
                _uiAction.emit(EndWorkFormViewModel.Actions.NavigateToStartWorkFormFragment())
            }
        }
    }

    fun sendWorkParameterValues(
        //workTypeId: Int,
        fuel: Int?,
        seeds: Int?,
        fertilizer: Int?,
        water: Int?,
        harvest: Int?,
    ) = viewModelScope.launch{
        val secondParameterValue = seeds ?: fertilizer ?: water ?: harvest
        val formIsValid = fuel != null && (workTypeId == 1 || secondParameterValue != null)
        if (formIsValid) {
            workId?.let {
                insertWorkParameterValues(
                    it,
                    fuel ?: 0,
                    secondParameterValue ?: 0
                )
            }
        }
        else{
            _uiAction.emit(Actions.ShowToast("Не все данные введены."))
        }
    }

    fun initData(workTypeId: Int, workId: Int) = viewModelScope.launch {
        this@EndWorkFormViewModel.workTypeId = workTypeId
        this@EndWorkFormViewModel.workId = workId
        _uiData.value = Data.Fields(
            seedsIsVisible = workTypeId == 2,
            fertilizerIsVisible = workTypeId == 3,
            waterIsVisible = workTypeId == 5,
            harvestIsVisible = workTypeId == 4
        )
    }

    sealed class Data {

        class Fields(
            val seedsIsVisible: Boolean,
            val fertilizerIsVisible: Boolean,
            val harvestIsVisible: Boolean,
            val waterIsVisible: Boolean,
        ) : Data()


    }

    sealed class Actions {
        class ShowToast(val message: String) : Actions()



        class NavigateToStartWorkFormFragment(
        ) : Actions()
    }
}