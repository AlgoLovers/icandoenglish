package com.na982.icandoenglish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import android.os.Handler
import android.os.Looper

// 데이터 클래스 정의
data class WordEntry(
    val kor: String,
    val eng: String,
    val sentences: List<Pair<String, String>>
)

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var words: List<WordEntry> = emptyList()
    private var currentIndex = 0
    private var isWordFront = true
    private var isSentenceFront = true
    private var currentGrade = "high"
    private var currentSentenceIndex = 0
    private var tts: TextToSpeech? = null
    private lateinit var cardView: MaterialCardView
    private lateinit var tvWord: TextView
    private lateinit var tvSentenceKor: TextView
    private lateinit var tvSentenceEng: TextView
    private lateinit var sentenceCard: MaterialCardView
    private lateinit var wordGestureDetector: GestureDetector
    private lateinit var sentenceGestureDetector: GestureDetector
    private val handler = Handler(Looper.getMainLooper())
    private var wordTtsRunnable: Runnable? = null
    private var sentenceTtsRunnable: Runnable? = null

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
            val textKor = if (isSentenceFront) kor else eng
            val textEng = if (isSentenceFront) eng else kor
            if (withAnim) {
                slideAnim(sentenceCard, textKor, isSentence = true, textEng = textEng)
            } else {
                tvSentenceKor.text = textKor
                tvSentenceEng.text = textEng
            }
        } else {
            tvSentenceKor.text = "예문 없음"
            tvSentenceEng.text = "No example"
        }
    }
    private fun updateAll(withAnim: Boolean = false) {
        updateWordCard(withAnim)
        updateSentenceCard(withAnim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        currentGrade = prefs.getString("grade", "high") ?: "high"
        words = loadWordsFromAssets(currentGrade)

        tvWord = findViewById(R.id.tvWord)
        cardView = findViewById(R.id.cardView)
        sentenceCard = findViewById(R.id.sentenceCard)
        val btnPrev = findViewById<MaterialButton>(R.id.btnPrev)
        val btnNext = findViewById<MaterialButton>(R.id.btnNext)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        tvSentenceKor = findViewById(R.id.tvSentenceKor)
        tvSentenceEng = findViewById(R.id.tvSentenceEng)
        val btnSentencePrev = findViewById<MaterialButton>(R.id.btnSentencePrev)
        val btnSentenceNext = findViewById<MaterialButton>(R.id.btnSentenceNext)

        tts = TextToSpeech(this, this)

        updateAll()

        // 단어 카드 제스처
        wordGestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // 0.3초 후 TTS 예약
                wordTtsRunnable?.let { handler.removeCallbacks(it) }
                wordTtsRunnable = Runnable {
                    val entry = words.getOrNull(currentIndex)
                    if (entry != null) {
                        tts?.speak(entry.eng, TextToSpeech.QUEUE_FLUSH, null, null)
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
                        if (currentIndex > 0) {
                            currentIndex--
                            isWordFront = true
                            isSentenceFront = true
                            currentSentenceIndex = 0
                            updateAll(withAnim = true)
                        }
                    } else {
                        // 왼쪽 → 다음 단어
                        if (currentIndex < words.size - 1) {
                            currentIndex++
                            isWordFront = true
                            isSentenceFront = true
                            currentSentenceIndex = 0
                            updateAll(withAnim = true)
                        }
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
            if (currentIndex > 0) {
                currentIndex--
                isWordFront = true
                isSentenceFront = true
                currentSentenceIndex = 0
                updateAll(withAnim = true)
            }
        }
        btnNext.setOnClickListener {
            if (currentIndex < words.size - 1) {
                currentIndex++
                isWordFront = true
                isSentenceFront = true
                currentSentenceIndex = 0
                updateAll(withAnim = true)
            }
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
                        val textKor = if (isSentenceFront) kor else eng
                        val textEng = if (isSentenceFront) eng else kor
                        tvSentenceKor.text = textKor
                        tvSentenceEng.text = textEng
                    } else {
                        tvSentenceKor.text = "예문 없음"
                        tvSentenceEng.text = "No example"
                    }
                }
                view.rotationY = -90f
                view.animate()
                    .rotationYBy(90f)
                    .setDuration(150)
                    .start()
            }.start()
    }

    private fun slideAnim(view: View, text: String, isSentence: Boolean = false, textEng: String = "") {
        view.animate().translationX(300f).alpha(0f).setDuration(120).withEndAction {
            if (isSentence) {
                tvSentenceKor.text = text
                tvSentenceEng.text = textEng
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
            words = loadWordsFromAssets(currentGrade)
            currentIndex = 0
            isWordFront = true
            isSentenceFront = true
            currentSentenceIndex = 0
            updateAll()
        }
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