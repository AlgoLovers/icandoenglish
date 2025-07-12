package com.na982.icandoenglish

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    
    private lateinit var dateSelectionCard: MaterialCardView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvStudyTime: TextView
    private lateinit var tvStudyTimeAverage: TextView
    private lateinit var tvTtsCount: TextView
    private lateinit var tvTtsCountAverage: TextView
    private lateinit var tvWordsViewed: TextView
    private lateinit var tvWordsViewedAverage: TextView
    private lateinit var tvWordsMemorized: TextView
    private lateinit var tvWordsMemorizedAverage: TextView
    private lateinit var btnBack: MaterialButton
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        
        dateSelectionCard = findViewById(R.id.dateSelectionCard)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvStudyTime = findViewById(R.id.tvStudyTime)
        tvStudyTimeAverage = findViewById(R.id.tvStudyTimeAverage)
        tvTtsCount = findViewById(R.id.tvTtsCount)
        tvTtsCountAverage = findViewById(R.id.tvTtsCountAverage)
        tvWordsViewed = findViewById(R.id.tvWordsViewed)
        tvWordsViewedAverage = findViewById(R.id.tvWordsViewedAverage)
        tvWordsMemorized = findViewById(R.id.tvWordsMemorized)
        tvWordsMemorizedAverage = findViewById(R.id.tvWordsMemorizedAverage)
        btnBack = findViewById(R.id.btnBack)
        
        // 오늘 날짜로 초기화
        val today = Calendar.getInstance()
        updateStatsForDate(dateFormat.format(today.time))
        
        // 날짜 선택 카드 클릭 리스너
        dateSelectionCard.setOnClickListener {
            showDatePicker()
        }
        
        // 뒤로 가기 버튼
        btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val selectedDate = dateFormat.format(selectedCalendar.time)
                updateStatsForDate(selectedDate)
            },
            year, month, day
        )
        
        // DatePickerDialog 스타일 설정
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // 오늘까지만 선택 가능
        
        datePickerDialog.show()
    }
    
    private fun updateStatsForDate(date: String) {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: Date()
        tvSelectedDate.text = displayDateFormat.format(calendar.time)
        
        val studyData = getStudyDataForDate(date)
        val studyTimeMonthlyAvg = getMonthlyAverage(date) { it.totalStudyTime }
        val ttsCountMonthlyAvg = getMonthlyAverage(date) { it.totalTtsCount.toLong() }
        val wordsViewedMonthlyAvg = getMonthlyAverage(date) { it.totalWordsViewed.toLong() }
        val wordsMemorizedMonthlyAvg = getMonthlyAverage(date) { it.totalWordsMemorized.toLong() }
        
        // 학습 시간(초→분)
        val studyTimeMinutes = (studyData.totalStudyTime / 60).toInt()
        val avgStudyTimeMinutes = (studyTimeMonthlyAvg / 60).toInt()
        tvStudyTime.text = "${studyTimeMinutes}분"
        tvStudyTimeAverage.text = "평균: ${avgStudyTimeMinutes}분"
        if (studyTimeMinutes >= avgStudyTimeMinutes) {
            tvStudyTime.setTextColor(Color.parseColor("#2E7D32")) // 초록
        } else {
            tvStudyTime.setTextColor(Color.parseColor("#D32F2F")) // 빨강
        }

        // 음성 재생
        tvTtsCount.text = "${studyData.totalTtsCount}회"
        tvTtsCountAverage.text = "평균: ${ttsCountMonthlyAvg}회"
        if (studyData.totalTtsCount >= ttsCountMonthlyAvg) {
            tvTtsCount.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            tvTtsCount.setTextColor(Color.parseColor("#D32F2F"))
        }

        // 본 단어
        tvWordsViewed.text = "${studyData.totalWordsViewed}개"
        tvWordsViewedAverage.text = "평균: ${wordsViewedMonthlyAvg}개"
        if (studyData.totalWordsViewed >= wordsViewedMonthlyAvg) {
            tvWordsViewed.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            tvWordsViewed.setTextColor(Color.parseColor("#D32F2F"))
        }

        // 암기 완료
        tvWordsMemorized.text = "${studyData.totalWordsMemorized}개"
        tvWordsMemorizedAverage.text = "평균: ${wordsMemorizedMonthlyAvg}개"
        if (studyData.totalWordsMemorized >= wordsMemorizedMonthlyAvg) {
            tvWordsMemorized.setTextColor(Color.parseColor("#2E7D32"))
        } else {
            tvWordsMemorized.setTextColor(Color.parseColor("#D32F2F"))
        }
    }

    private fun getStudyDataForDate(date: String): DailyStudyData {
        val prefs = getSharedPreferences("study_data", Context.MODE_PRIVATE)
        val json = prefs.getString("daily_data_$date", null)
        return if (json != null) {
            try {
                gson.fromJson(json, DailyStudyData::class.java)
            } catch (e: Exception) {
                DailyStudyData(date)
            }
        } else {
            DailyStudyData(date)
        }
    }

    // 항목별 평균을 구하는 제네릭 함수
    private fun getMonthlyAverage(date: String, selector: (DailyStudyData) -> Long): Long {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(date) ?: Date()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        var total = 0L
        var daysWithData = 0
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentTime = System.currentTimeMillis()
        for (day in 1..daysInMonth) {
            val checkCalendar = Calendar.getInstance()
            checkCalendar.set(year, month, day)
            if (checkCalendar.timeInMillis > currentTime) break
            val checkDate = dateFormat.format(checkCalendar.time)
            val studyData = getStudyDataForDate(checkDate)
            val value = selector(studyData)
            if (value > 0) {
                total += value
                daysWithData++
            }
        }
        return if (daysWithData > 0) total / daysWithData else 0L
    }
} 