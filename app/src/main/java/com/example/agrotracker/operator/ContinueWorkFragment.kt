package com.example.agrotracker.operator

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.UpdateWorkRequest
import com.example.agrotracker.databinding.FragmentContinueWorkBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ContinueWorkFragment : Fragment() {

    private var _binding: FragmentContinueWorkBinding? = null
    private val args: ContinueWorkFragmentArgs by navArgs()
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}
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

        _binding = FragmentContinueWorkBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectWorkId(args.creatorId,args.startTime)
        binding.buttonEnd.setOnClickListener {
            val endTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
            val values = binding.workId.text.split(": ")
            val workId = values[1]
            updateWork(workId.toInt(),endTime)
        }


        binding.time.text = "Вы в пути c " +args.startTime
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
                    binding.workId.text = "ID работы: "+selectWorkIdResponse.work_id.toString()
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}