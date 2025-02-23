package com.anmol.e_learning

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonUpdateProfile: Button
    private lateinit var buttonChangePassword: Button
    private lateinit var buttonLogout: Button
    private lateinit var fabEditProfilePicture: ImageView
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI Components
        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        editTextName = view.findViewById(R.id.editTextName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        buttonUpdateProfile = view.findViewById(R.id.buttonUpdateProfile)
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword)
        buttonLogout = view.findViewById(R.id.buttonLogout)
        fabEditProfilePicture = view.findViewById(R.id.fabEditProfilePicture)

        // Load user data
        fetchUserData()

        // Profile Picture Change
        fabEditProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Update Profile
        buttonUpdateProfile.setOnClickListener {
            updateUserName(editTextName.text.toString())
        }

        // Change Password
        buttonChangePassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        // Logout
        buttonLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    // Fetch User Data from Firestore
    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    editTextName.setText(document.getString("name"))
                    editTextEmail.setText(document.getString("email"))

                    val base64Image = document.getString("profilePicture")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val bitmap = decodeBase64(base64Image)
                            imageViewProfile.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Convert Bitmap to Base64 String (Compressed for Faster Fetching)
    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream) // Compressing to 50% quality
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP) // NO_WRAP to avoid extra newlines
    }

    // Decode Base64 String to Bitmap
    private fun decodeBase64(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    // Handle Image Selection from Gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                imageViewProfile.setImageURI(imageUri) // Show selected image
                saveProfilePictureToFirestore(imageUri) // Convert & save
            }
        }
    }

    // Compress Image, Convert to Base64 & Save in Firestore
    private fun saveProfilePictureToFirestore(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val compressedBitmap = compressBitmap(bitmap, 50) // Compress before saving
            val base64Image = encodeToBase64(compressedBitmap)

            if (base64Image.isNotEmpty()) {
                db.collection("Users").document(userId)
                    .update("profilePicture", base64Image)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
                        imageViewProfile.setImageBitmap(compressedBitmap) // Update UI
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save profile picture", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Failed to convert image", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }

    // Compress Bitmap to Reduce Size
    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    // Update User Name in Firestore
    private fun updateUserName(newName: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users").document(userId)
            .update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Send Password Reset Email
    private fun sendPasswordResetEmail() {
        auth.currentUser?.email?.let {
            auth.sendPasswordResetEmail(it).addOnCompleteListener {
                Toast.makeText(requireContext(), "Check your email for password reset link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
