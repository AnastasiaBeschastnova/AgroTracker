package com.example.agrotracker

import androidx.lifecycle.ViewModel
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkParameterValuesRequest
import com.example.agrotracker.localdata.AgroTrackerPreferences
import com.example.agrotracker.operator.EndWorkFormViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val api by lazy{ NetworkService.instance?.agroTrackerApi}

    private val _uiAction = MutableSharedFlow<Actions>()
    val uiAction: SharedFlow<Actions> = _uiAction.asSharedFlow()

    private var preferences: AgroTrackerPreferences? = null

    var loginVisibility = false

    fun setPreferences(preferences: AgroTrackerPreferences) {
        this.preferences = preferences
    }

    fun checkToken(){
        if(preferences?.getToken()!=null){
            selectUserInfo(preferences?.getToken().toString())
            loginVisibility = false
        }
        else{//если нет сохраненного токена, нужно авторизоваться
            loginVisibility=true
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
                    _uiAction.emit(Actions.ToOperator(selectUserInfoResponse.id))
                } else if (selectUserInfoResponse?.role == "Администратор") {
                    _uiAction.emit(Actions.ToAdmin())
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
                    _uiAction.emit(Actions.ToOperator(loginResponse.id))
                } else if (loginResponse?.role == "Администратор") {
                    _uiAction.emit(Actions.ToAdmin())
                }
            }
        }
    }



    sealed class Actions{
        class ToOperator(val id: Int): Actions()
        class ToAdmin(): Actions()
        class ShowToast(val message: String): Actions()
    }
}