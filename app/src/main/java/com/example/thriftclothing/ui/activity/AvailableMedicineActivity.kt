package com.example.thriftclothing.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftclothing.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Medicine(val id: String = "", val name: String = "", val price: String = "", val age: String = "", val weight: String = "")

class AvailableMedicineActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_medicine)

        // Initialize Firebase database reference for Medicine
        database = FirebaseDatabase.getInstance().getReference("Medicine")
        productsContainer = findViewById(R.id.productsContainer)

        // Initialize UI elements
        val medicineNameEditText = findViewById<EditText>(R.id.updateMedicineNameEditText)
        val priceEditText = findViewById<EditText>(R.id.updatePriceEditText)
        val ageEditText = findViewById<EditText>(R.id.updateAgeEditText)
        val weightEditText = findViewById<EditText>(R.id.updateWeightEditText)
        val addProductButton = findViewById<Button>(R.id.addProductButton)

        // Add Product
        addProductButton.setOnClickListener {
            val medicineName = medicineNameEditText.text.toString()
            val price = priceEditText.text.toString()
            val age = ageEditText.text.toString()
            val weight = weightEditText.text.toString()

            if (medicineName.isNotEmpty() && price.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty()) {
                val productId = database.push().key ?: return@setOnClickListener
                val medicine = Medicine(productId, medicineName, price, age, weight)

                database.child(productId).setValue(medicine).addOnSuccessListener {
                    Toast.makeText(this, "Medicine added", Toast.LENGTH_SHORT).show()
                    medicineNameEditText.text.clear()
                    priceEditText.text.clear()
                    ageEditText.text.clear()
                    weightEditText.text.clear()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Load products from Firebase
        loadProducts()
    }

    private fun loadProducts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productsContainer.removeAllViews()
                for (productSnapshot in snapshot.children) {
                    val medicine = productSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        addProductView(medicine)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AvailableMedicineActivity,
                    "Failed to load medicine",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addProductView(medicine: Medicine) {
        val productView = LinearLayout(this)
        productView.orientation = LinearLayout.VERTICAL
        productView.setPadding(20, 20, 20, 20)
        productView.setBackgroundResource(android.R.color.darker_gray)

        val textView = TextView(this)
        textView.text = "Medicine: ${medicine.name}\nPrice: ${medicine.price}\nAge: ${medicine.age}\nWeight: ${medicine.weight}"
        textView.textSize = 16f
        textView.setPadding(10, 10, 10, 10)

        val updateButton = Button(this)
        updateButton.text = "Update"
        updateButton.setOnClickListener {
            showUpdateDialog(medicine)
        }

        val removeButton = Button(this)
        removeButton.text = "Remove"
        removeButton.setOnClickListener {
            database.child(medicine.id).removeValue()
        }

        productView.addView(textView)
        productView.addView(updateButton)
        productView.addView(removeButton)
        productsContainer.addView(productView)
    }

    private fun showUpdateDialog(medicine: Medicine) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_medicine, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.medicineNameEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)
        val ageEditText = dialogView.findViewById<EditText>(R.id.ageEditText)
        val weightEditText = dialogView.findViewById<EditText>(R.id.weightEditText)

        nameEditText.setText(medicine.name)
        priceEditText.setText(medicine.price)
        ageEditText.setText(medicine.age)
        weightEditText.setText(medicine.weight)

        AlertDialog.Builder(this)
            .setTitle("Update Medicine")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedName = nameEditText.text.toString()
                val updatedPrice = priceEditText.text.toString()
                val updatedAge = ageEditText.text.toString()
                val updatedWeight = weightEditText.text.toString()

                // Validation to ensure no field is empty
                if (updatedName.isEmpty() || updatedPrice.isEmpty() || updatedAge.isEmpty() || updatedWeight.isEmpty()) {
                    Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedMedicine = Medicine(
                    medicine.id,
                    updatedName,
                    updatedPrice,
                    updatedAge,
                    updatedWeight
                )

                database.child(medicine.id).setValue(updatedMedicine).addOnSuccessListener {
                    Toast.makeText(this, "Medicine updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
