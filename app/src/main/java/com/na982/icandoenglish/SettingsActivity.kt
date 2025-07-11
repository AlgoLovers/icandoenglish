package com.na982.icandoenglish

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedGrade = prefs.getString("grade", "high")
        when (savedGrade) {
            "elementary_low" -> radioGroup.check(R.id.radioElementaryLow)
            "elementary_high" -> radioGroup.check(R.id.radioElementaryHigh)
            "middle" -> radioGroup.check(R.id.radioMiddle)
            "high" -> radioGroup.check(R.id.radioHigh)
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            when (checkedId) {
                R.id.radioElementaryLow -> editor.putString("grade", "elementary_low")
                R.id.radioElementaryHigh -> editor.putString("grade", "elementary_high")
                R.id.radioMiddle -> editor.putString("grade", "middle")
                R.id.radioHigh -> editor.putString("grade", "high")
            }
            editor.apply()
            finish()
        }
    }
} 