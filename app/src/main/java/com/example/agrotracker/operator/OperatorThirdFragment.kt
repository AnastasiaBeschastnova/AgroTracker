package com.example.agrotracker.operator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.R
import com.example.agrotracker.databinding.FragmentOperatorThirdBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OperatorThirdFragment : Fragment() {

    private var _binding: FragmentOperatorThirdBinding? = null
    private val fusedLocationClient: FusedLocationProviderClient by lazy{
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOperatorThirdBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonEnd.setOnClickListener {
            findNavController().navigate(R.id.action_operatorThirdFragment_to_operatorFourthFragment)
        }


        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        val time: String = simpleDateFormat.format(Date())
        binding.time.text = "Вы в пути: " + time
        var geo: String
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
                    geo = "${location?.latitude}, ${location?.longitude}"
                    binding.geoText.text = "Местоположение:\n"+geo
                }
        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}