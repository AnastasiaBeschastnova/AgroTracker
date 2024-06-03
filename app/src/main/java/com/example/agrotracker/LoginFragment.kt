package com.example.agrotracker

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
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.databinding.FragmentLoginBinding
import com.example.agrotracker.localdata.AgroTrackerPreferences
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private val preferences by lazy{ AgroTrackerPreferences(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setPreferences(preferences)
        //viewModel.checkToken()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiAction.collect {
                    when(it){
                        is LoginViewModel.Actions.ToStartWork -> {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToStartWorkFormFragment(it.id)
                            )
                        }
                        is LoginViewModel.Actions.ToContinueWork -> {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToContinueWorkFragment(
                                        it.creatorId,
                                        it.startTime,
                                        it.workTypeId)
                            )
                        }
                        is LoginViewModel.Actions.ToEndWork -> {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToEndWorkFormFragment(
                                        it.workId,
                                        it.startTime,
                                        it.workTypeId,
                                        it.creatorId)
                            )
                        }
                        is LoginViewModel.Actions.ToAdmin -> {
                            findNavController().navigate(
                                LoginFragmentDirections
                                    .actionLoginFragmentToWorklistFragment()
                            )
                        }
                        is LoginViewModel.Actions.ShowToast -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginVisibility.collect{
                    binding.helloText.text="Добро пожаловать!"
                    binding.autorizeCard.isVisible=it
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        viewModel.checkToken()
        binding.loginInputEditText.text = null
        binding.passwordInputEditText.text = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogIn.setOnClickListener {
            viewModel.login(
                binding.loginInputEditText.text.toString(),
                binding.passwordInputEditText.text.toString(),
            )
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}