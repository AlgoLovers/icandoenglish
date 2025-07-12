package com.na982.icandoenglish

import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGradeGroup)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveGrade)

        // 현재 저장된 학년 반영
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val grade = prefs.getString("grade", "high") ?: "high"
        when (grade) {
            "elementary_low" -> radioGroup.check(R.id.radioElementaryLow)
            "elementary_high" -> radioGroup.check(R.id.radioElementaryHigh)
            "middle" -> radioGroup.check(R.id.radioMiddle)
            else -> radioGroup.check(R.id.radioHigh)
        }

        btnSave.setOnClickListener {
            val checkedId = radioGroup.checkedRadioButtonId
            val selectedGrade = when (checkedId) {
                R.id.radioElementaryLow -> "elementary_low"
                R.id.radioElementaryHigh -> "elementary_high"
                R.id.radioMiddle -> "middle"
                else -> "high"
            }
            prefs.edit().putString("grade", selectedGrade).apply()
            finish()
        }
    }
} 