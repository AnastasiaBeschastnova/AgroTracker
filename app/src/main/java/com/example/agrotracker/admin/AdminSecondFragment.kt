package com.example.agrotracker.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.databinding.FragmentAdminSecondBinding
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AdminSecondFragment : Fragment() {

    private var _binding: FragmentAdminSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.adminRecycler.adapter = WorklistAdapter(
            dataSet = list,
            onItemClicked = { item ->
                findNavController().navigate(
                    AdminSecondFragmentDirections.actionAdminSecondFragmentToAdminThirdFragment(item)
                )
            })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val list = arrayOf(
        WorklistItemModel("поле1", "посев", "чеснок", "трактор", "60", Date(), Date()),
        WorklistItemModel("поле2", "культивирование", "огурец", "трактор", "59", Date(), Date()),
        WorklistItemModel("поле3", "полив", "кукуруза", "трактор", "77", Date(), Date()),
        WorklistItemModel("поле4", "посев", "чеснок", "трактор", "60", Date(), Date()),
        WorklistItemModel("поле5", "культивирование", "огурец", "трактор", "59", Date(), Date()),
        WorklistItemModel("поле6", "полив", "кукуруза", "трактор", "77", Date(), Date()),
        WorklistItemModel("поле7", "посев", "чеснок", "трактор", "60", Date(), Date()),
        WorklistItemModel("поле8", "культивирование", "огурец", "трактор", "59", Date(), Date()),
        WorklistItemModel("поле9", "полив", "кукуруза", "трактор", "77", Date(), Date()),
    )
}