package com.example.mysit708app.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mysit708app.R
import com.example.mysit708app.firestore.FirestoreClass
import com.example.mysit708app.models.User
import com.example.mysit708app.utils.Constants
import com.example.mysit708app.utils.GlideLoader
import com.example.mysit708app.utils.MSPButton
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException
import java.util.jar.Manifest

class UserProfileActivity : BaseActivity(),View.OnClickListener {
    private lateinit var mUserDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageURL: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        val et_first_name : EditText = findViewById(R.id.et_first_name)
        val et_last_name : EditText = findViewById(R.id.et_last_name)
        val et_email : EditText = findViewById(R.id.et_email)
        val btn_submit: MSPButton = findViewById(R.id.btn_submit)

        et_first_name.isEnabled = false
        et_first_name.setText(mUserDetails.firstName)

        et_last_name.isEnabled = false
        et_last_name.setText(mUserDetails.lastName)

        et_email.isEnabled = false
        et_email.setText(mUserDetails.email)

        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_submit.setOnClickListener(this@UserProfileActivity)
    }

    override fun onClick(v: View?) {
        if(v != null){
            when(v.id){
                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Constants.showImageChooser(this)
                    }else{
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }
                R.id.btn_submit -> {
                    if (validateUserProfileDetails()){
                        showProgressDialog(resources.getString(R.string.please_wait))
                        if (mSelectedImageFileUri != null){
                            FirestoreClass().uploadImageToCloudStorage(this,mSelectedImageFileUri)
                        }else{
                            updateUserProfileDetails()
                        }
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

                //showErrorSnackBar("The storage permission is granted.", false)
                Constants.showImageChooser(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {
                        // The uri of selected image from phone storage.
                        mSelectedImageFileUri = data.data!!
                        //Using Glide Loader
                        GlideLoader(this).loadUserPicture(mSelectedImageFileUri!!,iv_user_photo)
                        //iv_user_photo.setImageURI(Uri.parse(selectedImageFileUri.toString()))
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UserProfileActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    private fun validateUserProfileDetails(): Boolean{
        return when{
            TextUtils.isEmpty(et_mobile_number.text.toString().trim{ it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun userProfileUpdateSucess(){
        hideProgressDialog()

        Toast.makeText(
            this,
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_SHORT
        ).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun imageUploadSuccess(imageURL: String){
        mUserProfileImageURL = imageURL
        updateUserProfileDetails()
    }

    private fun updateUserProfileDetails(){
        val userHashMap = HashMap<String, Any>()
        val mobileNumber = et_mobile_number.text.toString().trim{ it <= ' ' }
        val gender = if (rb_male.isChecked){
            Constants.MALE
        }else{
            Constants.FEMALE
        }
        if (mUserProfileImageURL.isNotEmpty()){
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }
        if (mobileNumber.isNotEmpty()){
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        userHashMap[Constants.GENDER] = gender
        userHashMap[Constants.COMPLETE_PROFILE] = 1
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}