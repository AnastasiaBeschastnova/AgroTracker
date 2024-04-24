package com.example.agrotracker.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.responses.CulturesResponse
import com.example.agrotracker.api.responses.FieldsResponse
import com.example.agrotracker.api.responses.TechnicsResponse
import com.example.agrotracker.api.responses.WorktypesResponse
import com.example.agrotracker.api.utils.getCIdByName
import com.example.agrotracker.api.utils.getFIdByName
import com.example.agrotracker.api.utils.getTIdByName
import com.example.agrotracker.api.utils.getWIdByName
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
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class StartWorkFormViewModel : ViewModel() {

    private val api by lazy { NetworkService.instance?.agroTrackerApi }
    private val _uiData = MutableStateFlow<Data?>(null)
    val uiData: StateFlow<Data?> = _uiData.asStateFlow()

    private val _uiAction = MutableSharedFlow<Actions>()
    val uiAction: SharedFlow<Actions> = _uiAction.asSharedFlow()

    var startTime: String = ""
    private var workTypes: List<WorktypesResponse>? = null
    private var fields: List<FieldsResponse>? = null
    private var technics: List<TechnicsResponse>? = null
    private var cultures: List<CulturesResponse>? = null





    fun getStartForm() {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val startFormResponse = api?.getStartForm()
                emit(startFormResponse)
            }.retry(3)
                .catch { e ->
                    _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
                }.collect { startFormResponse ->
                    workTypes = startFormResponse?.workTypes
                    val worktypes_names = workTypes?.map { it.worktypeName }

                    fields = startFormResponse?.fields
                    val fields_names = fields?.map { it.fieldName }

                    technics = startFormResponse?.technics
                    val technics_names = technics?.map { it.technicName }

                    cultures = startFormResponse?.cultures
                    val cultures_names = cultures?.map { it.cultureName }

                    _uiData.value = Data.FieldsLists(
                        worktypes_names,
                        fields_names,
                        technics_names,
                        cultures_names
                    )

                }
        }
    }

    fun selectWorkType(worktype: String){
        val worktypeId =
            workTypes?.getWIdByName(worktype)
        _uiData.value = Data.IsCulturesVisible(worktypeId == 2)
    }

    fun sendWork(
        worktype: String,
        field: String,
        culture:String,
        technic:String,
        workname:String,
        creatorId:Int) = viewModelScope.launch{
        startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
        val worktype_id =
            workTypes?.getWIdByName(worktype)
        val field_id = fields?.getFIdByName(field)
        val technic_id = technics?.getTIdByName(technic)
        if (workname != "") {
            var culture =
                if (worktype_id == 2) cultures?.getCIdByName(culture) ?: 0
                else 0
            insertWork(
                culture,
                technic_id ?: 0,
                field_id ?: 0,
                worktype_id ?:0 ,
                creatorId,
                workname,
                startTime
            )
        } else {
            _uiAction.emit(Actions.ShowToast("Введите название обработки"))
        }
    }
    private fun insertWork(
        cultureId: Int, technicId: Int, fieldId: Int,
        workTypeId: Int, creatorId: Int, name: String, startTime: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val insertWorkResponse = api?.insertWork(
                    InsertWorkRequest(
                        cultureId,
                        technicId,
                        fieldId,
                        workTypeId,
                        creatorId,
                        name,
                        startTime
                    )
                )
                emit(insertWorkResponse)
            }.catch { e ->
                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
            }.collect { insertWorkResponse ->
                _uiAction.emit(Actions.NavigateToContinueWorkFragment(startTime,workTypeId))

            }
        }
    }

    sealed class Data{
        class FieldsLists(
            val worktypesNames: List<String?>?,
            val fieldsNames: List<String?>?,
            val technicsNames: List<String?>?,
            val culturesNames: List<String?>?,
        ): Data()

        class IsCulturesVisible(
            val isVisible: Boolean,
        ): Data()
    }

    sealed class Actions{
        class ShowToast(val message: String): Actions()

        class NavigateToContinueWorkFragment(
            val startTime: String,
            val workTypeId: Int,
        ):Actions()
    }
}