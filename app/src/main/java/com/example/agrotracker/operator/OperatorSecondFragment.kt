package com.example.agrotracker.operator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agrotracker.R
import com.example.agrotracker.admin.AdminThirdFragmentArgs
import com.example.agrotracker.api.NetworkService
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.requests.UpdateWorkRequest
import com.example.agrotracker.databinding.FragmentOperatorSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OperatorSecondFragment : Fragment() {

    private var _binding: FragmentOperatorSecondBinding? = null
    private val args: OperatorSecondFragmentArgs by navArgs()
    private val api by lazy{ NetworkService.instance?.agroTrackerApi}
    var startTime: String = ""
    var workTypeId: Int = 0

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

            if (binding.worktypeInputEditText.text.toString() == "2") {//2=посев
                binding.culture.isVisible = true
                binding.cultureTextInputLayout.isVisible = true
            } else {
                //очистить поле и сделать его невидимым
                binding.culture.isVisible = false
                binding.cultureTextInputLayout.isVisible = false

            }
        }
        binding.cultureInputEditText.doAfterTextChanged {

        }
        binding.buttonStart.setOnClickListener {
            startTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").format(Date())
            workTypeId=binding.worktypeInputEditText.text.toString().toInt()
            if(binding.worktypeInputEditText.text.toString() != "2"){//не посев- культуры нет - id=0
                insertWork(
                    0,
                    binding.technicInputEditText.text.toString().toInt(),
                    binding.fieldInputEditText.text.toString().toInt(),
                    binding.worktypeInputEditText.text.toString().toInt(),
                    args.creatorId,
                    binding.worknameInputEditText.text.toString(),
                    startTime
                )
            }
            else{
                insertWork(
                    binding.cultureInputEditText.text.toString().toInt(),
                    binding.technicInputEditText.text.toString().toInt(),
                    binding.fieldInputEditText.text.toString().toInt(),
                    binding.worktypeInputEditText.text.toString().toInt(),
                    args.creatorId,
                    binding.worknameInputEditText.text.toString(),
                    startTime
                )
            }
        }
    }


    private fun insertWork(cultureId: Int, technicId: Int,fieldId: Int,
                           workTypeId: Int, creatorId: Int, name:String,startTime: String) {
        CoroutineScope(Dispatchers.Main).launch {
            flow{
                val insertWorkResponse = api?.insertWork(
                    InsertWorkRequest(cultureId,technicId,fieldId,workTypeId,creatorId,name,startTime)
                )
                emit(insertWorkResponse)
            }.catch { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }.collect { insertWorkResponse ->
                findNavController().navigate(
                    OperatorSecondFragmentDirections
                        .actionOperatorSecondFragmentToOperatorThirdFragment(args.creatorId,startTime,workTypeId)
                )
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}