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

data class DogFood(val id: String = "", val name: String = "", val price: String = "", val weight: String = "", val age: String = "")

class AvailableDogFoodActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_dog_food)  // Ensure this matches your layout XML filename

        database = FirebaseDatabase.getInstance().getReference("DogFood")
        productsContainer = findViewById(R.id.productsContainer)

        val foodNameEditText = findViewById<EditText>(R.id.foodNameEditText)
        val priceEditText = findViewById<EditText>(R.id.priceEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val addProductButton = findViewById<Button>(R.id.addProductButton)

        addProductButton.setOnClickListener {
            val foodName = foodNameEditText.text.toString()
            val price = priceEditText.text.toString()
            val weight = weightEditText.text.toString()
            val age = ageEditText.text.toString()

            if (foodName.isNotEmpty() && price.isNotEmpty() && weight.isNotEmpty() && age.isNotEmpty()) {
                val productId = database.push().key ?: return@setOnClickListener
                val dogFood = DogFood(productId, foodName, price, weight, age)

                database.child(productId).setValue(dogFood).addOnSuccessListener {
                    Toast.makeText(this, "Dog Food added", Toast.LENGTH_SHORT).show()
                    foodNameEditText.text.clear()
                    priceEditText.text.clear()
                    weightEditText.text.clear()
                    ageEditText.text.clear()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add Dog Food", Toast.LENGTH_SHORT).show()
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
                    val dogFood = productSnapshot.getValue(DogFood::class.java)
                    if (dogFood != null) {
                        addProductView(dogFood)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AvailableDogFoodActivity, "Failed to load Dog Food", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addProductView(dogFood: DogFood) {
        val productView = LinearLayout(this)
        productView.orientation = LinearLayout.VERTICAL
        productView.setPadding(20, 20, 20, 20)
        productView.setBackgroundResource(android.R.color.darker_gray)

        val textView = TextView(this)
        textView.text = "Dog Food: ${dogFood.name}\nPrice: ${dogFood.price}\nWeight: ${dogFood.weight} kg\nAge: ${dogFood.age} months/years"
        textView.textSize = 16f
        textView.setPadding(10, 10, 10, 10)

        val updateButton = Button(this)
        updateButton.text = "Update"
        updateButton.setOnClickListener {
            showUpdateDialog(dogFood)
        }

        val removeButton = Button(this)
        removeButton.text = "Remove"
        removeButton.setOnClickListener {
            database.child(dogFood.id).removeValue()
        }

        productView.addView(textView)
        productView.addView(updateButton)
        productView.addView(removeButton)
        productsContainer.addView(productView)
    }

    private fun showUpdateDialog(dogFood: DogFood) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_product, null)
        val foodNameEditText = dialogView.findViewById<EditText>(R.id.updateFoodNameEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.updatePriceEditText)
        val weightEditText = dialogView.findViewById<EditText>(R.id.updateWeightEditText)
        val ageEditText = dialogView.findViewById<EditText>(R.id.updateAgeEditText)

        foodNameEditText.setText(dogFood.name)
        priceEditText.setText(dogFood.price)
        weightEditText.setText(dogFood.weight)
        ageEditText.setText(dogFood.age)

        AlertDialog.Builder(this)
            .setTitle("Update Dog Food")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedDogFood = DogFood(
                    dogFood.id,
                    foodNameEditText.text.toString(),
                    priceEditText.text.toString(),
                    weightEditText.text.toString(),
                    ageEditText.text.toString()
                )

                database.child(dogFood.id).setValue(updatedDogFood).addOnSuccessListener {
                    Toast.makeText(this, "Dog Food updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
