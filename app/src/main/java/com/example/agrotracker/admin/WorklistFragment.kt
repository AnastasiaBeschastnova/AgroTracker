package com.example.agrotracker.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.converters.toWorklistItemModel
import com.example.agrotracker.databinding.FragmentWorklistBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class WorklistFragment : Fragment() {

    private var _binding: FragmentWorklistBinding? = null
    private val viewModel: WorklistViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect {
                    when (it) {
                        is WorklistViewModel.Data.WorklistResponse -> {
                            createAdapter(it)
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
                        is WorklistViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                        is WorklistViewModel.Actions.NavigateToWorkInfoFragment ->{
                            //навигация на экран с выводом подробной информации о выбранной из списка полевой работе
                            findNavController().navigate(
                               WorklistFragmentDirections
                                    .actionWorklistFragmentToWorkInfoFragment(
                                        it.item
                                    ))
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

        _binding = FragmentWorklistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getWorklist()//список полевых работ
        Toast.makeText(requireContext(), "Список работ загружается.", Toast.LENGTH_SHORT).show()
    }
    private fun createAdapter(dataset: WorklistViewModel.Data.WorklistResponse?) {
        binding.adminRecycler.adapter = WorklistAdapter(
            dataSet =  dataset?.works?.map{it.toWorklistItemModel()}.orEmpty().toTypedArray(),
            onItemClicked = { item ->
                findNavController().navigate(
                    WorklistFragmentDirections.actionWorklistFragmentToWorkInfoFragment(item))

            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


