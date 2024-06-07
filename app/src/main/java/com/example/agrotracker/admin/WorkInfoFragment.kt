package com.example.agrotracker.admin

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.databinding.FragmentWorkInfoBinding
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class WorkInfoFragment : Fragment() {

    private var _binding: FragmentWorkInfoBinding? = null
    private val args: WorkInfoFragmentArgs by navArgs()
    private val viewModel: WorkInfoViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect {
                    when (it) {
                        //отображение подробной информации о полевой работе
                        is WorkInfoViewModel.Data.WorkInfo -> {
                            showWorkInfo(it)
                        }
                        // обновление таймера выполнения полевой работы
                        is WorkInfoViewModel.Data.Timer -> {
                            updateTimerText(it)
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiAction.collect {
                    when (it) {
                        // отображение всплывающих сообщений (тостов) в нижней части экрана
                        is WorkInfoViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                        // навигация на экран со списком полевых работ
                        is WorkInfoViewModel.Actions.NavigateToWorkListFragment -> {
                            findNavController().navigate(
                                WorkInfoFragmentDirections
                                    .actionWorkInfoFragmentToWorklistFragment()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentWorkInfoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //изначально информации о точках не видно, пока на какую-либо точку маршрута не нажмет пользователь
        binding.pointId.isVisible = false
        binding.coords.isVisible = false
        binding.pointTime.isVisible = false
        viewModel.getWorkInfo(args.work.workId)//получение информации о полевой работе по ее ID
        Toast.makeText(requireContext(), "Карта загружается. Подождите", Toast.LENGTH_LONG).show()
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_WorkInfoFragment_to_WorklistFragment)
        }
        //отображение карты
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(17.0)
    }

    private fun showWorkInfo(workInfo: WorkInfoViewModel.Data.WorkInfo) {
        //отображение подробной информации о полевой работе
        binding.field.text = "Поле: " + workInfo.field
        binding.worktype.text = "Тип обработки: " + workInfo.workType
        binding.culture.text = "Культура: " + workInfo.culture
        binding.technic.text = "Техника: " + workInfo.technic
        binding.workId.text = "ID: " + workInfo.workId
        binding.creator.text = "Оператор: " + workInfo.creator
        binding.workname.text = "Обработка: " + workInfo.workName
        val start = convertTime(workInfo.startTime)//разбиваем пришедшую строку на дату, время и часовой пояс
        binding.starttime.text =
            "Начало обработки: \n" + start[0] + " " + start[1] + " (UTC+" + start[2] + ")"
        if (workInfo.endTime != "В процессе") {
            //если работа завершена, то вывод времени окончания выполнения полевой работы
            val end = convertTime(workInfo.endTime)
            binding.endtime.text =
                "Конец обработки: \n" + end[0] + " " + end[1] + " (UTC+" + end[2] + ")"
            //если параметры данных, вводимых по окончании полевой работы, не пусты, вывести их
            if (workInfo.fuel != "null") {
                binding.fuel.isVisible = true
                binding.fuel.text = "Топливо, л: " + workInfo.fuel.toString()
            }
            if (workInfo.secondParameterName != "null" && workInfo.secondParameterValue != "null") {
                binding.secondParameter.isVisible = true
                binding.secondParameter.text =
                    workInfo.secondParameterName.toString() + ": " + workInfo.secondParameterValue.toString()
            }
        }
        //получение точек маршрута сельскохозяйственной техники
        val pointTimes = workInfo.pointTimes
        val lats = workInfo.lats
        val lons = workInfo.lons
        val pointIds = workInfo.pointIds

        //Отрисовка на карте границ поля,на котором выполнялась работа
        val area = Polygon(binding.mapview)
        val polygon: MutableList<GeoPoint> = ArrayList()
        val field_area = workInfo.fieldArea
        if (field_area != null) {
            for (i in 0..field_area.size.minus(1)) {
                polygon.add(GeoPoint(field_area[i][0], field_area[i][1]))
            }
        }
        area.points = polygon
        area.fillColor = Color.argb(128, 120, 193, 85)//dark_green color
        area.strokeColor = Color.parseColor("#FF78C155")
        area.strokeWidth = 5f
        area.isGeodesic = true
        area.infoWindow = null
        binding.mapview.getOverlays().add(area)
        binding.mapview.invalidate()

        //Вывод маршрута полевой работы на карту
        pointTimes?.let {
            lats?.let {
                lons?.let {
                    pointIds?.let {
                        //если есть какие-то точки маршрута
                        //Отрисовка маршрута по точкам в виде полилинии
                        val line = Polyline(binding.mapview)
                        val polyline: MutableList<GeoPoint> = ArrayList()
                        for (i in 0..lats.size.minus(1)) {
                            polyline.add(GeoPoint(lats[i], lons[i]))
                        }
                        line.setPoints(polyline)
                        line.width = 5f
                        line.setColor(Color.parseColor("#FFA500"))
                        line.isGeodesic = true
                        line.infoWindow = null
                        binding.mapview.getOverlays().add(line)
                        binding.mapview.invalidate()

                        //Вывод маршрута полевой работы на карту
                        val points: MutableList<IGeoPoint> = ArrayList()
                        for (i in 0..pointTimes.size.minus(1)) {
                            points.add(
                                LabelledGeoPoint(
                                    lats[i], lons[i], "№" + (i + 1).toString()
                                )
                            )
                        }
                        val pt = SimplePointTheme(points, true)

                        val textStyle = Paint()
                        textStyle.style = Paint.Style.FILL
                        textStyle.setColor(Color.parseColor("#000000"))//черный
                        textStyle.textAlign = Paint.Align.CENTER
                        textStyle.textSize = 24f

                        val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                            .setRadius(7f).setIsClickable(true).setCellSize(20)
                            .setTextStyle(textStyle)

                        val sfpo = SimpleFastPointOverlay(pt, opt)
                        //отображение подробной информации о точке маршрута, на которую нажал пользователь
                        sfpo.setOnClickListener { points, point ->
                            binding.pointId.isVisible = true
                            binding.coords.isVisible = true
                            binding.pointTime.isVisible = true
                            binding.pointId.text =
                                "ID точки №" + (point + 1).toString() + ": " + pointIds[point]
                            binding.coords.text =
                                "Координаты: \n" + lats[point].toString() + " (ш.), \n" + lons[point].toString() + " (д.)"
                            val cPointTime = convertTime(pointTimes[point])
                            binding.pointTime.text =
                                "Время: " + cPointTime[0] + " " + cPointTime[1] + " (UTC+" + cPointTime[2] + ")"
                        }
                        binding.mapview.getOverlays().add(sfpo)//добавление слоя с маршрутом на карту
                    }
                }
            }

            //Размещение на карте области, на которой располагается маршрут, для отображения только интересующей области
            workInfo.workArea?.let {
                val boundingBox = BoundingBox(it[0], it[1], it[2], it[3])
                binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(1.25f), false)
            }
            workInfo.workCenter?.let {
                binding.mapview.controller.setCenter(
                    GeoPoint(
                        it[0], it[1]
                    )
                )
            }
        }
        if (pointTimes?.size?.compareTo(0) == 0) {//если нет маршрута сельскохозяйственной техники
            binding.pointId.isVisible = true
            binding.pointId.text = "Точек маршрута нет."
        }
    }

    private fun updateTimerText(timer: WorkInfoViewModel.Data.Timer) {
        //таймер времени выполнения полевой работы
        binding.endtime.text = "В пути: " + timer.timer
    }


    private fun convertTime(cTime: String?): List<String> {
        //разделение строки с датой и временем на дату, время и часовой пояс
        val start_time_split = cTime?.split("\"")
        val start_time_value = start_time_split?.get(1)?.split("T")
        val start_date = start_time_value?.get(0)//yyyy-MM-dd
        val start_time_tz = start_time_value?.get(1)//hh:mm:ss.SSSSSS+03:00
        val start_time_tz_split = start_time_tz?.split("+")
        val start_time = start_time_tz_split?.get(0)//hh:mm:ss.SSSSSS
        val time_zone = start_time_tz_split?.get(1)//03:00
        val convertedTime =
            listOf(start_date.toString(), start_time.toString(), time_zone.toString())
        return convertedTime
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}