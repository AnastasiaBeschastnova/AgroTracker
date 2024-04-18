package com.example.agrotracker.admin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.databinding.FragmentWorkInfoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
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
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}
    private var pointTimes: List<String> = listOf()//workInfoResponse?.points?.map{it.pointTime}
    private var lats: List<Double> = listOf()
    private var lons: List<Double> = listOf()

    private val fusedLocationClient: FusedLocationProviderClient by lazy{
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    //private var mMapController: MapController? = null



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

        workInfo(args.work.workId)
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminThirdFragment_to_adminSecondFragment)
        }

        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)



//        val points: MutableList<IGeoPoint> = ArrayList()
//        val len = pointTimes.size-1
//        for (i in 0..9) {
//            points.add(
//                LabelledGeoPoint(
//                    48 + Math.random() * 5, 44 + Math.random() * 5, "Point #$i"
//                )
//
//            )
//        }
//        val pt = SimplePointTheme(points, true)
//
//        val textStyle = Paint()
//        textStyle.style = Paint.Style.FILL
//        textStyle.setColor(Color.parseColor("#0000ff"))
//        textStyle.textAlign = Paint.Align.CENTER
//        textStyle.textSize = 24f
//
//        val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
//            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
//            .setRadius(7f).setIsClickable(true).setCellSize(15).setTextStyle(textStyle)
//
//        val sfpo = SimpleFastPointOverlay(pt, opt)
//
//        sfpo.setOnClickListener { points, point ->
//            Toast.makeText(
//                binding.mapview.getContext(),
//                "You clicked " + (points[point] as LabelledGeoPoint).label,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//        //println(lats[0].toString()+" "+lons[0].toString())
////        println("lats: "+lats.toString())
//        binding.mapview.getOverlays().add(sfpo)
//        binding.mapview.controller.setCenter(LabelledGeoPoint(
//            48 + Math.random() * 5, 44 + Math.random() * 5, "Point center"
//        ))




        //binding.mapview.setBuiltInZoomControls(true)
        val geo = GeoPoint(48.7070, 44.5169)//Волгоград
//        pointTimes.forEachIndexed { index, it ->
//
//            val lat = lats.get(index)
//            val lon = lons.get(index)
//            val geo = GeoPoint(lat, lon)
//            binding.mapview.controller.setCenter(geo)
//            val startMarker = Marker(binding.mapview)
//            startMarker.setTitle(lat.toString()+",\n"+lon.toString())
//            startMarker.setPosition(geo)
//            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//            binding.mapview.getOverlays().add(startMarker)
//        }
//        binding.mapview.controller.setCenter(geo)
        //mMapController = binding.mapview.getController() as MapController
       // binding.mapview.controller.setZoom(13)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    //val geo = GeoPoint(location?.latitude, location?.longitude)
//                    println("${location?.latitude} ${location?.longitude}")
                    //geo = GeoPoint(location?.latitude.toDouble(), location?.longitude)
//                    pointTimes.forEachIndexed { index, it ->
//                        //Marker(binding.mapview).setPosition(GeoPoint(lats.get(index),lons.get(index)))
//
//                        val lat = lats.get(index)
//                        val lon = lons.get(index)
//                        val geo = GeoPoint(lat, lon)
//                        binding.mapview.controller.setCenter(geo)
//                        val startMarker = Marker(binding.mapview)
//                        startMarker.setTitle(lat.toString()+",\n"+lon.toString())
//                        startMarker.setPosition(geo)
//                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//                        binding.mapview.getOverlays().add(startMarker)
//                        //println(lats.get(index).toString()+" "+lons.get(index).toString())
//                    }






//                    val points: MutableList<IGeoPoint> = ArrayList()
//                    for (i in 0..9) {
//                        points.add(
//                            LabelledGeoPoint(
//                                37 + Math.random() * 5, -8 + Math.random() * 5, "Point #$i"
//                            )
//                        )
//                    }
//                    val pt = SimplePointTheme(points, true)
//
//                    val textStyle = Paint()
//                    textStyle.style = Paint.Style.FILL
//                    textStyle.setColor(Color.parseColor("#0000ff"))
//                    textStyle.textAlign = Paint.Align.CENTER
//                    textStyle.textSize = 24f
//
//                    val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
//                        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
//                        .setRadius(7f).setIsClickable(true).setCellSize(15).setTextStyle(textStyle)
//
//                    val sfpo = SimpleFastPointOverlay(pt, opt)
//
//                    sfpo.setOnClickListener { points, point ->
//                        Toast.makeText(
//                            binding.mapview.getContext(),
//                            "You clicked " + (points[point] as LabelledGeoPoint).label,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    binding.mapview.getOverlays().add(sfpo)




