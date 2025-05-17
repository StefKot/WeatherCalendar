package com.bas.weathercalendar.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar // Добавьте ProgressBar в ваш fragment_register.xml
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Для by viewModels()
import com.bas.weathercalendar.R
import com.bas.weathercalendar.network.model.UserCreate

class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModels()

    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordAgain: EditText
    private lateinit var buttonRegisterAction: Button
    private lateinit var buttonBack: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextLogin = view.findViewById(R.id.editText_registerLogin)
        editTextPassword = view.findViewById(R.id.editText_registerPassword)
        editTextPasswordAgain = view.findViewById(R.id.editText_registerPasswordAgain)
        buttonRegisterAction = view.findViewById(R.id.button_register_action)
        buttonBack = view.findViewById(R.id.button_back)
        progressBar = view.findViewById(R.id.progressBarRegister)
        progressBar.visibility = View.GONE // Скрыть по умолчанию


        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonRegisterAction.setOnClickListener {
            val login = editTextLogin.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val passwordAgain = editTextPasswordAgain.text.toString().trim()

            if (login.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
                showErrorAlert("Все поля должны быть заполнены.")
                return@setOnClickListener
            }
            if (password.length < 6) { // Проверка из OpenAPI
                showErrorAlert("Пароль должен содержать не менее 6 символов.")
                return@setOnClickListener
            }
            if (password != passwordAgain) {
                showErrorAlert("Пароли не совпадают.")
                return@setOnClickListener
            }

            val userCreate = UserCreate(username = login, password = password)
            registerViewModel.registerUser(userCreate)
        }

        registerViewModel.registrationStatus.observe(viewLifecycleOwner) { result ->
            progressBar.isVisible = result is RegistrationResult.Loading
            when (result) {
                is RegistrationResult.Success -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Успех")
                        .setMessage(result.message)
                        .setPositiveButton("ОК") { dialog, _ ->
                            dialog.dismiss()
                            parentFragmentManager.popBackStack() // Возвращаемся на экран входа
                        }
                        .show()
                }
                is RegistrationResult.Error -> {
                    showErrorAlert(result.errorMessage)
                }
                is RegistrationResult.Loading -> {
                    buttonRegisterAction.isEnabled = false
                    progressBar.visibility = View.VISIBLE // Показываем прогресс бар
                }
            }
            if (result !is RegistrationResult.Loading) buttonRegisterAction.isEnabled = true
        }
    }

    private fun showErrorAlert(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
}