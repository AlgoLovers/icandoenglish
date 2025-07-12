package com.na982.icandoenglish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import java.text.SimpleDateFormat

// 데이터 클래스 정의
data class WordEntry(
    val kor: String,
    val eng: String,
    val sentences: List<Pair<String, String>>
)

// 학습 시스템을 위한 새로운 데이터 클래스들
data class WordLearningData(
    val word: String,
    val grade: String,
    val memorizationCount: Int = 0, // 암기 성공 횟수 (0, 1, 2, 3...)
    val lastMemorizedDate: String? = null, // 마지막 암기 완료 날짜
    val nextReviewDate: String? = null, // 다음 복습 날짜
    val isNewWord: Boolean = true, // 신규 단어 여부
    val dailyNewWordDate: String? = null // 신규 단어로 배정된 날짜
)



enum class LearningMode {
    NEW_WORDS, // 신규 단어 암기 모드
    REVIEW_WORDS // 복습 단어 암기 모드
}

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var allWords: List<WordEntry> = emptyList()
    private var words: List<WordEntry> = emptyList()
    private var currentIndex = 0
    private var isWordFront = true
    private var isSentenceFront = true
    private var currentGrade = "high"
    private var currentSentenceIndex = 0
    private var tts: TextToSpeech? = null
    private lateinit var cardView: MaterialCardView
    private lateinit var tvWord: TextView
    private lateinit var tvSentence: TextView
    private lateinit var tvProgress: TextView
    private lateinit var sentenceCard: MaterialCardView
    private lateinit var wordGestureDetector: GestureDetector
    private lateinit var sentenceGestureDetector: GestureDetector
    private lateinit var btnMemorized: MaterialButton
    private lateinit var btnNeedMore: MaterialButton
    private lateinit var btnFilter: MaterialButton
    private lateinit var btnMenu: ImageButton
    private lateinit var sideMenu: MaterialCardView
    private lateinit var sideMenuOverlay: View
    private lateinit var menuListView: ListView
    private val handler = Handler(Looper.getMainLooper())
    private var wordTtsRunnable: Runnable? = null
    private var sentenceTtsRunnable: Runnable? = null
    private var showOnlyUnmemorized = true
    private var isSideMenuOpen = false
    private lateinit var btnMenuCategory: MaterialButton
    private lateinit var btnMenuCalendar: MaterialButton

    // 학습 시스템 관련 변수들
    private var currentLearningMode = LearningMode.NEW_WORDS
    private var todayNewWords: List<WordEntry> = emptyList() // 오늘의 신규 단어들
    private var todayReviewWords: List<WordEntry> = emptyList() // 오늘의 복습 단어들
    private val maxNewWordsPerDay = 10 // 하루 최대 신규 단어 수
    private val reviewIntervals = listOf(3, 6, 12, 24) // 복습 간격 (일)

    // 학습 데이터 추적
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var sessionStartTime: Long = 0
    private var currentSessionTtsCount = 0
    private var currentSessionWordsViewed = 0
    private var currentSessionWordsMemorized = 0
    private var lastWordChangeTime: Long = 0

    // --- 새로운 학습 시스템 관리 ---
    private fun getWordLearningData(word: String): WordLearningData {
        val prefs = getSharedPreferences("word_learning_data", Context.MODE_PRIVATE)
        val key = "word_data_${currentGrade}_${word.hashCode()}"
        val json = prefs.getString(key, null)
        
        return if (json != null) {
            try {
                gson.fromJson(json, WordLearningData::class.java)
            } catch (e: Exception) {
                WordLearningData(word, currentGrade)
            }
        } else {
            WordLearningData(word, currentGrade)
        }
    }

    private fun saveWordLearningData(data: WordLearningData) {
        val prefs = getSharedPreferences("word_learning_data", Context.MODE_PRIVATE)
        val key = "word_data_${currentGrade}_${data.word.hashCode()}"
        val json = gson.toJson(data)
        prefs.edit().putString(key, json).apply()
    }

    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    // 신규 단어 배정
    private fun assignNewWordsForToday() {
        val today = getCurrentDate()
        val prefs = getSharedPreferences("daily_new_words", Context.MODE_PRIVATE)
        val assignedDate = prefs.getString("last_assigned_date_$currentGrade", null)
        
        Log.d("WordAssignment", "=== 신규 단어 배정 시작 ===")
        Log.d("WordAssignment", "오늘 날짜: $today")
        Log.d("WordAssignment", "마지막 배정 날짜: $assignedDate")
        Log.d("WordAssignment", "현재 학년: $currentGrade")
        
        if (assignedDate != today) {
            // 오늘 아직 신규 단어를 배정하지 않았다면 배정
            val unassignedWords = allWords.filter { word ->
                val data = getWordLearningData(word.kor)
                data.dailyNewWordDate == null
            }
            
            Log.d("WordAssignment", "배정 가능한 단어 수: ${unassignedWords.size}")
            Log.d("WordAssignment", "필요한 단어 수: $maxNewWordsPerDay")
            
            // 정확히 10개만 배정 (배정 가능한 단어가 10개 이상이어야 함)
            if (unassignedWords.size >= maxNewWordsPerDay) {
                val wordsToAssign = unassignedWords.take(maxNewWordsPerDay)
                
                Log.d("WordAssignment", "실제 배정할 단어 수: ${wordsToAssign.size}")
                
                wordsToAssign.forEach { word ->
                    val data = getWordLearningData(word.kor)
                    val updatedData = data.copy(
                        dailyNewWordDate = today,
                        isNewWord = true
                    )
                    saveWordLearningData(updatedData)
                    Log.d("WordAssignment", "단어 배정: ${word.kor} -> $today")
                }
                
                // 배정 날짜 기록
                prefs.edit().putString("last_assigned_date_$currentGrade", today).apply()
                Log.d("WordAssignment", "배정 날짜 기록 완료: $today")
            } else {
                Log.d("WordAssignment", "배정 가능한 단어가 부족함: ${unassignedWords.size} < $maxNewWordsPerDay")
            }
        } else {
            Log.d("WordAssignment", "오늘 이미 배정 완료됨")
        }
        Log.d("WordAssignment", "=== 신규 단어 배정 완료 ===")
    }

    // 복습할 단어들 가져오기 (암기 완료하지 않은 것만)
    private fun getReviewWordsForToday(): List<WordEntry> {
        val today = getCurrentDate()
        return allWords.filter { word ->
            val data = getWordLearningData(word.kor)
            data.nextReviewDate == today && data.memorizationCount > 0 && data.memorizationCount < 4
        }
    }

    // 오늘의 신규 단어들 가져오기 (암기 완료하지 않은 것만)
    private fun getNewWordsForToday(): List<WordEntry> {
        val today = getCurrentDate()
        val result = allWords.filter { word ->
            val data = getWordLearningData(word.kor)
            data.dailyNewWordDate == today && data.memorizationCount == 0
        }
        
        Log.d("NewWords", "=== 오늘의 신규 단어 조회 ===")
        Log.d("NewWords", "오늘 날짜: $today")
        Log.d("NewWords", "전체 단어 수: ${allWords.size}")
        Log.d("NewWords", "오늘 배정된 단어 수: ${allWords.count { getWordLearningData(it.kor).dailyNewWordDate == today }}")
        Log.d("NewWords", "암기 완료하지 않은 신규 단어 수: ${result.size}")
        
        // 정확히 10개만 반환하도록 제한
        val limitedResult = result.take(10)
        Log.d("NewWords", "제한된 신규 단어 수: ${limitedResult.size}")
        Log.d("NewWords", "=== 신규 단어 조회 완료 ===")
        
        return limitedResult
    }

    // 단어 암기 처리
    private fun processWordMemorization(word: String, memorized: Boolean) {
        val data = getWordLearningData(word)
        val today = getCurrentDate()
        
        if (memorized) {
            // 암기 성공
            val newMemorizationCount = data.memorizationCount + 1
            val nextReviewInterval = if (newMemorizationCount <= reviewIntervals.size) {
                reviewIntervals[newMemorizationCount - 1]
            } else {
                reviewIntervals.last()
            }
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, nextReviewInterval)
            val nextReviewDate = dateFormat.format(calendar.time)
            
            val updatedData = data.copy(
                memorizationCount = newMemorizationCount,
                lastMemorizedDate = today,
                nextReviewDate = nextReviewDate
            )
            saveWordLearningData(updatedData)
            
            currentSessionWordsMemorized++
        } else {
            // 암기 실패 - 암기 횟수 초기화
            val updatedData = data.copy(
                memorizationCount = 0,
                nextReviewDate = getCurrentDate() // 다음날 복습
            )
            saveWordLearningData(updatedData)
        }
        
        saveStudyData()
        updateProgress() // 진행률 업데이트
        
        // 목표 달성 확인 추가
        checkGoalAchievement()
    }

    // 오늘의 단어들 업데이트
    private fun updateTodayWords() {
        Log.d("UpdateWords", "=== 오늘의 단어 업데이트 시작 ===")
        
        assignNewWordsForToday()
        
        todayNewWords = getNewWordsForToday()
        todayReviewWords = getReviewWordsForToday()
        
        Log.d("UpdateWords", "신규 단어 수: ${todayNewWords.size}")
        Log.d("UpdateWords", "복습 단어 수: ${todayReviewWords.size}")
        
        // 현재 모드에 따라 단어 목록 설정
        words = when (currentLearningMode) {
            LearningMode.NEW_WORDS -> todayNewWords
            LearningMode.REVIEW_WORDS -> todayReviewWords
        }
        
        Log.d("UpdateWords", "현재 모드: $currentLearningMode")
        Log.d("UpdateWords", "현재 단어 목록 크기: ${words.size}")
        
        // 현재 인덱스 조정
        if (currentIndex >= words.size) {
            currentIndex = if (words.isNotEmpty()) 0 else -1
        }
        
        Log.d("UpdateWords", "단어 목록 크기: ${words.size}, 현재 인덱스: $currentIndex")
        if (words.isNotEmpty()) {
            Log.d("UpdateWords", "현재 단어: ${words[currentIndex].kor}")
        }
        
        Log.d("UpdateWords", "현재 인덱스: $currentIndex")
        
        updateProgress()
        updateAll()
        
        Log.d("UpdateWords", "=== 오늘의 단어 업데이트 완료 ===")
    }
    
    // 목표 달성 확인
    private fun checkGoalAchievement() {
        val currentWords = when (currentLearningMode) {
            LearningMode.NEW_WORDS -> todayNewWords
            LearningMode.REVIEW_WORDS -> todayReviewWords
        }
        
        if (currentWords.isNotEmpty()) {
            // 현재 표시된 단어들 중에서 암기 완료된 단어 수 계산
            val completedWords = currentWords.count { word ->
                val data = getWordLearningData(word.kor)
                data.memorizationCount >= 1 // 최소 1번 이상 암기 완료
            }
            
            val totalWords = currentWords.size
            val modeText = when (currentLearningMode) {
                LearningMode.NEW_WORDS -> "신규 단어"
                LearningMode.REVIEW_WORDS -> "복습 단어"
            }
            
            Log.d("GoalAchievement", "목표 달성 확인 - 총 단어: $totalWords, 완료된 단어: $completedWords")
            
            // 모든 단어를 완료했을 때 축하 화면 표시
            if (completedWords >= totalWords && totalWords > 0) {
                Log.d("GoalAchievement", "🎉 모든 단어 암기 완료! 축하 화면 표시")
                val intent = Intent(this, CongratulationsActivity::class.java).apply {
                    putExtra("total_words", totalWords)
                    putExtra("memorized_words", completedWords)
                    putExtra("grade", currentGrade)
                    putExtra("learning_mode", if (currentLearningMode == LearningMode.NEW_WORDS) "new_words" else "review_words")
                    putExtra("today_new_words", todayNewWords.size)
                    putExtra("today_review_words", todayReviewWords.size)
                }
                startActivity(intent)
            }
        }
    }

    // 학습 모드 변경
    private fun switchLearningMode(mode: LearningMode) {
        currentLearningMode = mode
        currentIndex = 0
        isWordFront = true
        isSentenceFront = true
        currentSentenceIndex = 0
        updateTodayWords()
    }
    
    // 초기화 확인 다이얼로그
    private fun showResetConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ 학습 기록 초기화")
            .setMessage("모든 학습 기록이 삭제됩니다.\n정말 초기화하시겠습니까?")
            .setPositiveButton("초기화") { _, _ ->
                resetAllLearningData()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    // 모든 학습 데이터 초기화
    private fun resetAllLearningData() {
        Log.d("ResetData", "=== 학습 데이터 초기화 시작 ===")
        
        val today = getCurrentDate()
        Log.d("ResetData", "오늘 날짜: $today")
        
        // 어제 날짜 구하기
        val cal = java.util.Calendar.getInstance()
        cal.time = java.text.SimpleDateFormat("yyyy-MM-dd").parse(today)!!
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        val dailyNewWordsPrefs = getSharedPreferences("daily_new_words", Context.MODE_PRIVATE)
        dailyNewWordsPrefs.edit().putString("last_assigned_date_$currentGrade", yesterday).apply()
        Log.d("ResetData", "last_assigned_date를 어제로 설정: $yesterday")
        
        // 초기화 전 상태 확인
        val beforeResetNewWords = allWords.count { word ->
            val data = getWordLearningData(word.kor)
            data.dailyNewWordDate == today
        }
        val beforeResetCompleted = allWords.count { word ->
            val data = getWordLearningData(word.kor)
            data.dailyNewWordDate == today && data.memorizationCount > 0
        }
        Log.d("ResetData", "초기화 전 - 오늘 배정된 단어: ${beforeResetNewWords}개")
        Log.d("ResetData", "초기화 전 - 암기 완료된 단어: ${beforeResetCompleted}개")
        
        val memorizationPrefs = getSharedPreferences("memorization", Context.MODE_PRIVATE)
        val studyDataPrefs = getSharedPreferences("study_data", Context.MODE_PRIVATE)
        
        Log.d("ResetData", "memorization SharedPreferences 초기화")
        memorizationPrefs.edit().clear().apply()
        Log.d("ResetData", "study_data SharedPreferences 초기화")
        studyDataPrefs.edit().clear().apply()
        
        // word_learning_data에서 암기 기록만 삭제하고 배정 정보는 유지
        Log.d("ResetData", "word_learning_data 선택적 초기화 시작")
        // 오늘 날짜로 배정된 모든 단어의 배정 정보와 암기 기록 초기화
        var resetCount = 0
        allWords.forEach { word ->
            val data = getWordLearningData(word.kor)
            if (data.dailyNewWordDate == today) {
                val resetData = data.copy(
                    dailyNewWordDate = null, // 배정 정보도 초기화!
                    memorizationCount = 0,
                    lastMemorizedDate = null,
                    nextReviewDate = null
                )
                saveWordLearningData(resetData)
                resetCount++
            }
        }
        Log.d("ResetData", "오늘 날짜로 배정된 단어 초기화 수: $resetCount")
        
        // 초기화 후 상태 확인
        val afterResetNewWords = allWords.count { word ->
            val data = getWordLearningData(word.kor)
            data.dailyNewWordDate == today
        }
        val afterResetCompleted = allWords.count { word ->
            val data = getWordLearningData(word.kor)
            data.dailyNewWordDate == today && data.memorizationCount > 0
        }
        Log.d("ResetData", "초기화 후 - 오늘 배정된 단어: ${afterResetNewWords}개")
        Log.d("ResetData", "초기화 후 - 암기 완료된 단어: ${afterResetCompleted}개")
        
        Log.d("ResetData", "=== 학습 데이터 초기화 완료 ===")
        
        // 앱 재시작
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun updateProgress() {
        val modeText = when (currentLearningMode) {
            LearningMode.NEW_WORDS -> "신규 단어"
            LearningMode.REVIEW_WORDS -> "복습 단어"
        }
        
        val today = getCurrentDate()
        
        when (currentLearningMode) {
            LearningMode.NEW_WORDS -> {
                // 현재 표시된 신규 단어 목록 기준으로 진행률 계산
                val totalWords = todayNewWords.size
                val completedWords = todayNewWords.count { word ->
                    val data = getWordLearningData(word.kor)
                    data.memorizationCount > 0
                }
                
                Log.d("Progress", "신규 단어 진행률 - 총 단어: $totalWords, 완료: $completedWords")
                tvProgress.text = "📚 $modeText: $completedWords/$totalWords"
            }
            LearningMode.REVIEW_WORDS -> {
                // 현재 표시된 복습 단어 목록 기준으로 진행률 계산
                val totalWords = todayReviewWords.size
                val completedWords = todayReviewWords.count { word ->
                    val data = getWordLearningData(word.kor)
                    data.memorizationCount >= 4
                }
                
                Log.d("Progress", "복습 단어 진행률 - 총 단어: $totalWords, 완료: $completedWords")
                tvProgress.text = "📚 $modeText: $completedWords/$totalWords"
            }
        }
    }

    // 다음 단어로 이동하는 함수 (암기 완료한 단어 건너뛰기)
    private fun moveToNextWord() {
        if (words.isNotEmpty()) {
            var nextIndex = currentIndex
            var attempts = 0
            val maxAttempts = words.size * 2 // 무한 루프 방지
            
            do {
                nextIndex = if (nextIndex < words.size - 1) {
                    nextIndex + 1
                } else {
                    0 // 마지막 단어에서 첫 번째 단어로
                }
                attempts++
                
                // 현재 인덱스의 단어가 암기 완료되지 않았는지 확인
                val currentWord = words.getOrNull(nextIndex)
                if (currentWord != null) {
                    val data = getWordLearningData(currentWord.kor)
                    if (data.memorizationCount == 0) {
                        // 암기 완료하지 않은 단어를 찾았음
                        currentIndex = nextIndex
                        break
                    }
                }
            } while (attempts < maxAttempts && nextIndex != currentIndex)
            
            // 모든 단어가 암기 완료된 경우 첫 번째 단어로
            if (attempts >= maxAttempts) {
                currentIndex = 0
            }
            
            isWordFront = true
            isSentenceFront = true
            currentSentenceIndex = 0
            updateAll(withAnim = true)
            
            // 단어 변경 시간 기록
            lastWordChangeTime = System.currentTimeMillis()
            currentSessionWordsViewed++
            saveStudyData()
            
            val currentWord = words.getOrNull(currentIndex)
            Log.d("WordNavigation", "다음 단어로 이동: 인덱스 $currentIndex, 단어: ${currentWord?.kor ?: "없음"}, 암기 횟수: ${currentWord?.let { getWordLearningData(it.kor).memorizationCount } ?: 0}")
        }
    }

    // 이전 단어로 이동하는 함수 (암기 완료한 단어 건너뛰기)
    private fun moveToPrevWord() {
        if (words.isNotEmpty()) {
            var prevIndex = currentIndex
            var attempts = 0
            val maxAttempts = words.size * 2 // 무한 루프 방지
            
            do {
                prevIndex = if (prevIndex > 0) {
                    prevIndex - 1
                } else {
                    words.size - 1 // 첫 번째 단어에서 마지막 단어로
                }
                attempts++
                
                // 현재 인덱스의 단어가 암기 완료되지 않았는지 확인
                val currentWord = words.getOrNull(prevIndex)
                if (currentWord != null) {
                    val data = getWordLearningData(currentWord.kor)
                    if (data.memorizationCount == 0) {
                        // 암기 완료하지 않은 단어를 찾았음
                        currentIndex = prevIndex
                        break
                    }
                }
            } while (attempts < maxAttempts && prevIndex != currentIndex)
            
            // 모든 단어가 암기 완료된 경우 첫 번째 단어로
            if (attempts >= maxAttempts) {
                currentIndex = 0
            }
            
            isWordFront = true
            isSentenceFront = true
            currentSentenceIndex = 0
            updateAll(withAnim = true)
            
            // 단어 변경 시간 기록
            lastWordChangeTime = System.currentTimeMillis()
            currentSessionWordsViewed++
            saveStudyData()
            
            val currentWord = words.getOrNull(currentIndex)
            Log.d("WordNavigation", "이전 단어로 이동: 인덱스 $currentIndex, 단어: ${currentWord?.kor ?: "없음"}, 암기 횟수: ${currentWord?.let { getWordLearningData(it.kor).memorizationCount } ?: 0}")
        }
    }

    // 학습 데이터 저장
    private fun saveStudyData() {
        val currentDate = dateFormat.format(Date())
        val prefs = getSharedPreferences("study_data", Context.MODE_PRIVATE)
        
        val existingData = getStudyDataForDate(currentDate)
        existingData.totalStudyTime += (System.currentTimeMillis() - sessionStartTime) / 1000
        existingData.totalTtsCount += currentSessionTtsCount
        existingData.totalWordsViewed += currentSessionWordsViewed
        existingData.totalWordsMemorized += currentSessionWordsMemorized
        
        val json = gson.toJson(existingData)
        prefs.edit().putString("daily_data_$currentDate", json).apply()
        
        // 세션 데이터 초기화
        sessionStartTime = System.currentTimeMillis()
        currentSessionTtsCount = 0
        currentSessionWordsViewed = 0
        currentSessionWordsMemorized = 0
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

    // 사이드 메뉴 토글
    private fun toggleSideMenu() {
        if (isSideMenuOpen) {
            // 메뉴 닫기
            sideMenu.animate()
                .translationX(-1000f)
                .setDuration(300)
                .withEndAction {
                    sideMenuOverlay.visibility = View.GONE
                }
                .start()
        } else {
            // 메뉴 열기
            sideMenuOverlay.visibility = View.VISIBLE
            sideMenu.animate()
                .translationX(0f)
                .setDuration(300)
                .start()
        }
        isSideMenuOpen = !isSideMenuOpen
    }

    // --- UI Update Functions ---
    private fun updateWordCard(withAnim: Boolean = false) {
        if (words.isNotEmpty()) {
            val entry = words[currentIndex]
            val text = if (isWordFront) entry.kor else entry.eng
            if (withAnim) {
                slideAnim(cardView, text)
            } else {
                tvWord.text = text
            }
        } else {
            tvWord.text = "단어 없음"
        }
    }
    private fun updateSentenceCard(withAnim: Boolean = false) {
        val entry = words.getOrNull(currentIndex)
        if (entry != null && entry.sentences.isNotEmpty()) {
            val idx = currentSentenceIndex.coerceIn(0, entry.sentences.size - 1)
            val (kor, eng) = entry.sentences[idx]
            val text = if (isSentenceFront) kor else eng
            if (withAnim) {
                slideAnim(sentenceCard, text, isSentence = true)
            } else {
                tvSentence.text = text
            }
        } else {
            tvSentence.text = "예문 없음"
        }
    }
    private fun updateAll(withAnim: Boolean = false) {
        updateWordCard(withAnim)
        updateSentenceCard(withAnim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            prefs.edit().putBoolean("isFirstRun", false).apply()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        setContentView(R.layout.activity_main)

        currentGrade = prefs.getString("grade", "high") ?: "high"
        allWords = loadWordsFromAssets(currentGrade)
        
        // 학습 세션 시작
        sessionStartTime = System.currentTimeMillis()
        lastWordChangeTime = sessionStartTime

        tvWord = findViewById(R.id.tvWord)
        cardView = findViewById(R.id.cardView)
        sentenceCard = findViewById(R.id.sentenceCard)
        tvProgress = findViewById(R.id.tvProgress)
        btnMemorized = findViewById(R.id.btnMemorized)
        btnNeedMore = findViewById(R.id.btnNeedMore)
        btnFilter = findViewById(R.id.btnFilter)
        btnMenu = findViewById(R.id.btnMenu)
        sideMenu = findViewById(R.id.sideMenu)
        sideMenuOverlay = findViewById(R.id.sideMenuOverlay)
        val btnPrev = findViewById<MaterialCardView>(R.id.btnPrev)
        val btnNext = findViewById<MaterialCardView>(R.id.btnNext)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        tvSentence = findViewById(R.id.tvSentence)
        val btnSentencePrev = findViewById<MaterialCardView>(R.id.btnSentencePrev)
        val btnSentenceNext = findViewById<MaterialCardView>(R.id.btnSentenceNext)
        btnMenuCategory = findViewById(R.id.btnMenuCategory)
        btnMenuCalendar = findViewById(R.id.btnMenuCalendar)
        val btnMenuReset = findViewById<MaterialButton>(R.id.btnMenuReset)

        tts = TextToSpeech(this, this)

        // 초기 학습 모드 설정
        updateTodayWords()
        btnFilter.text = "복습 모드로"
        updateProgress()
        setupSideMenu()

        // 메뉴 버튼 클릭 리스너
        btnMenu.setOnClickListener {
            toggleSideMenu()
        }

        // 오버레이 클릭 시 메뉴 닫기
        sideMenuOverlay.setOnClickListener {
            toggleSideMenu()
        }
        btnMenuCategory.setOnClickListener {
            toggleSideMenu()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnMenuCalendar.setOnClickListener {
            toggleSideMenu()
            startActivity(Intent(this, CalendarActivity::class.java))
        }
        
        btnMenuReset.setOnClickListener {
            toggleSideMenu()
            showResetConfirmationDialog()
        }

        // 암기 버튼 클릭 리스너
        btnMemorized.setOnClickListener {
            val entry = words.getOrNull(currentIndex)
            if (entry != null) {
                processWordMemorization(entry.kor, true)
                moveToNextWord()
            }
        }

        btnNeedMore.setOnClickListener {
            val entry = words.getOrNull(currentIndex)
            if (entry != null) {
                processWordMemorization(entry.kor, false)
                moveToNextWord()
            }
        }

        btnFilter.setOnClickListener {
            // 학습 모드 전환
            currentLearningMode = if (currentLearningMode == LearningMode.NEW_WORDS) {
                LearningMode.REVIEW_WORDS
            } else {
                LearningMode.NEW_WORDS
            }
            
            btnFilter.text = when (currentLearningMode) {
                LearningMode.NEW_WORDS -> "복습 모드로"
                LearningMode.REVIEW_WORDS -> "신규 모드로"
            }
            
            switchLearningMode(currentLearningMode)
        }

        // 단어 카드 제스처
        wordGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 0.3초 후 TTS 예약
                wordTtsRunnable?.let { handler.removeCallbacks(it) }
                wordTtsRunnable = Runnable {
                    val entry = words.getOrNull(currentIndex)
                    if (entry != null) {
                        tts?.speak(entry.eng, TextToSpeech.QUEUE_FLUSH, null, null)
                        currentSessionTtsCount++
                        saveStudyData()
                    }
                }
                handler.postDelayed(wordTtsRunnable!!, 300)
                return true
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 예약된 TTS 취소, 플립만
                wordTtsRunnable?.let { handler.removeCallbacks(it) }
                flipCard(cardView, isWord = true)
                return true
            }
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        // 오른쪽 → 이전 단어
                        moveToPrevWord()
                    } else {
                        // 왼쪽 → 다음 단어
                        moveToNextWord()
                    }
                    return true
                }
                return false
            }
        })
        cardView.setOnTouchListener { _, event ->
            wordGestureDetector.onTouchEvent(event)
            true
        }

        // 예문 카드 제스처
        sentenceGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 0.3초 후 TTS 예약
                sentenceTtsRunnable?.let { handler.removeCallbacks(it) }
                sentenceTtsRunnable = Runnable {
                    val entry = words.getOrNull(currentIndex)
                    if (entry != null && entry.sentences.isNotEmpty()) {
                        val idx = currentSentenceIndex.coerceIn(0, entry.sentences.size - 1)
                        val eng = entry.sentences[idx].second
                        tts?.speak(eng, TextToSpeech.QUEUE_FLUSH, null, null)
                        currentSessionTtsCount++
                        saveStudyData()
                    }
                }
                handler.postDelayed(sentenceTtsRunnable!!, 300)
                return true
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // 예약된 TTS 취소, 플립만
                sentenceTtsRunnable?.let { handler.removeCallbacks(it) }
                flipCard(sentenceCard, isWord = false)
                return true
            }
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    val entry = words.getOrNull(currentIndex)
                    if (diffX > 0) {
                        // 오른쪽 → 이전 예문
                        if (entry != null && currentSentenceIndex > 0) {
                            currentSentenceIndex--
                            isSentenceFront = true
                            updateSentenceCard(withAnim = true)
                        }
                    } else {
                        // 왼쪽 → 다음 예문
                        if (entry != null && currentSentenceIndex < entry.sentences.size - 1) {
                            currentSentenceIndex++
                            isSentenceFront = true
                            updateSentenceCard(withAnim = true)
                        }
                    }
                    return true
                }
                return false
            }
        })
        sentenceCard.setOnTouchListener { _, event ->
            sentenceGestureDetector.onTouchEvent(event)
            true
        }

        btnPrev.setOnClickListener {
            moveToPrevWord()
        }
        btnNext.setOnClickListener {
            moveToNextWord()
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnSentencePrev.setOnClickListener {
            val entry = words.getOrNull(currentIndex)
            if (entry != null && currentSentenceIndex > 0) {
                currentSentenceIndex--
                isSentenceFront = true
                updateSentenceCard(withAnim = true)
            }
        }
        btnSentenceNext.setOnClickListener {
            val entry = words.getOrNull(currentIndex)
            if (entry != null && currentSentenceIndex < entry.sentences.size - 1) {
                currentSentenceIndex++
                isSentenceFront = true
                updateSentenceCard(withAnim = true)
            }
        }
    }

    private fun setupSideMenu() {
        // 사이드 메뉴 내용은 버튼 클릭 시 처리
    }

    private fun flipCard(view: View, isWord: Boolean) {
        val scale = applicationContext.resources.displayMetrics.density
        view.cameraDistance = 8000 * scale
        view.animate()
            .rotationYBy(90f)
            .setDuration(150)
            .withEndAction {
                if (isWord) {
                    isWordFront = !isWordFront
                    val entry = words.getOrNull(currentIndex)
                    tvWord.text = if (isWordFront) entry?.kor ?: "단어 없음" else entry?.eng ?: "단어 없음"
                } else {
                    isSentenceFront = !isSentenceFront
                    val entry = words.getOrNull(currentIndex)
                    val idx = currentSentenceIndex.coerceIn(0, entry?.sentences?.size?.minus(1) ?: 0)
                    if (entry != null && entry.sentences.isNotEmpty()) {
                        val (kor, eng) = entry.sentences[idx]
                        val text = if (isSentenceFront) kor else eng
                        tvSentence.text = text
                    } else {
                        tvSentence.text = "예문 없음"
                    }
                }
                view.rotationY = -90f
                view.animate()
                    .rotationYBy(90f)
                    .setDuration(150)
                    .start()
            }.start()
    }

    private fun slideAnim(view: View, text: String, isSentence: Boolean = false) {
        view.animate().translationX(300f).alpha(0f).setDuration(120).withEndAction {
            if (isSentence) {
                tvSentence.text = text
            } else {
                tvWord.text = text
            }
            view.translationX = -300f
            view.animate().translationX(0f).alpha(1f).setDuration(120).start()
        }.start()
    }

    override fun onResume() {
        super.onResume()
        // 세팅에서 돌아왔을 때 학년 반영
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val newGrade = prefs.getString("grade", "high") ?: "high"
        if (newGrade != currentGrade) {
            currentGrade = newGrade
            allWords = loadWordsFromAssets(currentGrade)
            currentIndex = 0
            isWordFront = true
            isSentenceFront = true
            currentSentenceIndex = 0
            updateTodayWords()
            updateProgress()
        }
    }

    override fun onPause() {
        super.onPause()
        // 앱이 백그라운드로 갈 때 학습 데이터 저장
        saveStudyData()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.95f)
        }
    }

    private fun loadWordsFromAssets(grade: String): List<WordEntry> {
        val result = mutableListOf<WordEntry>()
        val fileName = when (grade) {
            "elementary_low" -> "elementary_low.csv"
            "elementary_high" -> "elementary_high.csv"
            "middle" -> "middle.csv"
            else -> "high.csv"
        }
        try {
            val inputStream = assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val header = reader.readLine() // 헤더
            reader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size >= 8) {
                    val kor = parts[0].trim()
                    val eng = parts[1].trim()
                    val sentences = listOf(
                        Pair(parts[2].trim(), parts[3].trim()),
                        Pair(parts[4].trim(), parts[5].trim()),
                        Pair(parts[6].trim(), parts[7].trim())
                    )
                    result.add(WordEntry(kor, eng, sentences))
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}