//                    if(location?.latitude != null && location?.longitude!=null) {
//                        val lat = location.latitude
//                        val lon = location.longitude
//                        val geo = GeoPoint(lat, lon)
//                        binding.mapview.controller.setCenter(geo)
//                        val startMarker = Marker(binding.mapview)
//                        startMarker.setTitle(lat.toString()+",\n"+lon.toString())
//                        startMarker.setPosition(geo)
//                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
//
////                        pointTimes.forEachIndexed { index, it ->
////                            Marker(binding.mapview).setPosition(GeoPoint(lats.get(index),lons.get(index)))
////
////                        }
//
//
//                        binding.mapview.getOverlays().add(startMarker)
//                        //startMarker.infoWindow.
//
//                    }
                }
        }

        binding.mapview.controller.setZoom(17.0)
        //binding.mapview.controller.setCenter(geo)


//        mMapController?.setZoom(13)
//        mMapController?.setCenter(geo)
    }

    private fun workInfo(workId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val workInfoResponse = api?.workInfo(workId)
                emit(workInfoResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { workInfoResponse ->
                binding.field.text = "Поле: "+workInfoResponse?.fieldName.toString()
                binding.worktype.text = "Тип обработки: "+workInfoResponse?.workTypeName.toString()
                binding.culture.text = "Культура: "+workInfoResponse?.cultureName.toString()
                binding.technic.text = "Техника: "+workInfoResponse?.technicName.toString()
                binding.workId.text = "ID: "+workInfoResponse?.workId.toString()
                binding.creator.text = "Оператор: "+workInfoResponse?.creatorName.toString()
                binding.workname.text = "Обработка: "+workInfoResponse?.name.toString()
                binding.starttime.text = "Начало обработки: "+ workInfoResponse?.startTime.toString()

                if(workInfoResponse?.endTime.isNullOrBlank()){
                    binding.endtime.text = "Конец обработки: не закончена"
                }
                else{
                    binding.endtime.text = "Конец обработки: "+ workInfoResponse?.endTime.toString()
                    if(workInfoResponse?.fuel!=null){
                        binding.fuel.isVisible=true
                        binding.fuel.text = "Топливо, л: "+ workInfoResponse?.fuel.toString()
                    }
                    if(!workInfoResponse?.secondParameterName.isNullOrBlank() && workInfoResponse?.secondParameterValue != null){
                        binding.secondParameter.isVisible=true
                        binding.secondParameter.text = workInfoResponse?.secondParameterName.toString()+": "+ workInfoResponse?.secondParameterValue.toString()
                    }
                }
                pointTimes= workInfoResponse?.points?.map{it.pointTime.orEmpty()}.orEmpty()
                lats=workInfoResponse?.points?.map{it.lat ?: 0.0}.orEmpty()
                lons=workInfoResponse?.points?.map{it.lon ?: 0.0}.orEmpty()
                pointTimes?.forEachIndexed { index, it ->

                        println(it.toString()+" "+ lats?.get(index).toString()+" "+ lons?.get(index).toString())
                    }





                val points: MutableList<IGeoPoint> = ArrayList()
                val len = pointTimes.size-1
                for (i in 0..len) {
                    points.add(
                        LabelledGeoPoint(
                            lats[i], lons[i], pointTimes[i]
                        )

                    )
                }
                val pt = SimplePointTheme(points, true)

                val textStyle = Paint()
                textStyle.style = Paint.Style.FILL
                textStyle.setColor(Color.parseColor("#0000ff"))
                textStyle.textAlign = Paint.Align.CENTER
                textStyle.textSize = 24f

                val opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                    .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                    .setRadius(7f).setIsClickable(true).setCellSize(15).setTextStyle(textStyle)

                val sfpo = SimpleFastPointOverlay(pt, opt)

                sfpo.setOnClickListener { points, point ->
                    Toast.makeText(
                        binding.mapview.getContext(),
                        "You clicked " + (points[point] as LabelledGeoPoint).label,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //println(lats[0].toString()+" "+lons[0].toString())
//        println("lats: "+lats.toString())
                binding.mapview.getOverlays().add(sfpo)
                binding.mapview.controller.setCenter(LabelledGeoPoint(
                    lats[0], lons[0], pointTimes[0]
                ))
//                        val lat = lats?.get(index)
//                        val lon = lons?.get(index)
//                        val geo = GeoPoint(lats?.get(index)!!, lons?.get(index)!!)
//                        binding.mapview.controller.setCenter(geo)
//                        val marker = Marker(binding.mapview)
//                        marker.setTitle(lats.get(index).toString() + ",\n" + lons.get(index).toString())
//                        marker.setPosition(geo)
//                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)


//                }

            }
        }
    }

//    private fun getPointlist(workId: Int) {
//        CoroutineScope(Dispatchers.Main).launch {
//            flow{
//                val pointListResponse = api?.getPointlist(workId)
//                emit(pointListResponse)
//            }.catch { e ->
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }.collect { pointListResponse ->
//                val pointTimes=pointListResponse?.map{it.pointTime}
//                val lats=pointListResponse?.map{it.lat}
//                val lons=pointListResponse?.map{it.lon}
//                pointTimes?.forEachIndexed { index, it ->
//                    println(it.toString()+" "+ lats?.get(index).toString()+" "+ lons?.get(index).toString())
//
//                }
//            }
//        }
//    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}