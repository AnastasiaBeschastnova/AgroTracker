package com.example.agrotracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.localdata.AgroTrackerPreferences
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class LoginViewModel : ViewModel() {

    private val api by lazy{ NetworkService.instance?.agroTrackerApi}

    private val _uiAction = MutableSharedFlow<Actions>()
    val uiAction: SharedFlow<Actions> = _uiAction.asSharedFlow()

    private var preferences: AgroTrackerPreferences? = null


    private val _loginVisibility = MutableStateFlow<Boolean>(false)
    val loginVisibility: StateFlow<Boolean> = _loginVisibility.asStateFlow()
    private var startTime: String = ""
    private var creatorId: Int = 0


    fun setPreferences(preferences: AgroTrackerPreferences) {
        this.preferences = preferences
    }

    fun checkToken() = viewModelScope.launch{
        if(preferences?.getToken()!=null){
            selectUserInfo(preferences?.getToken().toString())
        }
        else{//если нет сохраненного токена, нужно авторизоваться
            _loginVisibility.emit(true)
        }
    }
    fun checkWorks(){
        if(preferences?.getToken()!=null){
            selectUserInfo(preferences?.getToken().toString())
        }
    }

    private fun selectUserInfo(token: String){
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val selectUserInfoResponse = api?.selectUserInfo(token)
                emit(selectUserInfoResponse)
            }.catch { e ->
                val message = when(e){
                    is retrofit2.HttpException -> {
                        when(e.code()){
                            404 -> "Нет такого пользователя"
                            else -> "Ошибка сервера"
                        }
                    }
                    else -> "Внутренняя ошибка, ${e.message}"
                }
                _uiAction.emit(Actions.ShowToast(message))
            }.collect { selectUserInfoResponse ->
                if (selectUserInfoResponse?.role == "Оператор") {
                    selectOperatorWorks(selectUserInfoResponse.id)
                } else if (selectUserInfoResponse?.role == "Администратор") {
                    _uiAction.emit(Actions.ToAdmin())
                }
            }
        }
    }

    private fun selectOperatorWorks(creatorId: Int){
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val selectOperatorWorksResponse = api?.selectOperatorWorks(creatorId)
                emit(selectOperatorWorksResponse)
            }.catch { e ->
                val message = when(e){
                    is retrofit2.HttpException -> {
                        when(e.code()){
                            404 -> "Неверные логин или пароль"
                            else -> "Ошибка сервера"
                        }
                    }
                    else -> "Внутренняя ошибка, ${e.message}"
                }
                _uiAction.emit(Actions.ShowToast(message))
            }.collect { selectOperatorWorksResponse ->
                if(selectOperatorWorksResponse?.comment=="ContinueWorkFragment"){
                    if(selectOperatorWorksResponse.startTime!=null &&
                        selectOperatorWorksResponse.workTypeId !=null){
                        val start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSX").parse(selectOperatorWorksResponse.startTime)
                        startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(start)
                        _uiAction.emit(Actions.ToContinueWork(
                            creatorId,
                            startTime,
                            selectOperatorWorksResponse.workTypeId
                        ))
                    }
                }
                else if(selectOperatorWorksResponse?.comment=="EndWorkFormFragment"){
                    if(selectOperatorWorksResponse.startTime!=null &&
                        selectOperatorWorksResponse.workTypeId !=null
                        && selectOperatorWorksResponse.workId !=null){
                        val start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSX").parse(selectOperatorWorksResponse.startTime)
                        startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(start)
                        _uiAction.emit(Actions.ToEndWork(
                            selectOperatorWorksResponse.workId,
                            startTime,
                            selectOperatorWorksResponse.workTypeId,
                            creatorId
                        ))
                    }
                }
                else{
                    _uiAction.emit(Actions.ToStartWork(creatorId))
                }
            }
        }
    }
    fun login(login: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val loginResponse = api?.login(login, password)
                emit(loginResponse)
            }.catch { e ->
                val message = when(e){
                    is retrofit2.HttpException -> {
                        when(e.code()){
                            404 -> "Неверные логин или пароль"
                            else -> "Ошибка сервера"
                        }
                    }
                    else -> "Внутренняя ошибка, ${e.message}"
                }
                _uiAction.emit(Actions.ShowToast(message))
            }.collect { loginResponse ->
                // запись токена в префы
                if(loginResponse?.token!=null)
                {preferences?.saveToken(loginResponse.token)}
                if (loginResponse?.role == "Оператор") {
                    creatorId = loginResponse.id
                    _uiAction.emit(Actions.ToStartWork(loginResponse.id))
                } else if (loginResponse?.role == "Администратор") {
                    _uiAction.emit(Actions.ToAdmin())
                }
            }
        }
    }



    sealed class Actions{
        class ToStartWork(val id: Int): Actions()
        class ToContinueWork(
            val creatorId: Int,
            val startTime: String,
            val workTypeId: Int
        ): Actions()
        class ToEndWork(
            val workId: Int,
            val startTime: String,
            val workTypeId: Int,
            val creatorId: Int
        ): Actions()
        class ToAdmin(): Actions()
        class ShowToast(val message: String): Actions()
    }
}