package com.example.mysit708app.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.example.mysit708app.R
import com.example.mysit708app.firestore.FirestoreClass
import com.example.mysit708app.models.User
import com.example.mysit708app.utils.Constants
import com.example.mysit708app.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : BaseActivity(), View.OnClickListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val tv_register: TextView = findViewById(R.id.tv_register)
        val btn_login: Button = findViewById(R.id.btn_login)
        val tv_forgot_password: TextView = findViewById(R.id.tv_forgot_password)

        tv_forgot_password.setOnClickListener(this)

        btn_login.setOnClickListener(this)

        tv_register.setOnClickListener(this)


    }
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.tv_forgot_password -> {
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login -> {
                    logInRegisteredUser()
                }

                R.id.tv_register -> {
                    // Launch the register screen when the user clicks on the text.
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }


    private fun validateLoginDetails(): Boolean {
        val et_email: EditText = findViewById(R.id.et_email)
        val et_password: EditText = findViewById(R.id.et_password)
        return when{
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun logInRegisteredUser(){
        val et_email: EditText = findViewById(R.id.et_email)
        val et_password: EditText = findViewById(R.id.et_password)
        if (validateLoginDetails()){
            showProgressDialog(resources.getString(R.string.please_wait))
            val email = et_email.text.toString().trim{ it <= ' ' }
            val password = et_password.text.toString().trim{ it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        FirestoreClass().getUserDetails(this@LoginActivity)
                    }else{
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
        }
    }

    fun userLoggedInSuccess(user: User) {

        // Hide the progress dialog.
        hideProgressDialog()

        // Print the user details in the log as of now.
        Log.i("First Name: ", user.firstName)
        Log.i("Last Name: ", user.lastName)
        Log.i("Email: ", user.email)

        //check user has completed his/her profile
        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Main Screen after log in.
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }
        finish()
    }
}