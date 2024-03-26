package com.example.agrotracker.admin

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.agrotracker.databinding.FragmentAdminThirdBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AdminThirdFragment : Fragment() {

    private var _binding: FragmentAdminThirdBinding? = null
    private val args: AdminThirdFragmentArgs by navArgs()
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}

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




        _binding = FragmentAdminThirdBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workInfo(args.work.workId)
//        binding.field.text = "Поле (номер): "+args.work.fieldName
//        binding.worktype.text = "Тип обработки: "+args.work.workType
//        binding.culture.text = "Культура: "+args.work.culture
//        binding.technic.text = "Техника: "+args.work.technic
//        binding.workId.text = "ID: "+args.work.workId
////        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
////        val date: String = simpleDateFormat.format(args.work.startTime)
//
//        binding.starttime.text = "Начало обработки: "+ args.work.startTime

        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminThirdFragment_to_adminSecondFragment)
        }

        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        //binding.mapview.setBuiltInZoomControls(true)
        val geo = GeoPoint(48.7070, 44.5169)//Волгоград
        binding.mapview.controller.setCenter(geo)
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
                    println("${location?.latitude} ${location?.longitude}")
                    //geo = GeoPoint(location?.latitude.toDouble(), location?.longitude)
                    if(location?.latitude != null && location?.longitude!=null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        val geo = GeoPoint(lat, lon)
                        binding.mapview.controller.setCenter(geo)
                        val startMarker = Marker(binding.mapview)
                        startMarker.setTitle(lat.toString()+",\n"+lon.toString())
                        startMarker.setPosition(geo)
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        binding.mapview.getOverlays().add(startMarker)
                        //startMarker.infoWindow.

                    }
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
//                val message = when(e){
//                    is retrofit2.HttpException -> {
//                        when(e.code()){
//                            404 -> "Неверные логин или пароль"
//                            else -> "Ошибка сервера"
//                        }
//                    }
//                    else -> "Внутренняя ошибка, ${e.message}"
//                }
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { workInfoResponse ->
                binding.field.text = "Поле: "+workInfoResponse?.fieldName.toString()
                binding.worktype.text = "Тип обработки: "+workInfoResponse?.workTypeName.toString()
                binding.culture.text = "Культура: "+workInfoResponse?.cultureName.toString()
                binding.technic.text = "Техника: "+workInfoResponse?.technicName.toString()
                binding.workId.text = "ID: "+workInfoResponse?.workId.toString()
                binding.creator.text = "Оператор: "+workInfoResponse?.creatorName.toString()
                binding.workname.text = "Обработка: "+workInfoResponse?.workId.toString()
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

//                if (loginResponse?.role == "Оператор") {
//                    findNavController().navigate(R.id.action_FirstFragment_to_operatorSecondFragment)
//                } else if (loginResponse?.role == "Администратор") {
//                    findNavController().navigate(R.id.action_FirstFragment_to_adminSecondFragment)
//                } //else {
//                    Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_LONG).show()
//                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}