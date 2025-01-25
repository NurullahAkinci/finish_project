package com.nurullahakinci.myhealthcouch

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.content.SharedPreferences
import android.net.Uri
import android.content.Intent
import android.provider.MediaStore
import de.hdodenhof.circleimageview.CircleImageView

class ProfileSettingsActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var profileImage: CircleImageView
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
        
        setupToolbar()
        setupPreferences()
        loadProfileData()
        setupImagePicker()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profil Düzenle"
    }
    
    private fun setupPreferences() {
        prefs = getSharedPreferences("MyHealthCouch", MODE_PRIVATE)
    }
    
    private fun setupImagePicker() {
        profileImage = findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, PICK_IMAGE)
        }
    }
    
    private fun loadProfileData() {
        findViewById<TextInputEditText>(R.id.nameInput).setText(prefs.getString("user_name", ""))
        findViewById<TextInputEditText>(R.id.ageInput).setText(prefs.getInt("user_age", 0).toString())
        findViewById<TextInputEditText>(R.id.heightInput).setText(prefs.getInt("user_height", 0).toString())
        findViewById<TextInputEditText>(R.id.weightInput).setText(prefs.getFloat("user_weight", 0f).toString())
        
        // Profil resmini yükle
        val savedImageUri = prefs.getString("profile_image", null)
        if (savedImageUri != null) {
            try {
                profileImage.setImageURI(Uri.parse(savedImageUri))
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.default_profile)
            }
        }
    }
    
    fun saveProfileData() {
        val editor = prefs.edit()
        
        // Metin alanlarından verileri al
        val name = findViewById<TextInputEditText>(R.id.nameInput).text.toString()
        val age = findViewById<TextInputEditText>(R.id.ageInput).text.toString().toIntOrNull() ?: 0
        val height = findViewById<TextInputEditText>(R.id.heightInput).text.toString().toIntOrNull() ?: 0
        val weight = findViewById<TextInputEditText>(R.id.weightInput).text.toString().toFloatOrNull() ?: 0f
        
        // Verileri kaydet
        editor.putString("user_name", name)
        editor.putInt("user_age", age)
        editor.putInt("user_height", height)
        editor.putFloat("user_weight", weight)
        
        // Profil resmini kaydet
        selectedImageUri?.let {
            editor.putString("profile_image", it.toString())
        }
        
        editor.apply()
        
        Toast.makeText(this, "Profil bilgileri kaydedildi", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            selectedImageUri = data?.data
            profileImage.setImageURI(selectedImageUri)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 