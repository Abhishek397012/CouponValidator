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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class   MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseReference

    val boothName = "admin"

    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Firebase.database.reference

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dialog = Dialog(this)

        binding.seeInfo.setOnClickListener {
            dialog.setContentView(R.layout.see_booth_info)




            dialog.show()
        }

        binding.verify.setOnClickListener {
//            setup(1, 100)
            binding.verify.isEnabled = false
            val number = binding.editTextNumber.text.trim().toString()

            if (number.isEmpty()) {
                dialog.setContentView(R.layout.invalid)
                val close = dialog.findViewById<ImageView>(R.id.close2)
                close.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                binding.verify.isEnabled = true
                return@setOnClickListener
            }


            db.child("referral").child(binding.editTextNumber.text.toString())
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {

                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(
                                baseContext,
                                "Some Network Error Occurred, Please Tuy again!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.verify.isEnabled = true
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            if (data.exists()) {
                                if (data.child("Used").value.toString() == "No") {
                                    dialog.setContentView(R.layout.confirmation)
                                    val yes = dialog.findViewById<Button>(R.id.yes)
                                    val no = dialog.findViewById<Button>(R.id.no)

                                    yes.setOnClickListener {
                                        db.child("referral/$number/Used")
                                            .setValue("Yes")
                                        db.child("referral/$number/time")
                                            .setValue(Date().toString())
                                        db.child("referral/$number/redeemedBy")
                                            .setValue(boothName)
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
                                    val time = data.child("time").value.toString()
                                    val redeemedBy = data.child("redeemedBy").value.toString()
                                    val message = "Coupon is already redeemed on $time by $redeemedBy"


                                    dialog.setContentView(R.layout.alreadyused)
                                    val close = dialog.findViewById<ImageView>(R.id.close)
                                    val setText = dialog.findViewById<TextView>(R.id.invalidwarning)
                                    setText.text = message
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

        db.child("referral").addValueEventListener(
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

                    binding.percentage.text =
                        if (ct != 0)
                            String.format("%.2f", (redeemed.toDouble() / ct.toDouble()) * 100) + "%"
                        else
                            "0.00%"

                    binding.progressBar.progress =
                        ((redeemed.toDouble() / ct.toDouble()) * 100).toInt()
                }
            }
        )

    }


    private fun setup(start: Int, end: Int) {
        for(each in start..end) {
            val map = mapOf(
                "Used" to "No"
            )
            db.child("referral").child(each.toString()).setValue(map)
        }
    }
}