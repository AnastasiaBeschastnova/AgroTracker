package com.example.agrotracker.operator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.databinding.FragmentStartWorkFormBinding
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class StartWorkFormFragment : Fragment() {

    private var _binding: FragmentStartWorkFormBinding? = null
    private val args: StartWorkFormFragmentArgs by navArgs()
    private val viewModel: StartWorkFormViewModel by viewModels()
    var startTime: String = ""
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiData.collect {
                    when (it) {
                        is StartWorkFormViewModel.Data.FieldsLists -> {
                            createAdapters(it)
                        }

                        is StartWorkFormViewModel.Data.IsCulturesVisible -> {
                            binding.culture.isVisible = it.isVisible
                            binding.cultureSpinner.isVisible = it.isVisible
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
                        is StartWorkFormViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                        is StartWorkFormViewModel.Actions.NavigateToContinueWorkFragment ->{
                            findNavController().navigate(
                                StartWorkFormFragmentDirections
                                    .actionStartWorkFormFragmentToContinueWorkFragment(
                                        args.creatorId,
                                        it.startTime,
                                        it.workTypeId,
                                    ))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStartWorkFormBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getStartForm()

        binding.worktypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.selectWorkType(binding.worktypeSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }


        binding.buttonStart.setOnClickListener {
            viewModel.sendWork(
                worktype = binding.worktypeSpinner.selectedItem.toString(),
                field = binding.fieldSpinner.selectedItem.toString(),
                culture = binding.cultureSpinner.selectedItem.toString(),
                technic = binding.technicSpinner.selectedItem.toString(),
                creatorId = args.creatorId,
                workname = binding.worknameInputEditText.text.toString(),
            )
        }
    }

    private fun createAdapters(fields: StartWorkFormViewModel.Data.FieldsLists) {
        val worktypesSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), R.layout.item_spinner, fields.worktypesNames.orEmpty().toTypedArray()
        )
        worktypesSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
        binding.worktypeSpinner.setAdapter(worktypesSpinnerArrayAdapter)


        val fieldSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), R.layout.item_spinner, fields.fieldsNames.orEmpty().toTypedArray()
        )
        fieldSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
        binding.fieldSpinner.setAdapter(fieldSpinnerArrayAdapter)


        val technicSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), R.layout.item_spinner, fields.technicsNames.orEmpty().toTypedArray()
        )
        technicSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
        binding.technicSpinner.setAdapter(technicSpinnerArrayAdapter)


        val cultureSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), R.layout.item_spinner, fields.culturesNames.orEmpty().toTypedArray()
        )
        cultureSpinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner)
        binding.cultureSpinner.setAdapter(cultureSpinnerArrayAdapter)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}