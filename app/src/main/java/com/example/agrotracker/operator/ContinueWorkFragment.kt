package com.example.agrotracker.operator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertPointRequest
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.requests.UpdateWorkRequest
import com.example.agrotracker.databinding.FragmentContinueWorkBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ContinueWorkFragment : Fragment() {

    private var workId: Int ?= null
    private var _binding: FragmentContinueWorkBinding? = null
    private val args: ContinueWorkFragmentArgs by navArgs()
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}
    private val geoVlg = GeoPoint(48.7070, 44.5169)//Волгоград
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentContinueWorkBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectWorkId(args.creatorId,args.startTime)
        binding.buttonEnd.setOnClickListener {
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
            val values = binding.workId.text.split(": ")
            val workId = values[1].toInt()
            updateWork(workId,endTime)
        }

        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(17.0)

        listenLocation()
        startTimer()
        Toast.makeText(requireContext(), "Карта загружается. Подождите", Toast.LENGTH_LONG).show()

    }


    private fun selectWorkId(creatorId: Int, startTime: String) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val selectWorkIdResponse = api?.selectWorkId(creatorId,startTime)
                emit(selectWorkIdResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { selectWorkIdResponse ->
                if(selectWorkIdResponse?.work_id!=null)
                {
                    workId=selectWorkIdResponse.work_id
                    binding.workId.text = "ID работы: "+workId.toString()
                }
            }
        }
    }

    private fun updateWork(workId: Int, endTime: String) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val updateWorkResponse = api?.updateWork(
                    UpdateWorkRequest(workId,endTime)
                )
                emit(updateWorkResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { updateWorkResponse ->
                findNavController().navigate(
                    ContinueWorkFragmentDirections
                        .actionOperatorThirdFragmentToOperatorFourthFragment(workId,endTime,args.workTypeId)
                )
            }
        }
    }

    private fun listenLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdateDelayMillis(1000)
                    .build(),
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        p0 ?: return
                        for (location in p0.locations) {
                            if (location != null && (lifecycle.currentState == Lifecycle.State.STARTED || lifecycle.currentState == Lifecycle.State.RESUMED)) {
                                val startPoint = GeoPoint(location.latitude, location.longitude)
                                val startMarker = Marker(binding.mapview)
                                startMarker.setPosition(startPoint)
                                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                binding.mapview.getOverlays().clear()
                                binding.mapview.getOverlays().add(startMarker)
                                binding.mapview.controller.setCenter(startPoint)
                                binding.loadingText.isVisible=false
                                binding.geo.isVisible=true
                                binding.mapview.isVisible=true
                                binding.workId.isVisible=true
                                binding.workTime.isVisible=true
                                binding.geo.text ="Местоположение: \n"+location.latitude.toString()+" (ш.),\n"+location.longitude.toString()+" (д.)"
                                val point_time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date()).toString()
                                workId?.let {
                                    insertPoint(it, location.latitude, location.longitude, point_time)
                                }

                            }
                            else if(location == null && (lifecycle.currentState == Lifecycle.State.STARTED || lifecycle.currentState == Lifecycle.State.RESUMED)){
                                val boundingBox = BoundingBox(geoVlg.altitude*1.01, geoVlg.longitude*1.01, geoVlg.altitude*0.99, geoVlg.longitude*0.99)
                                binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(0.05f), false)
                                binding.mapview.controller.setCenter(geoVlg)
                                binding.geo.text ="Местоположение: не распознано"
                            }
                        }
                    }
                },
                Looper.getMainLooper()
                )
        }
    }

    private fun insertPoint(
        workId: Int, lat: Double, lon: Double, pointTime: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val insertPointResponse = api?.insertPoint(
                    InsertPointRequest(
                        workId,
                        lat,
                        lon,
                        pointTime
                    )
                )
                emit(insertPointResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { insertPointResponse ->
                println(insertPointResponse)
            }
        }
    }

    private fun startTimer() = CoroutineScope(Dispatchers.Main).launch{
        while(lifecycle.currentState == Lifecycle.State.RESUMED){

            val nowTime=Date()
            val startTime=SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(args.startTime)
            //разница в миллисекундах - перевод в часы, минуты, секунды, миллисекунды
            val pathTime = kotlin.math.abs(nowTime.time - startTime.time)//миллисекунды
            val path = SimpleDateFormat("HH:mm:ss" ).apply {
                timeZone = TimeZone.getTimeZone("GMT+00:00")
            }.format(Date(pathTime))
            binding.workTime.text = "В пути: "+path
            delay(1000)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}