package com.bas.weathercalendar.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bas.weathercalendar.MainActivity
import com.bas.weathercalendar.R
import com.bas.weathercalendar.network.model.UserLogin
import com.bas.weathercalendar.register.RegisterFragment
import com.bas.weathercalendar.register.ResetPassFragment

class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignIn: Button
    private lateinit var buttonForgotPassword: Button
    private lateinit var buttonRegister: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextLogin = view.findViewById(R.id.editTextLogin)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonSignIn = view.findViewById(R.id.button_sign_in)
        buttonForgotPassword = view.findViewById(R.id.button_forgot_password)
        buttonRegister = view.findViewById(R.id.button_register)
        progressBar = view.findViewById(R.id.progressBarLogin)
        progressBar.visibility = View.GONE // Скрыть по умолчанию

        buttonRegister.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_auth, RegisterFragment())
                addToBackStack(null)
                commit()
            }
        }

        buttonForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_auth, ResetPassFragment())
                addToBackStack(null)
                commit()
            }
        }

        buttonSignIn.setOnClickListener {
            val login = editTextLogin.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                showErrorAlert("Логин и пароль не могут быть пустыми.")
                return@setOnClickListener
            }
            val userLogin = UserLogin(username = login, password = password)
            loginViewModel.loginUser(userLogin)
        }

        loginViewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginResult.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Вход успешен!", Toast.LENGTH_SHORT).show()
                    // Переход на MainActivity
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Очищаем стек активностей
                    startActivity(intent)
                    activity?.finish() // Закрываем AuthActivity
                    buttonSignIn.isEnabled = true
                }
                is LoginResult.Error -> {
                    progressBar.visibility = View.GONE
                    showErrorAlert(result.errorMessage)
                    buttonSignIn.isEnabled = true
                }
                is LoginResult.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    buttonSignIn.isEnabled = false
                }
            }
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