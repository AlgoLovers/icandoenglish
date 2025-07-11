package com.na982.icandoenglish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    // 샘플 데이터 (각 학년별 3개씩, 실제로는 100개로 확장)
    private val words = mapOf(
        "elementary_low" to listOf(
            Pair("사과", "apple"),
            Pair("책상", "desk"),
            Pair("학교", "school")
        ),
        "elementary_high" to listOf(
            Pair("연필", "pencil"),
            Pair("친구", "friend"),
            Pair("운동장", "playground")
        ),
        "middle" to listOf(
            Pair("과학", "science"),
            Pair("역사", "history"),
            Pair("수학", "math")
        ),
        "high" to listOf(
            Pair("철학", "philosophy"),
            Pair("문학", "literature"),
            Pair("경제", "economy")
        )
    )

    private var currentIndex = 0
    private var isFront = true
    private var currentGrade = "high"
    private var currentList = words["high"] ?: emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        currentGrade = prefs.getString("grade", "high") ?: "high"
        currentList = words[currentGrade] ?: words["high"]!!

        val tvWord = findViewById<TextView>(R.id.tvWord)
        val cardView = findViewById<CardView>(R.id.cardView)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        fun updateCard() {
            val (kor, eng) = currentList[currentIndex]
            tvWord.text = if (isFront) kor else eng
        }

        updateCard()

        cardView.setOnClickListener {
            isFront = !isFront
            updateCard()
        }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                isFront = true
                updateCard()
            }
        }
        btnNext.setOnClickListener {
            if (currentIndex < currentList.size - 1) {
                currentIndex++
                isFront = true
                updateCard()
            }
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 세팅에서 돌아왔을 때 학년 반영
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val newGrade = prefs.getString("grade", "high") ?: "high"
        if (newGrade != currentGrade) {
            currentGrade = newGrade
            currentList = words[currentGrade] ?: words["high"]!!
            currentIndex = 0
            isFront = true
            val tvWord = findViewById<TextView>(R.id.tvWord)
            tvWord.text = currentList[currentIndex].first
        }
    }
}