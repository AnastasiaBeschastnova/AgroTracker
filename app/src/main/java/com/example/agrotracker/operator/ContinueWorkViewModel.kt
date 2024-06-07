package com.example.agrotracker.operator

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertPointRequest
import com.example.agrotracker.api.requests.UpdateWorkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class ContinueWorkViewModel : ViewModel() {

    private var workId: Int? = null
    private var location: Location? = null
    private val api by lazy { NetworkService.instance?.agroTrackerApi }
    private val _uiData = MutableStateFlow<ContinueWorkViewModel.Data?>(null)
    val uiData: StateFlow<ContinueWorkViewModel.Data?> = _uiData.asStateFlow()
    private var timerIsStarted = false
    private var startTime: Date = Date()
    private var currentTime: Date = Date()

    private val _uiAction = MutableSharedFlow<ContinueWorkViewModel.Actions>()
    val uiAction: SharedFlow<ContinueWorkViewModel.Actions> = _uiAction.asSharedFlow()

    fun sendUpdatedWork(workId: Int, endTime: String, workTypeId: Int) = viewModelScope.launch {
        //добавить существующей полевой работе в базе данных время окончания ее выполнения, остановить таймер времени в пути
        updateWork(workId, endTime, workTypeId)
        stopTimer()
    }

    private fun updateWork(workId: Int, endTime: String, workTypeId: Int) {
        //обновить полевую работу в базе данных
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val updateWorkResponse = api?.updateWork(
                    UpdateWorkRequest(workId, endTime)
                )
                emit(updateWorkResponse)
            }.catch { e ->
                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
            }.collect { updateWorkResponse ->
                _uiAction.emit(Actions.NavigateToEndWorkFormFragment(endTime, workId, workTypeId))
            }
        }
    }

    fun sendPoint(location: Location) = viewModelScope.launch {
        //отправлять геолокацию устройства оператора для построения маршрута сельскохозяйственной техники
        this@ContinueWorkViewModel.location = location
        updateUiData()
        insertPoint(location.latitude, location.longitude)
    }

    private fun insertPoint(
        lat: Double, lon: Double
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            updateUiData()
            workId?.let { workIdNotNull ->
                flow {
                    val insertPointResponse = api?.insertPoint(
                        InsertPointRequest(
                            workIdNotNull,
                            lat,
                            lon,
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
                        )
                    )
                    emit(insertPointResponse)
                }.catch { e ->
                    _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
                }.collect { insertPointResponse ->
                    println(insertPointResponse)
                }
            }
        }
    }

    fun selectWorkId(creatorId: Int, startTime: String) {
        //получить ID созданной оператором полевой работы
        CoroutineScope(Dispatchers.Main).launch {
            this@ContinueWorkViewModel.startTime =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(startTime)
            flow {
                val selectWorkIdResponse = api?.selectWorkId(creatorId, startTime)
                emit(selectWorkIdResponse)
            }.catch { e ->
                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
            }.collect { selectWorkIdResponse ->
                if (selectWorkIdResponse?.work_id != null) {
                    workId = selectWorkIdResponse.work_id
                    startTimer()
                }
            }
        }
    }

    private fun updateUiData() = viewModelScope.launch {
        //обновлять таймер и геолокацию
        workId?.let {
            if (location != null)
                _uiData.value = Data.UiData(
                    workId = it,
                    currentTime = SimpleDateFormat("HH:mm:ss").apply {
                        timeZone = TimeZone.getTimeZone("GMT+00:00")
                    }.format(currentTime),
                    location = location
                )
        }
    }

    private fun startTimer() = CoroutineScope(Dispatchers.Main).launch {
        //запуск таймера в пути
        timerIsStarted = true
        while (timerIsStarted) {
            val nowTime = Date()
            val pathTime = kotlin.math.abs(nowTime.time - startTime.time)//миллисекунды
            currentTime = Date(pathTime)
            updateUiData()
            delay(1000)
        }
    }

    private fun stopTimer() {//остановка таймера
        timerIsStarted = false
    }

    sealed class Data {
        class UiData(
            val workId: Int,
            val currentTime: String,
            val location: Location?
        ) : Data()
    }

    sealed class Actions {
        class ShowToast(val message: String) : Actions()

        class NavigateToEndWorkFormFragment(
            val endTime: String,
            val workId: Int,
            val workTypeId: Int,
        ) : Actions()
    }
}