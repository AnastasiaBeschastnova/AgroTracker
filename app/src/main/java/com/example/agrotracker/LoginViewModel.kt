package com.example.agrotracker

import androidx.lifecycle.ViewModel
import com.example.agrotracker.api.NetworkService
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