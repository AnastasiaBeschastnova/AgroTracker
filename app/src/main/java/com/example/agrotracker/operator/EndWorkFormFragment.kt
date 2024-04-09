package com.example.agrotracker.operator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkParameterValuesRequest
import com.example.agrotracker.databinding.FragmentEndWorkFormBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class EndWorkFormFragment : Fragment() {

    private var _binding: FragmentEndWorkFormBinding? = null
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}
    private val args: EndWorkFormFragmentArgs by navArgs()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEndWorkFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(args.workTypeId){
            1 -> {//обработка почвы
                //активна только строка для топлива
            }
            2 -> {//посев
                binding.seedsTextInputLayout.isVisible=true
                binding.seeds.isVisible=true
            }
            3 -> {//внесение удобрений
                binding.fertilizerTextInputLayout.isVisible=true
                binding.fertilizer.isVisible=true
            }
            4 -> {//уборка урожая
                binding.harvestTextInputLayout.isVisible=true
                binding.harvest.isVisible=true
            }
            5 -> {//полив
                binding.waterTextInputLayout.isVisible=true
                binding.water.isVisible=true
            }
        }

        binding.buttonSend.setOnClickListener {
            when(args.workTypeId){
                1 -> {//обработка почвы
                    insertWorkParameterValues(args.workId,binding.fuelInputEditText.text.toString().toInt(),0)
                }
                2 -> {//посев
                    insertWorkParameterValues(args.workId,binding.fuelInputEditText.text.toString().toInt(),binding.seedsInputEditText.text.toString().toInt())
                }
                3 -> {//внесение удобрений
                    insertWorkParameterValues(args.workId,binding.fuelInputEditText.text.toString().toInt(),binding.fertilizerInputEditText.text.toString().toInt())
                }
                4 -> {//уборка урожая
                    insertWorkParameterValues(args.workId,binding.fuelInputEditText.text.toString().toInt(),binding.harvestInputEditText.text.toString().toInt())
                }
                5 -> {//полив
                    insertWorkParameterValues(args.workId,binding.fuelInputEditText.text.toString().toInt(),binding.waterInputEditText.text.toString().toInt())
                }
            }

        }


    }
    private fun insertWorkParameterValues(workId: Int, fuel: Int,secondParameterValue: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val insertWorkParameterValuesResponse = api?.insertWorkParameterValues(
                    InsertWorkParameterValuesRequest(workId,fuel,secondParameterValue)
                )
                emit(insertWorkParameterValuesResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { insertWorkParameterValuesResponse ->
                findNavController().navigate(R.id.action_operatorFourthFragment_to_FirstFragment)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}