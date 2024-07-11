package com.example.hideucalculator

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hideucalculator.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.Manifest


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>



    //TextViews
    lateinit var ans: TextView
    lateinit var no1: EditText
    lateinit var no2: EditText
    lateinit var operator: TextView

    lateinit var module_txt: TextView
    lateinit var parathansis_1_txt: TextView
    lateinit var parnthasis_2_txt: TextView
    lateinit var backspace_txt: TextView
    lateinit var seven_txt: TextView
    lateinit var eight_txt: TextView
    lateinit var nine_txt: TextView
    lateinit var division_txt: TextView
    lateinit var four_txt: TextView
    lateinit var five_txt: TextView
    lateinit var six_txt: TextView
    lateinit var multiply_txt: TextView
    lateinit var one_txt: TextView
    lateinit var two_txt: TextView
    lateinit var three_txt: TextView
    lateinit var minus_txt: TextView
    lateinit var zero_txt: TextView
    lateinit var dot_txt: TextView
    lateinit var plus_txt: TextView

    //CardView
    lateinit var module_card: CardView
    lateinit var pranthsis_1_card: CardView
    lateinit var prnthasis_2_card: CardView
    lateinit var backspace_card: CardView
    lateinit var seven_card: CardView
    lateinit var six_card: CardView
    lateinit var eight_card: CardView
    lateinit var nine_card: CardView
    lateinit var division_card: CardView
    lateinit var four_card: CardView
    lateinit var five_card: CardView
    lateinit var three_card: CardView
    lateinit var multiply_card: CardView
    lateinit var two_card: CardView
    lateinit var one_card: CardView
    lateinit var minus_card: CardView
    lateinit var zero_card: CardView
    lateinit var dot_card: CardView
    lateinit var ans_card: CardView
    lateinit var plus_card: CardView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Load SharedPreferenced Data
        loaddata()

        //For Asking Media Permission
        setupPermissionLauncher()
        checkAndRequestPermissions()

        //For Displaying Dilaog Box For Asking Passsword
        if(loaddata()==true){

        }else {
            var dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_xml_file)
            dialog.setCancelable(false)
            dialog.show()



            val view = layoutInflater.inflate(R.layout.dialog_xml_file, null)
            dialog.setContentView(view)

            var btn: Button = view.findViewById(R.id.dialog_save_btn)

            btn.setOnClickListener {
                savedata()

                var pass_edittext :EditText=view.findViewById(R.id.dialog_et)
                var pass:Int=pass_edittext.text.toString().toInt()


                database=FirebaseDatabase.getInstance().getReference("User")
                val User=User(pass)

                database.child(pass.toString()).setValue(User).addOnSuccessListener {

                    dialog.dismiss()
                    Toast.makeText(this,"Saved Successfull",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{
                    Toast.makeText(this,"Not Saved",Toast.LENGTH_SHORT).show()
                }

            }
        }




        //Textview Id Find

        ans = findViewById(R.id.ans)
        no1 = findViewById(R.id.no1)
        no2 = findViewById(R.id.no2)
        plus_txt = findViewById(R.id.plus_txt)
        minus_txt = findViewById(R.id.minus_txt)
        division_txt = findViewById(R.id.division_txt)
        multiply_txt = findViewById(R.id.multiply_txt)
        module_txt = findViewById(R.id.module_txt)
        parathansis_1_txt = findViewById(R.id.parathansis_1_txt)
        parnthasis_2_txt = findViewById(R.id.pranthasis_2_txt)
        backspace_txt = findViewById(R.id.backspace_txt)
        dot_txt = findViewById(R.id.dot_txt)
        one_txt = findViewById(R.id.one_txt)
        two_txt = findViewById(R.id.two_txt)
        three_txt = findViewById(R.id.three_txt)
        four_txt = findViewById(R.id.four_txt)
        five_txt = findViewById(R.id.five_txt)
        six_txt = findViewById(R.id.six_txt)
        seven_txt = findViewById(R.id.seven_txt)
        eight_txt = findViewById(R.id.eight_txt)
        nine_txt = findViewById(R.id.nine_txt)
        operator = findViewById(R.id.operator)

        //CardView Id Find
        plus_card = findViewById(R.id.plus_card)
        minus_card = findViewById(R.id.minus_card)
        division_card = findViewById(R.id.division_card)
        multiply_card = findViewById(R.id.multiply_card)
        module_card = findViewById(R.id.module_card)
        backspace_card = findViewById(R.id.backspace_card)

        zero_card = findViewById(R.id.zero_card)
        one_card = findViewById(R.id.one_card)
        two_card = findViewById(R.id.two_card)
        three_card = findViewById(R.id.three_card)
        four_card = findViewById(R.id.four_card)
        five_card = findViewById(R.id.five_card)
        six_card = findViewById(R.id.six_card)
        seven_card = findViewById(R.id.seven_card)
        eight_card = findViewById(R.id.eight_card)
        nine_card = findViewById(R.id.nine_card)
        ans_card = findViewById(R.id.ans_card)
        dot_card = findViewById(R.id.dot_card)




        //Reterive Data From The Database

        ans_card.setOnClickListener {

            var check_pass=no1.text.toString()

            if(check_pass.isNotEmpty()){
                readData(check_pass)
            }else{
                Toast.makeText(this,"Please Enter Toast",Toast.LENGTH_SHORT).show()
            }
        }




        zero_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "0")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "0")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }


        one_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "1")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "1")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        two_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "2")

                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            } else {
                no2.setText(no2.text.toString() + "2")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        three_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "3")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "3")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        four_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "4")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "4")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        five_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "5")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "5")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        six_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "6")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "6")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        seven_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "7")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "7")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        eight_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "8")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "8")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        nine_card.setOnClickListener() {

            if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + "9")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f

            } else {
                no2.setText(no2.text.toString() + "9")
                no1.textSize = 30f
                no2.textSize = 30f
                operator.textSize = 30f
            }
        }

        dot_card.setOnClickListener() {

            if (no1.text.toString().contains(".") || no2.text.toString().contains(".")) {
                ans.setText("Invalid Expression")
            } else if (operator.text.isEmpty()) {
                no1.setText(no1.text.toString() + ".")
            } else {
                no2.setText(no2.text.toString() + ".")
            }
        }


        plus_card.setOnClickListener() {

            if (no1.text.toString().isEmpty()) {
                ans.setText("Invalid Expression")
            } else {
                operator.text = "+"
            }

        }

        minus_card.setOnClickListener() {

            if (no1.text.toString().isEmpty()) {
                ans.setText("Invalid Expression")
            } else {
                operator.text = "-"
            }

        }

        division_card.setOnClickListener() {

            if (no1.text.toString().isEmpty()) {
                ans.setText("Invalid Expression")
            } else {
                operator.text = "/"
            }

        }

        multiply_card.setOnClickListener() {

            if (no1.text.toString().isEmpty()) {
                ans.setText("Invalid Expression")
            } else {
                operator.text = "*"
            }

        }

        module_card.setOnClickListener() {
            no1.setText("")
            operator.text = ""
            no2.setText("")
            ans.text = ""
        }

        backspace_card.setOnClickListener() {

            if (operator.text.isEmpty()) {

                var txt = no1.text.toString()
                var st = ""
                for (i in txt.dropLast(1)) {
                    st = st + i
                }
                no1.setText(st)
            }

            if (!operator.text.isEmpty()) {

                var txt = no2.text.toString()
                var st = ""
                for (i in txt.dropLast(1)) {
                    st = st + i
                }
                no2.setText(st)
            }
        }


        ans_card.setOnClickListener() {
            if (no1.text.isEmpty() || no2.text.isEmpty() || operator.text.isEmpty()) {
                ans.text = "Invalid Exptession"
                var check_pass=no1.text.toString()

                if(check_pass.isNotEmpty()){
                    readData(check_pass)
                }else{
                    Toast.makeText(this,"Please Enter Toast",Toast.LENGTH_SHORT).show()
                }
            }else if (operator.text.toString().contains("+")) {
                ans.text =
                    (no1.text.toString().toFloat() + no2.text.toString().toFloat()).toString()
                no1.textSize = 20f
                no2.textSize = 20f
                operator.textSize = 20f
            } else if (operator.text.toString().contains("-")) {
                ans.text =
                    (no1.text.toString().toFloat() - no2.text.toString().toFloat()).toString()
                no1.textSize = 20f
                no2.textSize = 20f
                operator.textSize = 20f
            } else if (operator.text.toString().contains("*")) {
                ans.text =
                    (no1.text.toString().toFloat() * no2.text.toString().toFloat()).toString()
                no1.textSize = 20f
                no2.textSize = 20f
                operator.textSize = 20f
            } else if (operator.text.toString().contains("/")) {
                ans.text =
                    (no1.text.toString().toFloat() / no2.text.toString().toFloat()).toString()
                no1.textSize = 20f
                no2.textSize = 20f
                operator.textSize = 20f
            } else {
                ans.text =
                    (no1.text.toString().toFloat() % no2.text.toString().toFloat()).toString()
                no1.textSize = 20f
                no2.textSize = 20f
                operator.textSize = 20f
            }
        }


    }

    //Function For save data in Shared Srefrences

    fun savedata() {
        var preference: SharedPreferences = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var editor = preference.edit()
        editor.apply() {
            putBoolean("key", true)
        }.apply()
        Toast.makeText(this, "Password Saved", Toast.LENGTH_SHORT).show()
    }

    //Function for retrive data of Shared Preference
    fun loaddata():Boolean{
        var preference: SharedPreferences = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var mykey=preference.getBoolean("key",false)
        return mykey
    }

    fun readData(check_Pass:String){
        database=FirebaseDatabase.getInstance().getReference("User")
        database.child(check_Pass).get().addOnSuccessListener {

            if(it.exists()){
                var intent=Intent(this,UpActivity::class.java)
                var mess=no1.text.toString()
                intent.putExtra("PASSWORD",mess)
                Toast.makeText(this,"Hello"+mess,Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Password Does Not Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Unable To Get Data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // All permissions are granted
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                // Some permissions are denied
                Toast.makeText(this, "Permissions are required to access media files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}



