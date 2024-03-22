package com.example.agrotracker.operator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.R
import com.example.agrotracker.databinding.FragmentOperatorSecondBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OperatorSecondFragment : Fragment() {

    private var _binding: FragmentOperatorSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOperatorSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //пробная проверка отображения дополнительных параметров в зависимости от типа
        binding.worktypeInputEditText.doAfterTextChanged {


            if (binding.worktypeInputEditText.text.toString() == "посев") {
                binding.culture.isVisible = true
                binding.cultureTextInputLayout.isVisible = true
                //binding.cultureInputEditText.isVisible = true
                //println("if")
            } else {
                //очистить поле и сделать его невидимым
                binding.culture.isVisible = false
                binding.cultureTextInputLayout.isVisible = false
                //binding.cultureInputEditText.isVisible = false
                //binding.cultureInputEditText.setText("")
                //println("else")

            }

            //println(binding.worktypeInputEditText.text.toString())
            //print(binding.cultureInputEditText.text.toString())
        }
        binding.cultureInputEditText.doAfterTextChanged {
            //println(binding.cultureInputEditText.text.toString())

        }
        binding.buttonStart.setOnClickListener {
            findNavController().navigate(R.id.action_operatorSecondFragment_to_operatorThirdFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}