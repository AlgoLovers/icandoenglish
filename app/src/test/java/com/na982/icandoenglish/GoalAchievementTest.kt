package com.na982.icandoenglish

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import java.text.SimpleDateFormat
import java.util.*

/**
 * 목표 달성 확인 로직에 대한 포괄적인 테스트
 * 
 * 이 테스트는 다음 기능들을 검증합니다:
 * - 신규 단어 완료 확인
 * - 복습 단어 완료 확인
 * - 진행률 계산
 * - 목표 달성 조건 검증
 */
class GoalAchievementTest {
    private lateinit var testWords: List<WordEntry>
    private lateinit var wordLearningDataMap: MutableMap<String, WordLearningData>
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())

    @Before
    fun setUp() {
        // 테스트용 단어 목록 생성
        testWords = listOf(
            WordEntry("환경", "environment", listOf(Pair("환경을 보호해야 한다", "We must protect the environment"))),
            WordEntry("회사", "company", listOf(Pair("회사에 다닌다", "I work at a company"))),
            WordEntry("정부", "government", listOf(Pair("정부의 정책", "Government policy"))),
            WordEntry("얼굴", "face", listOf(Pair("얼굴이 예쁘다", "She has a beautiful face"))),
            WordEntry("경주", "race", listOf(Pair("경주를 한다", "I run a race"))),
            WordEntry("식물", "plant", listOf(Pair("식물을 키운다", "I grow plants"))),
            WordEntry("화재", "fire", listOf(Pair("화재가 발생했다", "A fire broke out"))),
            WordEntry("문제", "problem", listOf(Pair("문제를 해결한다", "I solve the problem"))),
            WordEntry("상태", "condition", listOf(Pair("상태가 좋다", "The condition is good"))),
            WordEntry("원인", "cause", listOf(Pair("원인을 찾는다", "I find the cause")))
        )

        // 테스트용 학습 데이터 초기화
        wordLearningDataMap = mutableMapOf()
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = WordLearningData(
                word = word.kor,
                grade = "high",
                memorizationCount = 0,
                dailyNewWordDate = today
            )
        }
    }

    /**
     * 신규 단어 완료 확인 테스트
     */
    @Test
    fun `testNewWordCompletion`() {
        // Given - 일부 단어를 암기 완료 상태로 설정
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 1)

        // When - 완료된 단어 수 계산
        val completedWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(3, completedWords)
        assertEquals(10, testWords.size)
        assertFalse(isAllWordsCompleted()) // 아직 모든 단어가 완료되지 않음
    }

    /**
     * 모든 신규 단어 완료 확인 테스트
     */
    @Test
    fun `testAllNewWordsCompleted`() {
        // Given - 모든 단어를 암기 완료 상태로 설정
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 1)
        }

        // When - 완료된 단어 수 계산
        val completedWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(10, completedWords)
        assertEquals(10, testWords.size)
        assertTrue(isAllWordsCompleted()) // 모든 단어가 완료됨
    }

    /**
     * 복습 단어 완료 확인 테스트
     */
    @Test
    fun `testReviewWordCompletion`() {
        // Given - 복습 단어들을 설정 (memorizationCount >= 4가 완료 조건)
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 4)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 3)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 4)

        // When - 완료된 복습 단어 수 계산
        val completedReviewWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 4
        }

        // Then
        assertEquals(2, completedReviewWords)
        assertFalse(isAllReviewWordsCompleted()) // 아직 모든 복습 단어가 완료되지 않음
    }

    /**
     * 모든 복습 단어 완료 확인 테스트
     */
    @Test
    fun `testAllReviewWordsCompleted`() {
        // Given - 모든 단어를 복습 완료 상태로 설정
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 4)
        }

        // When - 완료된 복습 단어 수 계산
        val completedReviewWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 4
        }

        // Then
        assertEquals(10, completedReviewWords)
        assertTrue(isAllReviewWordsCompleted()) // 모든 복습 단어가 완료됨
    }

    /**
     * 진행률 계산 테스트
     */
    @Test
    fun `testProgressCalculation`() {
        // Given - 다양한 암기 상태의 단어들
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 0)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["얼굴"] = wordLearningDataMap["얼굴"]!!.copy(memorizationCount = 0)
        wordLearningDataMap["경주"] = wordLearningDataMap["경주"]!!.copy(memorizationCount = 1)

        // When - 진행률 계산
        val totalWords = testWords.size
        val completedWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }
        val progressPercentage = (completedWords.toFloat() / totalWords) * 100

        // Then
        assertEquals(10, totalWords)
        assertEquals(3, completedWords)
        assertEquals(30.0f, progressPercentage, 0.01f)
    }

    /**
     * 빈 단어 목록 처리 테스트
     */
    @Test
    fun `testEmptyWordListHandling`() {
        // Given - 빈 단어 목록
        val emptyWords = emptyList<WordEntry>()

        // When - 완료된 단어 수 계산
        val completedWords = emptyWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(0, completedWords)
        assertTrue(isAllWordsCompleted(emptyWords)) // 빈 목록은 완료된 것으로 간주
    }

    /**
     * 단일 단어 완료 확인 테스트
     */
    @Test
    fun `testSingleWordCompletion`() {
        // Given - 단일 단어 목록
        val singleWord = listOf(WordEntry("테스트", "test", emptyList()))
        val singleWordData = mutableMapOf("테스트" to WordLearningData("테스트", "high", 1))

        // When - 완료된 단어 수 계산
        val completedWords = singleWord.count { word ->
            val data = singleWordData[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(1, completedWords)
        assertEquals(1, singleWord.size)
        assertTrue(isAllWordsCompleted(singleWord, singleWordData))
    }

    /**
     * 암기 횟수 경계값 테스트
     */
    @Test
    fun `testMemorizationCountBoundary`() {
        // Given - 다양한 암기 횟수의 단어들
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 0)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["얼굴"] = wordLearningDataMap["얼굴"]!!.copy(memorizationCount = 3)
        wordLearningDataMap["경주"] = wordLearningDataMap["경주"]!!.copy(memorizationCount = 4)

        // When - 완료된 단어 수 계산 (memorizationCount >= 1)
        val completedWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(4, completedWords) // 1, 2, 3, 4번 암기한 단어들만 완료로 간주
    }

    /**
     * 복습 단어 암기 횟수 경계값 테스트
     */
    @Test
    fun `testReviewWordMemorizationCountBoundary`() {
        // Given - 다양한 암기 횟수의 복습 단어들
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 3)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 4)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 5)

        // When - 완료된 복습 단어 수 계산 (memorizationCount >= 4)
        val completedReviewWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 4
        }

        // Then
        assertEquals(2, completedReviewWords) // 4, 5번 암기한 단어들만 완료로 간주
    }

    /**
     * 목표 달성 조건 검증 테스트
     */
    @Test
    fun `testGoalAchievementConditions`() {
        // Given - 일부 단어만 완료된 상태
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 1)

        // When & Then - 모든 단어가 완료되지 않았으므로 목표 달성하지 않음
        assertFalse(isAllWordsCompleted())

        // Given - 모든 단어를 완료 상태로 설정
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 1)
        }

        // When & Then - 모든 단어가 완료되었으므로 목표 달성
        assertTrue(isAllWordsCompleted())
    }

    /**
     * 진행률 정확성 테스트
     */
    @Test
    fun `testProgressAccuracy`() {
        // Given - 정확히 절반의 단어만 완료
        for (i in 0 until testWords.size / 2) {
            wordLearningDataMap[testWords[i].kor] = wordLearningDataMap[testWords[i].kor]!!.copy(memorizationCount = 1)
        }

        // When - 진행률 계산
        val totalWords = testWords.size
        val completedWords = testWords.count { word ->
            val data = wordLearningDataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }

        // Then
        assertEquals(10, totalWords)
        assertEquals(5, completedWords)
        assertEquals(50.0f, (completedWords.toFloat() / totalWords) * 100, 0.01f)
    }

    // 헬퍼 함수들
    private fun isAllWordsCompleted(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Boolean {
        if (words.isEmpty()) return true
        return words.all { word ->
            val data = dataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }
    }

    private fun isAllReviewWordsCompleted(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Boolean {
        if (words.isEmpty()) return true
        return words.all { word ->
            val data = dataMap[word.kor]
            data?.memorizationCount ?: 0 >= 4
        }
    }
} 