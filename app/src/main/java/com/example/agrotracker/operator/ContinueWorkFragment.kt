package com.example.agrotracker.operator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.databinding.FragmentContinueWorkBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ContinueWorkFragment : Fragment() {

    private var _binding: FragmentContinueWorkBinding? = null
    private val args: ContinueWorkFragmentArgs by navArgs()
    private val viewModel: ContinueWorkViewModel by viewModels()
    private val geoVlg = GeoPoint(48.7070, 44.5169)//Волгоград


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect {
                    when (it) {
                        is ContinueWorkViewModel.Data.UiData -> {
                            setUpUiData(it)
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
                        is ContinueWorkViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        is ContinueWorkViewModel.Actions.NavigateToEndWorkFormFragment -> {
                            findNavController().navigate(
                                ContinueWorkFragmentDirections
                                    .actionContinueWorkFragmentToEndWorkFormFragment(
                                        it.workId, it.endTime, args.workTypeId.toInt(), args.creatorId
                                    )
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

        _binding = FragmentContinueWorkBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectWorkId(args.creatorId, args.startTime)//вывод ID созданной оператором полевой работы
        binding.buttonEnd.setOnClickListener {
            //обновить в базе данных полевую работу - добавить время окончания ее выполнения
            viewModel.sendUpdatedWork(
                workId = binding.workId.text.split(": ")[1].toInt(),
                endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date()),
                workTypeId = args.workTypeId
            )
        }

        listenLocation()//прослушивать геолокацию устройства

        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)//отображать на карте с актуальную геолокацию устройства оператора
        binding.mapview.controller.setZoom(17.0)
        Toast.makeText(requireContext(), "Карта загружается. Подождите", Toast.LENGTH_LONG).show()

    }


    private fun listenLocation() {//прослушивание локации устройства оператора
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdateDelayMillis(1000)
                    .build(),
                object : LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        p0 ?: return
                        for (location in p0.locations) {
                            if (location != null && (lifecycle.currentState == Lifecycle.State.STARTED || lifecycle.currentState == Lifecycle.State.RESUMED)) {
                                viewModel.sendPoint(location)
                            }
                        }
                    }
                },
                Looper.getMainLooper()
            )
        }
    }

    private fun setUpUiData(data: ContinueWorkViewModel.Data.UiData) {
        val location = data.location
        binding.workTime.text = "В пути: " + data.currentTime

        binding.workId.text = "ID работы: " + data.workId.toString()

        if (location != null && (lifecycle.currentState == Lifecycle.State.STARTED || lifecycle.currentState == Lifecycle.State.RESUMED)) {
            //обновлять информацию о геолокации
            val startPoint = GeoPoint(location.latitude, location.longitude)
            val startMarker = Marker(binding.mapview)
            startMarker.setPosition(startPoint)
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            binding.mapview.getOverlays().clear()
            binding.mapview.getOverlays().add(startMarker)
            binding.mapview.controller.setCenter(startPoint)
            binding.loadingText.isVisible = false
            binding.geo.isVisible = true
            binding.mapview.isVisible = true
            binding.workId.isVisible = true
            binding.workTime.isVisible = true
            binding.geo.text =
                "Местоположение: \n" + location.latitude.toString() + " (ш.),\n" + location.longitude.toString() + " (д.)"
        } else if (location == null && (lifecycle.currentState == Lifecycle.State.STARTED || lifecycle.currentState == Lifecycle.State.RESUMED)) {
            //если геолокация не определена, отобразить на карте фрагмент г. Волгограда
            val boundingBox = BoundingBox(
                geoVlg.altitude * 1.01,
                geoVlg.longitude * 1.01,
                geoVlg.altitude * 0.99,
                geoVlg.longitude * 0.99
            )
            binding.mapview.zoomToBoundingBox(boundingBox.increaseByScale(0.05f), false)
            binding.mapview.controller.setCenter(geoVlg)
            binding.geo.text = "Местоположение: не распознано"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}