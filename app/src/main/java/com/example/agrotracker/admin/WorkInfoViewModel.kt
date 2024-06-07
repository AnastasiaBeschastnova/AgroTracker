package com.example.agrotracker.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrotracker.api.NetworkService
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

class WorkInfoViewModel : ViewModel() {
    private val api by lazy { NetworkService.instance?.agroTrackerApi }

    private val _uiAction = MutableSharedFlow<Actions>()
    val uiAction: SharedFlow<Actions> = _uiAction.asSharedFlow()

    private val _uiData = MutableStateFlow<Data?>(null)
    val uiData: StateFlow<Data?> = _uiData.asStateFlow()

    private var timerIsStarted = false
    private var startTime: Date = Date()
    private var currentTime: Date = Date()


    fun getWorkInfo(workId: Int) {
        //получение информации о полевой работе
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val workInfoResponse = api?.workInfo(workId)
                emit(workInfoResponse)
            }.catch { e ->
                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
            }.collect { workInfoResponse ->
                if (workInfoResponse?.endTime == "В процессе") {
                    //если работа в процессе выполнения, запустить таймер со временем в пути
                    val start = workInfoResponse.startTime?.split("\"")
                    this@WorkInfoViewModel.startTime =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(start?.get(1))
                    startTimer()
                }
                val lats = workInfoResponse?.points?.map { it.lat ?: 0.0 }.orEmpty()
                val lons = workInfoResponse?.points?.map { it.lon ?: 0.0 }.orEmpty()
                var workArea: List<Double>? = listOf()
                var workCenter: List<Double>? = listOf()
                var workScale = 0.05f
                //определяем участок карты
                if (lats.size != 0 && lons.size != 0) {
                    var minLat = lats[0]
                    var maxLat = lats[0]
                    var minLong = lons[0]
                    var maxLong = lons[0]
                    for (i in 0..(lats.size - 1)) {
                        if (lats[i] < minLat) minLat = lats[i]
                        if (lats[i] > maxLat) maxLat = lats[i]
                        if (lons[i] < minLong) minLong = lons[i]
                        if (lons[i] > maxLong) maxLong = lons[i]
                    }
                    //определяем центр отображаемой карты для удобного размещения маршрута сельскохозяйственной техники
                    if (lats.size > 1 && minLat != maxLat && minLong != maxLong) {//если точка не одна и они все разные
                        workArea = listOf(maxLat, maxLong, minLat, minLong)
                        workCenter =
                            listOf(
                                minLat + (maxLat - minLat) / 2,
                                minLong + (maxLong - minLong) / 2
                            )
                        workScale = 1.25f
                    }
                    if (lats.size == 1 || (minLat == maxLat && minLong == maxLong)) {//если точка одна (или все одинаковые), то ее сделать центром
                        workArea =
                            listOf(
                                lats[0] * 1.0001,
                                lons[0] * 1.0001,
                                lats[0] * 0.9999,
                                lons[0] * 0.9999
                            )
                        workCenter = listOf(lats[0], lons[0])
                        workScale = 1.25f
                    }
                }
                if (lats.size == 0) {//если точек нет
                    val fieldAreaPoint = workInfoResponse?.fieldArea?.get(0)
                    fieldAreaPoint?.let {
                        workArea = listOf(
                            fieldAreaPoint[0].times(1.0001),
                            fieldAreaPoint[1].times(1.0001),
                            fieldAreaPoint[0].times(0.9999),
                            fieldAreaPoint[1].times(0.9999)
                        )
                    }
                    workScale = 0.05f
                    workCenter = fieldAreaPoint
                }

                //данные о полевой работе
                _uiData.value = Data.WorkInfo(
                    field = workInfoResponse?.fieldName.toString(),
                    workType = workInfoResponse?.workTypeName.toString(),
                    culture = workInfoResponse?.cultureName.toString(),
                    technic = workInfoResponse?.technicName.toString(),
                    workId = workInfoResponse?.workId.toString(),
                    creator = workInfoResponse?.creatorName.toString(),
                    workName = workInfoResponse?.name.toString(),
                    startTime = workInfoResponse?.startTime.toString(),
                    endTime = workInfoResponse?.endTime.toString(),
                    fuel = workInfoResponse?.fuel.toString(),
                    secondParameterName = workInfoResponse?.secondParameterName.toString(),
                    secondParameterValue = workInfoResponse?.secondParameterValue.toString(),
                    pointTimes = workInfoResponse?.points?.map { it.pointTime.orEmpty() }.orEmpty(),
                    lats = lats,
                    lons = lons,
                    pointIds = workInfoResponse?.points?.map { it.id ?: 0 }.orEmpty(),
                    fieldArea = workInfoResponse?.fieldArea,
                    workArea = workArea,
                    workCenter = workCenter,
                    workScale = workScale
                )
            }
        }
    }

    private fun startTimer() = CoroutineScope(Dispatchers.Main).launch {
        //запуск таймера в пути
        timerIsStarted = true
        while (timerIsStarted) {
            val nowTime = Date()
            val pathTime = kotlin.math.abs(nowTime.time - startTime.time)//миллисекунды
            currentTime = Date(pathTime)
            updateTimerText()
            delay(1000)
        }
    }

    private fun updateTimerText() = viewModelScope.launch {
        //обновление текста таймера
        _uiData.value = Data.Timer(
            timer = SimpleDateFormat("HH:mm:ss").apply {
                timeZone = TimeZone.getTimeZone("GMT+00:00")
            }.format(currentTime)
        )
    }

    sealed class Data {

        class WorkInfo(
            val field: String,
            val workType: String,
            val culture: String,
            val technic: String,
            val workId: String,
            val creator: String,
            val workName: String,
            val startTime: String,
            val endTime: String? = null,
            val fuel: String? = null,
            val secondParameterName: String? = null,
            val secondParameterValue: String? = null,
            val pointTimes: List<String?>? = null,
            val lats: List<Double>? = null,
            val lons: List<Double>? = null,
            val pointIds: List<Int?>? = null,
            val fieldArea: List<List<Double>>?,
            val workArea: List<Double>?,
            val workCenter: List<Double>?,
            val workScale: Float
        ) : Data()

        class Timer(
            var timer: String,
        ) : Data()
    }

    sealed class Actions {
        class NavigateToWorkListFragment() : Actions()
        class ShowToast(val message: String) : Actions()
    }
}