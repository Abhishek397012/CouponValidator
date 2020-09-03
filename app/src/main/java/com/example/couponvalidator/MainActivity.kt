package com.example.couponvalidator

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.couponvalidator.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var db = FirebaseDatabase.getInstance().reference
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dialog = Dialog(this)

        binding.verify.setOnClickListener {
            binding.verify.isEnabled = false
            val number = binding.editTextNumber.text.toString()

            if (binding.editTextNumber.text.isEmpty()) {

                dialog.setContentView(R.layout.invalid)
                val close = dialog.findViewById<ImageView>(R.id.close2)
                close.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                binding.verify.isEnabled = true
                return@setOnClickListener
            }

            db.child("referal").child(binding.editTextNumber.text.toString())
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            if (data.exists()) {
                                if (data.child("Used").value.toString() == "No") {
                                    dialog.setContentView(R.layout.confirmation)
                                    val yes = dialog.findViewById<Button>(R.id.yes)
                                    val no = dialog.findViewById<Button>(R.id.no)

                                    yes.setOnClickListener {
                                        db.child("referal/$number/Used")
                                            .setValue("Yes")
                                        db.child("referal/$number/time")
                                            .setValue(Date().toString())
                                        dialog.dismiss()
                                        Toast.makeText(
                                            baseContext,
                                            "Coupon Redeemed!!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        binding.editTextNumber.text.clear()
                                    }
                                    no.setOnClickListener {
                                        dialog.dismiss()
                                        binding.editTextNumber.text.clear()
                                    }
                                    dialog.show()
                                } else {
                                    var message = "Coupon is already used on "
                                    val time = data.child("time").value.toString()
                                    message += time

                                    dialog.setContentView(R.layout.alreadyused)
                                    val close = dialog.findViewById<ImageView>(R.id.close)
                                    val settext = dialog.findViewById<TextView>(R.id.invalidwarning)
                                    settext.text = message
                                    close.setOnClickListener {
                                        dialog.dismiss()
                                        binding.editTextNumber.text.clear()
                                    }
                                    dialog.show()

                                }
                            } else {

                                dialog.setContentView(R.layout.invalid)
                                val close = dialog.findViewById<ImageView>(R.id.close2)
                                close.setOnClickListener {
                                    dialog.dismiss()
                                    binding.editTextNumber.text.clear()
                                }
                                dialog.show()
                            }

                            binding.verify.isEnabled = true


                        }


                    }
                )
        }

        db.child("referal").addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(data: DataSnapshot) {
                    var ct: Int = 0
                    var redeemed: Int = 0
                    for (each in data.children) {
                        ct++
                        if (each.child("Used").value.toString() == "Yes")
                            redeemed++
                    }
                    binding.fraction.text = "$redeemed/$ct"
                    if (ct != 0)
                        binding.percentage.text =
                            String.format("%.2f", (redeemed.toDouble() / ct.toDouble()) * 100) + "%"
                    binding.progressBar.progress =
                        ((redeemed.toDouble() / ct.toDouble()) * 100).toInt()
                }
            }
        )

    }
}