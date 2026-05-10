package com.example.cafeteriafoodpreorder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var etDisplayName: TextInputEditText
    private lateinit var btnUpdateName: MaterialButton
    private lateinit var btnChangePassword: Button
    private lateinit var btnLogout: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        etDisplayName = findViewById(R.id.etDisplayName)
        btnUpdateName = findViewById(R.id.btnUpdateName)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBar)

        loadUserData()

        val btnBackToMenu = findViewById<MaterialButton>(R.id.btnBackToMenu)
        btnBackToMenu.setOnClickListener {
            finish()
        }

        btnUpdateName.setOnClickListener {
            val newName = etDisplayName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateDisplayName(newName)
            } else {
                etDisplayName.error = "Name cannot be empty"
            }
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        tvUserEmail.text = user.email

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: user.displayName ?: "Customer"
                tvUserName.text = name
                etDisplayName.setText(name)

                val createdAt = document.getLong("createdAt")
                if (createdAt != null) {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    tvMemberSince.text = "Member since: ${dateFormat.format(Date(createdAt))}"
                }
            }
            .addOnFailureListener {
                tvUserName.text = user.displayName ?: "Customer"
            }
    }

    private fun updateDisplayName(newName: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                db.collection("users").document(user.uid)
                    .update("name", newName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show()
                        tvUserName.text = newName
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Firestore update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        // Use EditText because the simplified layout uses plain EditText
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update Password") { _, _ ->
                val currentPwd = etCurrentPassword.text.toString().trim()
                val newPwd = etNewPassword.text.toString().trim()
                val confirmPwd = etConfirmPassword.text.toString().trim()

                if (currentPwd.isEmpty()) {
                    Toast.makeText(this, "Enter current password", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd.isEmpty()) {
                    Toast.makeText(this, "Enter new password", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd != confirmPwd) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(currentPwd, newPwd)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        progressBar.visibility = ProgressBar.VISIBLE

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Current password is incorrect: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}