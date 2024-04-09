package com.example.agrotracker.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.converters.toWorklistItemModel
import com.example.agrotracker.databinding.FragmentWorklistBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class WorklistFragment : Fragment() {

    private var _binding: FragmentWorklistBinding? = null
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWorklistBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        get_worklist()
        Toast.makeText(requireContext(), "Список работ загружается.", Toast.LENGTH_SHORT).show()



    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun get_worklist() {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val worksResponse = api?.getWorklist()
                emit(worksResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { worksResponse ->
                binding.adminRecycler.adapter = WorklistAdapter(
                    dataSet = worksResponse?.map { it.toWorklistItemModel() }.orEmpty().toTypedArray(),
                    onItemClicked = { item ->
                        findNavController().navigate(
                            WorklistFragmentDirections.actionAdminSecondFragmentToAdminThirdFragment(item)
                        )
                    })
            }
        }
    }


}