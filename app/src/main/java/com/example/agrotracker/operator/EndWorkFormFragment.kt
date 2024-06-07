package com.example.agrotracker.operator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.databinding.FragmentEndWorkFormBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class EndWorkFormFragment : Fragment() {

    private var _binding: FragmentEndWorkFormBinding? = null
    private val args: EndWorkFormFragmentArgs by navArgs()
    private val viewModel: EndWorkFormViewModel by viewModels()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initData(args.workTypeId, args.workId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect {
                    when (it) {
                        is EndWorkFormViewModel.Data.Fields-> {
                            //отображать только те поля для ввода параметров по окончании полевой работы, которые соответствуют типу выполненной полевой работы
                            binding.seedsTextInputLayout.isVisible=it.seedsIsVisible
                            binding.seeds.isVisible=it.seedsIsVisible
                            binding.fertilizerTextInputLayout.isVisible=it.fertilizerIsVisible
                            binding.fertilizer.isVisible=it.fertilizerIsVisible
                            binding.harvestTextInputLayout.isVisible=it.harvestIsVisible
                            binding.harvest.isVisible=it.harvestIsVisible
                            binding.waterTextInputLayout.isVisible=it.waterIsVisible
                            binding.water.isVisible=it.waterIsVisible
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
                        is EndWorkFormViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        is EndWorkFormViewModel.Actions.NavigateToStartWorkFormFragment -> {
                            findNavController().navigate(
                                EndWorkFormFragmentDirections.actionEndWorkFormFragmentToStartWorkFormFragment(args.creatorId)
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

        _binding = FragmentEndWorkFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSend.setOnClickListener {
            //отправка параметров, вводимых по окончании выполнения полевой работы
            viewModel.sendWorkParameterValues(
                fuel = binding.fuelInputEditText.text.toString().toIntOrNull(),
                water = binding.waterInputEditText.text.toString().toIntOrNull(),
                fertilizer = binding.fertilizerInputEditText.text.toString().toIntOrNull(),
                harvest = binding.harvestInputEditText.text.toString().toIntOrNull(),
                seeds = binding.seedsInputEditText.text.toString().toIntOrNull(),
            )
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}