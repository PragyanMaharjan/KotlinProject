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

data class DogAccessory(val id: String = "", val name: String = "", val price: String = "", val size: String = "", val color: String = "")

class AvailableDogAccessoryActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_dog_accessory)

        database = FirebaseDatabase.getInstance().getReference("DogAccessories")
        productsContainer = findViewById(R.id.productsContainer)

        val accessoryNameEditText = findViewById<EditText>(R.id.AccessoriesEditText)
        val priceEditText = findViewById<EditText>(R.id.priceEditText)
        val sizeEditText = findViewById<EditText>(R.id.sizeEditText)
        val colorEditText = findViewById<EditText>(R.id.colorEditText)
        val addProductButton = findViewById<Button>(R.id.addProductButton)

        // Add Product
        addProductButton.setOnClickListener {
            val accessoryName = accessoryNameEditText.text.toString()
            val price = priceEditText.text.toString()
            val size = sizeEditText.text.toString()
            val color = colorEditText.text.toString()

            if (accessoryName.isNotEmpty() && price.isNotEmpty() && size.isNotEmpty() && color.isNotEmpty()) {
                val productId = database.push().key ?: return@setOnClickListener
                val accessory = DogAccessory(productId, accessoryName, price, size, color)

                database.child(productId).setValue(accessory).addOnSuccessListener {
                    Toast.makeText(this, "Accessory added", Toast.LENGTH_SHORT).show()
                    accessoryNameEditText.text.clear()
                    priceEditText.text.clear()
                    sizeEditText.text.clear()
                    colorEditText.text.clear()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add accessory", Toast.LENGTH_SHORT).show()
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
                    val accessory = productSnapshot.getValue(DogAccessory::class.java)
                    if (accessory != null) {
                        addProductView(accessory)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AvailableDogAccessoryActivity,
                    "Failed to load accessories",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addProductView(accessory: DogAccessory) {
        val productView = LinearLayout(this)
        productView.orientation = LinearLayout.VERTICAL
        productView.setPadding(20, 20, 20, 20)
        productView.setBackgroundResource(android.R.color.darker_gray)

        val textView = TextView(this)
        textView.text =
            "Accessory: ${accessory.name}\nPrice: ${accessory.price}\nSize: ${accessory.size}\nColor: ${accessory.color}"
        textView.textSize = 16f
        textView.setPadding(10, 10, 10, 10)

        val updateButton = Button(this)
        updateButton.text = "Update"
        updateButton.setOnClickListener {
            showUpdateDialog(accessory)
        }

        val removeButton = Button(this)
        removeButton.text = "Remove"
        removeButton.setOnClickListener {
            database.child(accessory.id).removeValue()
        }

        productView.addView(textView)
        productView.addView(updateButton)
        productView.addView(removeButton)
        productsContainer.addView(productView)
    }

    private fun showUpdateDialog(accessory: DogAccessory) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_dog_accessory, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.updateAccessoriesEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.updatePriceEditText)
        val sizeEditText = dialogView.findViewById<EditText>(R.id.updateSizeEditText)
        val colorEditText = dialogView.findViewById<EditText>(R.id.updateColorEditText)

        nameEditText.setText(accessory.name)
        priceEditText.setText(accessory.price)
        sizeEditText.setText(accessory.size)
        colorEditText.setText(accessory.color)

        AlertDialog.Builder(this)
            .setTitle("Update Accessory")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedAccessory = DogAccessory(
                    accessory.id,
                    nameEditText.text.toString(),
                    priceEditText.text.toString(),
                    sizeEditText.text.toString(),
                    colorEditText.text.toString()
                )

                database.child(accessory.id).setValue(updatedAccessory).addOnSuccessListener {
                    Toast.makeText(this, "Accessory updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
