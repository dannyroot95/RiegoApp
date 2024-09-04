package com.electric.liquid


import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.electric.liquid.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var binding : ActivityMainBinding
    private lateinit var switchActiveMotor: Switch
    private lateinit var switchActiveSensor: Switch
    private lateinit var switchActiveProgramming: Switch
    private lateinit var lottieAnimationView: LottieAnimationView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()

        lottieAnimationView = binding.animationView

        database = FirebaseDatabase.getInstance().getReference("Device/0101010100")
        getParametersSensor()

        switchActiveMotor = binding.btnSwitch
        switchActiveSensor = binding.btnSwitch2
        switchActiveProgramming = binding.btnSwitch3

        // Configurar listener para el Switch
        switchActiveMotor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Cambiar el valor en Firebase a "on"
                updateActiveStatus("activeSensor","on")
                lottieAnimationView.progress = 0f
                lottieAnimationView.repeatCount = LottieDrawable.INFINITE
                lottieAnimationView.playAnimation()

            } else {
                // Cambiar el valor en Firebase a "off"
                updateActiveStatus("activeSensor","off")
                if(!switchActiveSensor.isChecked && !switchActiveProgramming.isChecked){
                    lottieAnimationView.progress = 0f
                    lottieAnimationView.cancelAnimation()
                    lottieAnimationView.removeAllAnimatorListeners()
                }


            }
        }

        switchActiveSensor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateActiveStatus("isAuto","true")
                binding.viewSensor.visibility = View.VISIBLE
                lottieAnimationView.progress = 0f
                lottieAnimationView.repeatCount = LottieDrawable.INFINITE
                lottieAnimationView.playAnimation()
            }else{
                updateActiveStatus("isAuto","false")
                binding.viewSensor.visibility = View.GONE

                if(!switchActiveMotor.isChecked && !switchActiveProgramming.isChecked){
                    lottieAnimationView.progress = 0f
                    lottieAnimationView.cancelAnimation()
                    lottieAnimationView.removeAllAnimatorListeners()
                }

            }
        }

        switchActiveProgramming.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateActiveStatus("isProgramming","true")
                binding.btnProgramming.visibility = View.VISIBLE
                lottieAnimationView.progress = 0f
                lottieAnimationView.repeatCount = LottieDrawable.INFINITE
                lottieAnimationView.playAnimation()
                val marginRightPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics
                ).toInt()

                // Establecer el margen derecho
                val layoutParams = binding.btnSwitch3.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, marginRightPx, layoutParams.bottomMargin)
                binding.btnSwitch3.layoutParams = layoutParams
            }else{
                updateActiveStatus("isProgramming","false")
                binding.btnProgramming.visibility = View.GONE
                val marginRightPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
                ).toInt()

                // Establecer el margen derecho
                val layoutParams = binding.btnSwitch3.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, marginRightPx, layoutParams.bottomMargin)
                binding.btnSwitch3.layoutParams = layoutParams

                if(!switchActiveMotor.isChecked && !switchActiveSensor.isChecked){
                    lottieAnimationView.progress = 0f
                    lottieAnimationView.cancelAnimation()
                    lottieAnimationView.removeAllAnimatorListeners()
                }

            }
        }

        // Opcional: Leer el valor actual de Firebase para actualizar el estado del Switch
        getParameters()


}


    private fun updateActiveStatus(type: String,state: String) {
        database.child(type).setValue(state)
                /*
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {

            }
        }*/
    }

    private fun getParameters() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActiveMotor = snapshot.child("activeSensor").getValue(String::class.java)
                val isActiveSensor = snapshot.child("isAuto").getValue(String::class.java)
                val isActiveProgramming = snapshot.child("isProgramming").getValue(String::class.java)

                switchActiveMotor.isChecked = isActiveMotor == "on"
                switchActiveSensor.isChecked = isActiveSensor == "true"
                switchActiveProgramming.isChecked = isActiveProgramming == "true"

                binding.loader.visibility = View.GONE
                binding.allView.visibility = View.VISIBLE

            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        })
    }

    private fun getParametersSensor() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val humidityValue = snapshot.child("humidity").getValue(Int::class.java)
                val rainValue = snapshot.child("rainValue").getValue(Int::class.java)
                binding.humudityValue.text = (humidityValue!! /100).toString()+"%"
                binding.rainValue.text = (rainValue!! /100).toString()+"%"

            }
            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value: ${error.message}")
            }
        })
    }

    private fun setupTopBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE;
            }
        } else {
            // Para versiones anteriores, puedes usar un enfoque diferente
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }



}