package com.example.mysit708app.activities

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mysit708app.R
import com.example.mysit708app.models.User
import com.example.mysit708app.utils.Constants
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.util.jar.Manifest

class UserProfileActivity : BaseActivity(),View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        var userDetails: User = User()
        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            userDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        val et_first_name : EditText = findViewById(R.id.et_first_name)
        val et_last_name : EditText = findViewById(R.id.et_last_name)
        val et_email : EditText = findViewById(R.id.et_email)

        et_first_name.isEnabled = false
        et_first_name.setText(userDetails.firstName)

        et_last_name.isEnabled = false
        et_last_name.setText(userDetails.lastName)

        et_email.isEnabled = false
        et_email.setText(userDetails.email)

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
    }

    override fun onClick(v: View?) {
        if(v != null){
            when(v.id){
                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        showErrorSnackBar("You Already Have The Storage Permission", false)
                    }else{
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showErrorSnackBar("The storage permission is granted.", false)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}