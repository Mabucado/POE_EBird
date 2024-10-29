package com.ebookfrenzy.poe_ebird

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var radioGroupEducation: RadioGroup
    private lateinit var radioMatric: RadioButton
    private lateinit var radioImparal: RadioButton
    private lateinit var saveButton: Button
    private lateinit var profileButton: Button
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_UPLOAD = 2
    private lateinit var imageUri: Uri
    private lateinit var profile:ImageView

    val model= globalModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioGroupEducation = view.findViewById(R.id.radioGroupEducation)
        radioMatric = view.findViewById(R.id.radioMatric)
        radioImparal = view.findViewById(R.id.radioImparal)
        saveButton = view.findViewById(R.id.saveButton)
        profileButton = view.findViewById(R.id.profileButton)
        profile=view.findViewById(R.id.profileImageView)


        saveButton.setOnClickListener {
            val selectedId = radioGroupEducation.checkedRadioButtonId
            val selectedOption: String? = when (selectedId) {
                R.id.radioMatric -> "metric"
                R.id.radioImparal -> "imperial"
                else -> null
            }

            if (selectedOption != null) {
                model.usersList.find { it.username==model.loggedInUser }?.let { user ->
                    user.unit = selectedOption}
                    Log.i("userList",model.usersList.toString())


                // Show confirmation
                Toast.makeText(activity, "Saved: $selectedOption", Toast.LENGTH_SHORT).show()

                // Close the dialog
                dismiss()
            } else {
                Toast.makeText(activity, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }

        // Profile button click listener
        profileButton.setOnClickListener {

            checkPermissions()
            showImageOptionsDialog()
            if( model.usersList.find { it.username==model.loggedInUser }?.profile_image!=null
            ){
                profile.setImageURI(model.usersList.find { it.username==model.loggedInUser }?.profile_image)
                saveModelDataToFirestore(model)
            }
        }


    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_IMAGE_CAPTURE
            )
        } else {
            // Permissions are already granted, proceed with your action
            showImageOptionsDialog()
        }
    }

    // Step 3: Handle the permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted, you can open camera or gallery
                    showImageOptionsDialog()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(requireContext(), "Camera and Storage permissions are required.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun showImageOptionsDialog() {
        val options = arrayOf("Capture Image", "Upload Image")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> captureImage() // Option 1: Capture Image
                1 -> uploadImage()  // Option 2: Upload Image
            }
        }
        builder.show()
    }
    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            // Create a file to save the image
            val photoFile: File = createImageFile() // Implement this method to create a file
            imageUri = Uri.fromFile(photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_UPLOAD)
    }
    private fun createImageFile(): File {
        // Create an image file name and return the file
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", storageDir)
    }

    // Handle the result from camera or gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Handle captured image from camera
                    // You now have the imageUri that points to the captured image
                    val extras = data!!.extras
                        // Image was captured using the camera
                        val photo: Bitmap = data!!.extras?.get("data") as Bitmap
                        // Convert Bitmap to Uri (You may need to save it locally and get the Uri)
                        val imageUri = saveBitmapToUri(photo)
                    model.usersList.find { it.username==model.loggedInUser }?.let { user ->
                        user.profile_image = imageUri}


                }
                REQUEST_IMAGE_UPLOAD -> {
                    // Handle uploaded image from gallery
                    if (data != null) {
                        val selectedImageUri: Uri? = data.data
                        model.usersList.find { it.username==model.loggedInUser }?.let { user ->
                            user.profile_image = selectedImageUri}

                    }
                }
            }
        }
    }
    private fun saveBitmapToUri(bitmap: Bitmap): Uri? {
        // Save the bitmap to a file and return its Uri
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "captured_image.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}