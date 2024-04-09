package com.example.agrotracker.operator

import android.R as AR
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.collection.arrayMapOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.admin.WorklistAdapter
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.responses.CulturesResponse
import com.example.agrotracker.api.responses.FieldsResponse
import com.example.agrotracker.api.responses.TechnicsResponse
import com.example.agrotracker.api.responses.WorktypesResponse
import com.example.agrotracker.api.utils.getCIdByName
import com.example.agrotracker.api.utils.getFIdByName
import com.example.agrotracker.api.utils.getTIdByName
import com.example.agrotracker.api.utils.getWIdByName
import com.example.agrotracker.converters.toWorklistItemModel
import com.example.agrotracker.databinding.FragmentStartWorkFormBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Date


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StartWorkFormFragment : Fragment() {

    private var _binding: FragmentStartWorkFormBinding? = null
    private val args: StartWorkFormFragmentArgs by navArgs()
    private val api by lazy { NetworkService.instance?.agroTrackerApi }
    var startTime: String = ""
   // var workTypeId: Int = 0


    private var workTypes: List<WorktypesResponse>? = null
    private var fields: List<FieldsResponse>? = null
    private var technics: List<TechnicsResponse>? = null
    private var cultures: List<CulturesResponse>? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStartWorkFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        get_start_form()
//        get_worktypes()
//        get_fields()
//        get_technics()
//        get_cultures()

        binding.worktypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val worktype_id =
                    workTypes?.getWIdByName(binding.worktypeSpinner.selectedItem.toString())

                if (worktype_id?.toInt() == 2) {
                    binding.culture.isVisible = true
                    binding.cultureSpinner.isVisible = true
                } else {
                    //очистить поле и сделать его невидимым
                    binding.culture.isVisible = false
                    binding.cultureSpinner.isVisible = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                val worktype_id = workTypes?.getWIdByName(binding.worktypeSpinner.selectedItem.toString())
            }
        }


        binding.buttonStart.setOnClickListener {
            startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
            val worktype_id = workTypes?.getWIdByName(binding.worktypeSpinner.selectedItem.toString())
            val field_id = fields?.getFIdByName(binding.fieldSpinner.selectedItem.toString())
            val technic_id = technics?.getTIdByName(binding.technicSpinner.selectedItem.toString())
            if(binding.worknameInputEditText.text.toString() != ""){
            if (worktype_id != 2) {
                insertWork(
                    0,
                    technic_id!!.toInt(),
                    field_id!!.toInt(),
                    worktype_id!!.toInt(),
                    args.creatorId,
                    binding.worknameInputEditText.text.toString(),
                    startTime
                )
            } else {
                val culture_id = cultures?.getCIdByName(binding.cultureSpinner.selectedItem.toString())
                insertWork(
                    culture_id!!.toInt(),
                    technic_id!!.toInt(),
                    field_id!!.toInt(),
                    worktype_id.toInt(),
                    args.creatorId,
                    binding.worknameInputEditText.text.toString(),
                    startTime
                )
            }}
            else{
                Toast.makeText(requireContext(), "Введите название обработки", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun insertWork(
        cultureId: Int, technicId: Int, fieldId: Int,
        workTypeId: Int, creatorId: Int, name: String, startTime: String
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val insertWorkResponse = api?.insertWork(
                    InsertWorkRequest(
                        cultureId,
                        technicId,
                        fieldId,
                        workTypeId,
                        creatorId,
                        name,
                        startTime
                    )
                )
                emit(insertWorkResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { insertWorkResponse ->
                findNavController().navigate(
                    StartWorkFormFragmentDirections
                        .actionOperatorSecondFragmentToOperatorThirdFragment(
                            args.creatorId,
                            startTime,
                            workTypeId
                        )
                )
            }
        }
    }

//    private fun get_worktypes() {
//        CoroutineScope(Dispatchers.Main).launch {
//            flow {
//                val worktypesResponse = api?.getWorktypes()
//                emit(worktypesResponse)
//            }.retry(3){ e-> e is HttpException}
//                .catch { e ->
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }.collect { worktypesResponse ->
////                val len = worktypesResponse?.toTypedArray()?.size.toString()
////
//                workTypes = worktypesResponse
//                val worktype_names = worktypesResponse?.map { it.worktypeName }
//
////                val worktype_ids = worktypesResponse?.map{ it.id }
////                val items = arrayOf("")
////                if(len.toInt()>0){
////                    for(i in 1 ..len.toInt()){
////                        items+=worktype_ids[i]+worktype_names[i]
////                }}
//
//                val worktypeSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//                    requireContext(), R.layout.item_spinner, worktype_names.orEmpty().toTypedArray()
//                    //items
//                )
//                worktypeSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
//                binding.worktypeSpinner.setAdapter(worktypeSpinnerArrayAdapter)
//            }
//        }
//    }
//
//
//    private fun get_fields() {
//        CoroutineScope(Dispatchers.Main).launch {
//            flow {
//                val fieldsResponse = api?.getFields()
//                emit(fieldsResponse)
//            }.retry(3){ e-> e is HttpException}
//                .catch { e ->
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }.collect { fieldsResponse ->
//                fields = fieldsResponse
//                val fields_names = fieldsResponse?.map { it.fieldName }
//
//                val fieldSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//                    requireContext(), R.layout.item_spinner, fields_names.orEmpty().toTypedArray()
//                )
//                fieldSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
//                binding.fieldSpinner.setAdapter(fieldSpinnerArrayAdapter)
//            }
//        }
//    }
//
//    private fun get_cultures() {
//        CoroutineScope(Dispatchers.Main).launch {
//            flow {
//                val culturesResponse = api?.getCultures()
//                emit(culturesResponse)
//            }.retry(3){ e-> e is HttpException}
//                .catch { e ->
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }.collect { culturesResponse ->
//                cultures = culturesResponse
//                val cultures_names = culturesResponse?.map { it.cultureName }
//
//                val cultureSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//                    requireContext(), R.layout.item_spinner, cultures_names.orEmpty().toTypedArray()
//                )
//                cultureSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
//                binding.cultureSpinner.setAdapter(cultureSpinnerArrayAdapter)
//            }
//        }
//    }
//
//    private fun get_technics() {
//        CoroutineScope(Dispatchers.Main).launch {
//            flow {
//                val technicsResponse = api?.getTechnics()
//                emit(technicsResponse)
//            }.retry(3)
//                .catch { e ->
//                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//            }.collect { technicsResponse ->
//                technics = technicsResponse
//                val technics_names = technicsResponse?.map { it.technicName }
//
//                val technicSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//                    requireContext(), R.layout.item_spinner, technics_names.orEmpty().toTypedArray()
//                )
//                technicSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
//                binding.technicSpinner.setAdapter(technicSpinnerArrayAdapter)
//            }
//        }
//    }

    private fun get_start_form() {
        CoroutineScope(Dispatchers.Main).launch {
            flow {
                val startFormResponse = api?.getStartForm()
                emit(startFormResponse)
            }.retry(3)
                .catch { e ->
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                }.collect { startFormResponse ->
                    workTypes = startFormResponse?.workTypes
                    val worktypes_names = workTypes?.map { it.worktypeName }
                    val worktypesSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(), R.layout.item_spinner, worktypes_names.orEmpty().toTypedArray()
                    )
                    worktypesSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
                    binding.worktypeSpinner.setAdapter(worktypesSpinnerArrayAdapter)


                    fields = startFormResponse?.fields
                    val fields_names = fields?.map { it.fieldName }
                    val fieldSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(), R.layout.item_spinner, fields_names.orEmpty().toTypedArray()
                    )
                    fieldSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
                    binding.fieldSpinner.setAdapter(fieldSpinnerArrayAdapter)


                    technics = startFormResponse?.technics
                    val technics_names = technics?.map { it.technicName }
                    val technicSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(), R.layout.item_spinner, technics_names.orEmpty().toTypedArray()
                    )
                    technicSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
                    binding.technicSpinner.setAdapter(technicSpinnerArrayAdapter)


                    cultures = startFormResponse?.cultures
                    val cultures_names = cultures?.map { it.cultureName }
                    val cultureSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(), R.layout.item_spinner, cultures_names.orEmpty().toTypedArray()
                    )
                    cultureSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
                    binding.cultureSpinner.setAdapter(cultureSpinnerArrayAdapter)

                }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}