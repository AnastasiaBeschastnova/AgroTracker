package com.example.agrotracker.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.databinding.FragmentAdminThirdBinding
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AdminThirdFragment : Fragment() {

    private var _binding: FragmentAdminThirdBinding? = null
    private val args: AdminThirdFragmentArgs by navArgs()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminThirdBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.field.text = "Поле (номер): "+args.work.fieldName
        binding.worktype.text = "Тип обработки: "+args.work.workType
        binding.culture.text = "Культура: "+args.work.culture
        binding.technic.text = "Техника: "+args.work.technic
        binding.fuel.text = "Топливо: "+args.work.fuel
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val date: String = simpleDateFormat.format(args.work.startTime)

        binding.worktime.text = "Начало обработки: "+ date

        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminThirdFragment_to_adminSecondFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}