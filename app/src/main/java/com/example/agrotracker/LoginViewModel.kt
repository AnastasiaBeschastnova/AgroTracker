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

    fun setPreferences(preferences: AgroTrackerPreferences) {
        this.preferences = preferences
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
//                AgroTrackerPreferences()
                if (loginResponse?.role == "Оператор") {
                    _uiAction.emit(Actions.ToOperator(loginResponse.id))
                } else if (loginResponse?.role == "Администратор") {
                    _uiAction.emit(Actions.ToAdmin())
                }
//                if(loginResponse?.id != null)
//                {
//                    insertUserKey(loginResponse.id)
//                }
            }
        }
    }

//    private fun insertUserKey(userId: Int){
//        CoroutineScope(Dispatchers.Main).launch {
//            flow {
//                val insertUserKeyResponse = api?.insertUserKey(
//                    UserKeyResponse(userId)
//                )
//                emit(insertUserKeyResponse)
//            }.catch { e ->
//                _uiAction.emit(Actions.ShowToast(e.message.orEmpty()))
//            }.collect { insertUserKeyResponse ->
//
//            }
//        }
//    }


    sealed class Actions{
        class ToOperator(val id: Int): Actions()
        class ToAdmin(): Actions()
        class ShowToast(val message: String): Actions()
    }
}