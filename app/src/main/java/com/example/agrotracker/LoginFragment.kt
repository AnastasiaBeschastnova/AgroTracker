package com.example.agrotracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.databinding.FragmentLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val api by lazy{NetworkService.instance?.agroTrackerApi}

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogIn.setOnClickListener {
            login(
                binding.loginInputEditText.text.toString(),
                binding.loginInputEditText.text.toString(),
            )
        }
    }

    private fun login(login: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val loginResponse = api?.login(login, password)
                emit(loginResponse)
            }.catch { e ->
                val message = when(e){
                    is retrofit2.HttpException -> {
                        when(e.code()){
                            404 -> "Неверные логин или пароль"
                            else -> "Ошибка сервера"
                        }
                    }
                    else -> "Внутренняя ошибка, ${e.message}"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }.collect { loginResponse ->
                if (loginResponse?.role == "Оператор") {
                    findNavController().navigate(
                        LoginFragmentDirections.actionFirstFragmentToOperatorSecondFragment(loginResponse.id)
                    )
                } else if (loginResponse?.role == "Администратор") {
                    findNavController().navigate(R.id.action_FirstFragment_to_adminSecondFragment)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}