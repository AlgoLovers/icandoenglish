package com.na982.icandoenglish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class CongratulationsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congratulations)
        
        val totalWords = intent.getIntExtra("total_words", 0)
        val memorizedWords = intent.getIntExtra("memorized_words", 0)
        val grade = intent.getStringExtra("grade") ?: "high"
        val learningMode = intent.getStringExtra("learning_mode") ?: "new_words"
        val todayNewWords = intent.getIntExtra("today_new_words", 0)
        val todayReviewWords = intent.getIntExtra("today_review_words", 0)
        
        val tvTotalWords = findViewById<TextView>(R.id.tvTotalWords)
        val tvMemorizedWords = findViewById<TextView>(R.id.tvMemorizedWords)
        val tvProgressPercent = findViewById<TextView>(R.id.tvProgressPercent)
        val btnBackToMain = findViewById<MaterialButton>(R.id.btnBackToMain)
        val btnResetProgress = findViewById<MaterialButton>(R.id.btnResetProgress)
        
        // 통계 업데이트
        val modeText = when (learningMode) {
            "new_words" -> "신규 단어"
            "review_words" -> "복습 단어"
            else -> "단어"
        }
        
        tvTotalWords.text = "오늘의 $modeText: ${totalWords}개"
        tvMemorizedWords.text = "완료한 $modeText: ${memorizedWords}개"
        val progressPercent = if (totalWords > 0) (memorizedWords * 100 / totalWords) else 0
        tvProgressPercent.text = "진행률: ${progressPercent}%"
        
        // 메인으로 돌아가기
        btnBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        
        // 진행률 초기화
        btnResetProgress.setOnClickListener {
            // 모든 학습 데이터 초기화
            val memorizationPrefs = getSharedPreferences("memorization", Context.MODE_PRIVATE)
            val wordLearningPrefs = getSharedPreferences("word_learning_data", Context.MODE_PRIVATE)
            val dailyNewWordsPrefs = getSharedPreferences("daily_new_words", Context.MODE_PRIVATE)
            val studyDataPrefs = getSharedPreferences("study_data", Context.MODE_PRIVATE)
            
            memorizationPrefs.edit().clear().apply()
            wordLearningPrefs.edit().clear().apply()
            dailyNewWordsPrefs.edit().clear().apply()
            studyDataPrefs.edit().clear().apply()
            
            // 초기화 후 메인으로 돌아가기
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
} 