package com.example.agrotracker.api

import com.example.agrotracker.api.requests.InsertPointRequest
import com.example.agrotracker.api.requests.InsertWorkParameterValuesRequest
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.requests.UpdateWorkRequest
import com.example.agrotracker.api.responses.AuthInfoResponse
import com.example.agrotracker.api.responses.SelectOperatorWorks
import com.example.agrotracker.api.responses.SelectWorkIdResponse
import com.example.agrotracker.api.responses.StartFormResponse
import com.example.agrotracker.api.responses.WorklistResponse
import com.example.agrotracker.api.responses.WorkInfoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

public interface AgroTrackerApi {
    @GET("/agro_tracker/users")
    //авторизация в системе
    suspend fun login(@Query("login") login: String, @Query("password") password: String) : AuthInfoResponse


    @GET("/agro_tracker/works")
    //получение списка всех полевых работ
    suspend fun getWorklist() : List<WorklistResponse>

    @GET("/agro_tracker/start_form")
    //получение параметров, выбираемых из выпадающих списков перед созданием полевой работы
    suspend fun getStartForm() : StartFormResponse

    @POST("/agro_tracker/points/insert")
    //добавление в базу данных геолокации сельскохозяйственной техники, чтобы по этим точкам затем был отрисован маршрут
    suspend fun insertPoint(@Body insertPoint: InsertPointRequest) : Any

    @GET("/agro_tracker/works/{work_id}")
    //вывод подробной информации о выбранной полевой работе
    suspend fun workInfo(@Path("work_id") workId: Int) : WorkInfoResponse

    @POST("/agro_tracker/works/insert")
    //добавление в базу данных новой полевой работы
    suspend fun insertWork(@Body insertWork: InsertWorkRequest) : Any

    @GET("/agro_tracker/works/{creator_id}&{start_time}")
    //вывод IDтолько что созданной оператором полевой работы
    suspend fun selectWorkId(@Path("creator_id") creatorId: Int,
                             @Path("start_time") startTime: String,) : SelectWorkIdResponse

    @POST("/agro_tracker/works/update")
    //обновить существующую полевую работу: добавить время окончания ее выполнения
    suspend fun updateWork(@Body updateWork: UpdateWorkRequest) : Any

    @POST("/agro_tracker/work_parameter_values")
    //добавить в базу данных параметры полевой работы, вводимые по ее окончании
    suspend fun insertWorkParameterValues(@Body insertWorkParameterValuesRequest: InsertWorkParameterValuesRequest) : Any

    @GET("/agro_tracker/user_info")
    //вывод информации о пользователе
    suspend fun selectUserInfo(@Query("token") token: String) : AuthInfoResponse

    @GET("/agro_tracker/works/operator/")
    //проверка наличия у оператора незавершенных полевых работ
    suspend fun selectOperatorWorks(@Query("creator_id") creatorId: Int) : SelectOperatorWorks


}