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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.databinding.FragmentWorkInfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
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
    private val api by lazy { NetworkService.instance?.agroTrackerApi }
    private val geoVlg = GeoPoint(48.7070, 44.5169)//Волгоград



    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentWorkInfoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pointId.isVisible = false
        binding.coords.isVisible = false
        binding.pointTime.isVisible = false
        workInfo(args.work.workId)
        Toast.makeText(requireContext(), "Карта загружается. Подождите", Toast.LENGTH_LONG).show()
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminThirdFragment_to_adminSecondFragment)
        }
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(17.0)


    }

    private fun workInfo(workId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val workInfoResponse = api?.workInfo(workId)
                emit(workInfoResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { workInfoResponse ->
                binding.field.text = "Поле: " + workInfoResponse?.fieldName.toString()
                binding.worktype.text =
                    "Тип обработки: " + workInfoResponse?.workTypeName.toString()
                binding.culture.text = "Культура: " + workInfoResponse?.cultureName.toString()
                binding.technic.text = "Техника: " + workInfoResponse?.technicName.toString()
                binding.workId.text = "ID: " + workInfoResponse?.workId.toString()
                binding.creator.text = "Оператор: " + workInfoResponse?.creatorName.toString()
                binding.workname.text = "Обработка: " + workInfoResponse?.name.toString()
                binding.starttime.text =
                    "Начало обработки: " + workInfoResponse?.startTime.toString()

                if (workInfoResponse?.endTime.isNullOrBlank()) {
                    binding.endtime.text = "Конец обработки: не закончена"
                } else {
                    binding.endtime.text =
                        "Конец обработки: " + workInfoResponse?.endTime.toString()
                    if (workInfoResponse?.fuel != null) {
                        binding.fuel.isVisible = true
                        binding.fuel.text = "Топливо, л: " + workInfoResponse?.fuel.toString()
                    }
                    if (!workInfoResponse?.secondParameterName.isNullOrBlank() && workInfoResponse?.secondParameterValue != null) {
                        binding.secondParameter.isVisible = true
                        binding.secondParameter.text =
                            workInfoResponse?.secondParameterName.toString() + ": " + workInfoResponse?.secondParameterValue.toString()
                    }
                }
                val pointTimes = workInfoResponse?.points?.map { it.pointTime.orEmpty() }.orEmpty()
                val lats = workInfoResponse?.points?.map { it.lat ?: 0.0 }.orEmpty()
                val lons = workInfoResponse?.points?.map { it.lon ?: 0.0 }.orEmpty()
                val pointIds = workInfoResponse?.points?.map { it.id ?: 0 }.orEmpty()


                //Вывод маршрута полевой работы на карту
                if (pointTimes.size > 0) {//если есть какие-то точки маршрута
                    val points: MutableList<IGeoPoint> = ArrayList()
                    val len = pointTimes.size - 1
                    for (i in 0..len) {
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
                        .setRadius(7f).setIsClickable(true).setCellSize(20).setTextStyle(textStyle)

                    val sfpo = SimpleFastPointOverlay(pt, opt)

                    sfpo.setOnClickListener { points, point ->
                        binding.pointId.isVisible = true
                        binding.coords.isVisible = true
                        binding.pointTime.isVisible = true
                        binding.pointId.text =
                            "ID точки №" + (point + 1).toString() + ": " + pointIds[point]
                        binding.coords.text =
                            "Координаты: " + lats[point].toString() + " (ш.), " + lons[point].toString() + " (д.)"
                        binding.pointTime.text = "Время: " + pointTimes[point]
                    }
                    binding.mapview.getOverlays().add(sfpo)
                    //Вычисление область, на которой располагается маршрут, для отображения только интересующей области
                    var minLat = lats[0]
                    var maxLat = lats[0]
                    var minLong = lons[0]
                    var maxLong = lons[0]

                    for (i in 0..(points.size - 1)) {
                        if (lats[i] < minLat) minLat = lats[i]
                        if (lats[i] > maxLat) maxLat = lats[i]
                        if (lons[i] < minLong) minLong = lons[i]
                        if (lons[i] > maxLong) maxLong = lons[i]
                    }
                    if (points.size > 1) {//если точка не одна
                        val boundingBox = BoundingBox(maxLat, maxLong, minLat, minLong)
                        binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(1.25f), false)
                        binding.mapview.controller.setCenter(
                            GeoPoint(
                                minLat + (maxLat - minLat) / 2, minLong + (maxLong - minLong) / 2
                            )
                        )
                    } else {//если точка одна, то ее сделать центром
                        val boundingBox = BoundingBox(
                            lats[0] * 1.01,
                            lons[0] * 1.01,
                            lats[0] * 0.99,
                            lons[0] * 0.99
                        )
                        binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(0.05f), false)
                        binding.mapview.controller.setCenter(
                            GeoPoint(
                                lats[0], lons[0]
                            )
                        )
                    }

                    //Отрисовка маршрута по точкам в виде полилинии
                    val line = Polyline(binding.mapview)
                    val polyline: MutableList<GeoPoint> = ArrayList()
                    for (i in 0..lats.size - 1) {
                        polyline.add(GeoPoint(lats[i], lons[i]))
                    }
                    line.setPoints(polyline)
                    line.width = 5f
                    line.setColor(Color.parseColor("#FFA500"))
                    line.isGeodesic = true
                    binding.mapview.getOverlays().add(line)
                    binding.mapview.invalidate()
                } else {
                    binding.pointId.isVisible = true
                    binding.pointId.text = "Точек маршрута нет."

                    val boundingBox = BoundingBox(geoVlg.altitude*1.01, geoVlg.longitude*1.01, geoVlg.altitude*0.99, geoVlg.longitude*0.99)
                    binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(0.05f), false)
                    binding.mapview.controller.setCenter(geoVlg)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}