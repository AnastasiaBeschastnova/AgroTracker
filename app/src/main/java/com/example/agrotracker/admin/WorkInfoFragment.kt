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
//    private var pointTimes: List<String> = listOf()
//    private var lats: List<Double> = listOf()
//    private var lons: List<Double> = listOf()

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




        _binding = FragmentWorkInfoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pointId.isVisible=false
        binding.coords.isVisible=false
        binding.pointTime.isVisible=false
        workInfo(args.work.workId)
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminThirdFragment_to_adminSecondFragment)
        }

        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)






        //binding.mapview.setBuiltInZoomControls(true)
        val geo = GeoPoint(48.7070, 44.5169)//Волгоград

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

                }
        }

        binding.mapview.controller.setZoom(17.0)
        binding.mapview.controller.setCenter(geo)


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
                val pointTimes= workInfoResponse?.points?.map{it.pointTime.orEmpty()}.orEmpty()
                val lats=workInfoResponse?.points?.map{it.lat ?: 0.0}.orEmpty()
                val lons=workInfoResponse?.points?.map{it.lon ?: 0.0}.orEmpty()
                val pointIds=workInfoResponse?.points?.map{it.id ?: 0}.orEmpty()





                val points: MutableList<IGeoPoint> = ArrayList()
                val len = pointTimes.size-1
                for (i in 0..len) {
                    points.add(
                        LabelledGeoPoint(
                            lats[i], lons[i], "№"+(i+1).toString()
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
                    binding.pointId.isVisible=true
                    binding.coords.isVisible=true
                    binding.pointTime.isVisible=true
                    binding.pointId.text="ID точки №"+(point+1).toString()+": "+pointIds[point]
                    binding.coords.text="Координаты: "+lats[point].toString()+" (ш.), "+lons[point].toString()+" (д.)"
                    binding.pointTime.text="Время: "+pointTimes[point]
                }
                binding.mapview.getOverlays().add(sfpo)
                binding.mapview.controller.setCenter(LabelledGeoPoint(
                    lats[0], lons[0], "№1"
                ))
                binding.mapview.controller.setZoom(13.0)
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}