package com.example.agrotracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //временная псевдоавторизация
        //if (binding.loginInputEditText.text.toString() == "ivanov" && binding.passwordInputEditText.text.toString() == "ivanov") {
        binding.buttonLogIn.setOnClickListener {
            if (binding.loginInputEditText.text.toString() == "ivanov" && binding.passwordInputEditText.text.toString() == "ivanov") {
                findNavController().navigate(R.id.action_FirstFragment_to_operatorSecondFragment)
            } else if (binding.loginInputEditText.text.toString() == "petrov" && binding.passwordInputEditText.text.toString() == "petrov") {
                findNavController().navigate(R.id.action_FirstFragment_to_adminSecondFragment)}
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}