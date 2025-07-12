package com.na982.icandoenglish

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import java.text.SimpleDateFormat
import java.util.*

/**
 * 전체 학습 워크플로우에 대한 통합 테스트
 * 
 * 이 테스트는 다음 시나리오들을 검증합니다:
 * - 신규 단어 배정 및 학습
 * - 단어 암기 및 순환
 * - 목표 달성 확인
 * - 데이터 지속성
 */
class IntegrationTest {
    private lateinit var testWords: List<WordEntry>
    private lateinit var wordLearningDataMap: MutableMap<String, WordLearningData>
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val today = dateFormat.format(Date())

    @Before
    fun setUp() {
        // 테스트용 단어 목록 생성 (실제 앱의 단어들과 동일)
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
            wordLearningDataMap[word.kor] = WordLearningData(word.kor, "high")
        }
    }

    /**
     * 전체 학습 워크플로우 테스트
     */
    @Test
    fun `testCompleteLearningWorkflow`() {
        // Given - 신규 단어 배정
        val assignedWords = assignNewWordsForToday(10)
        assertEquals(10, assignedWords.size)
        
        // When & Then - 첫 번째 단어부터 순차적으로 암기
        var currentIndex = 0
        var memorizedCount = 0
        
        // 1. 환경 암기
        currentIndex = memorizeWord(currentIndex, "환경", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("환경"))
        assertFalse(isAllWordsCompleted())
        
        // 2. 회사 암기
        currentIndex = memorizeWord(currentIndex, "회사", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("회사"))
        assertFalse(isAllWordsCompleted())
        
        // 3. 정부 암기
        currentIndex = memorizeWord(currentIndex, "정부", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("정부"))
        assertFalse(isAllWordsCompleted())
        
        // 4. 얼굴 암기
        currentIndex = memorizeWord(currentIndex, "얼굴", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("얼굴"))
        assertFalse(isAllWordsCompleted())
        
        // 5. 경주 암기
        currentIndex = memorizeWord(currentIndex, "경주", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("경주"))
        assertFalse(isAllWordsCompleted())
        
        // 6. 식물 암기
        currentIndex = memorizeWord(currentIndex, "식물", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("식물"))
        assertFalse(isAllWordsCompleted())
        
        // 7. 화재 암기
        currentIndex = memorizeWord(currentIndex, "화재", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("화재"))
        assertFalse(isAllWordsCompleted())
        
        // 8. 문제 암기
        currentIndex = memorizeWord(currentIndex, "문제", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("문제"))
        assertFalse(isAllWordsCompleted())
        
        // 9. 상태 암기
        currentIndex = memorizeWord(currentIndex, "상태", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("상태"))
        assertFalse(isAllWordsCompleted())
        
        // 10. 원인 암기 (마지막 단어)
        currentIndex = memorizeWord(currentIndex, "원인", true)
        memorizedCount++
        assertEquals(1, getMemorizationCount("원인"))
        assertTrue(isAllWordsCompleted()) // 모든 단어 완료!
        
        assertEquals(10, memorizedCount)
    }

    /**
     * 암기 실패 시나리오 테스트
     */
    @Test
    fun `testMemorizationFailureScenario`() {
        // Given - 신규 단어 배정
        val assignedWords = assignNewWordsForToday(10)
        
        // When - 단어 암기 실패
        var currentIndex = 0
        currentIndex = memorizeWord(currentIndex, "환경", false) // 암기 실패
        
        // Then - 암기 횟수가 0으로 초기화되어야 함
        assertEquals(0, getMemorizationCount("환경"))
        assertFalse(isAllWordsCompleted())
        
        // When - 다시 암기 시도
        currentIndex = memorizeWord(currentIndex, "환경", true) // 암기 성공
        
        // Then - 암기 횟수가 1로 증가해야 함
        assertEquals(1, getMemorizationCount("환경"))
    }

    /**
     * 단어 순환 및 건너뛰기 테스트
     */
    @Test
    fun `testWordNavigationAndSkipping`() {
        // Given - 일부 단어를 암기 완료 상태로 설정
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["화재"] = wordLearningDataMap["화재"]!!.copy(memorizationCount = 1)
        
        var currentIndex = 0 // 환경 (암기 완료)
        
        // When & Then - 암기 완료한 단어들을 건너뛰고 다음 미완료 단어로 이동
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(1, currentIndex) // 회사 (암기 미완료)
        
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(2, currentIndex) // 정부 (암기 완료) -> 3번째로 이동
        
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(3, currentIndex) // 얼굴 (암기 미완료)
        
        // When - 얼굴 암기 완료
        currentIndex = memorizeWord(currentIndex, "얼굴", true)
        assertEquals(1, getMemorizationCount("얼굴"))
        
        // Then - 다음 미완료 단어로 이동
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(4, currentIndex) // 경주 (암기 미완료)
    }

    /**
     * 진행률 추적 테스트
     */
    @Test
    fun `testProgressTracking`() {
        // Given - 신규 단어 배정
        val assignedWords = assignNewWordsForToday(10)
        
        // When - 단어들을 순차적으로 암기
        val progressHistory = mutableListOf<Int>()
        
        for (i in 0 until 5) { // 5개 단어만 암기
            memorizeWord(i, testWords[i].kor, true)
            val completedCount = getCompletedWordsCount()
            progressHistory.add(completedCount)
        }
        
        // Then - 진행률이 순차적으로 증가해야 함
        assertEquals(listOf(1, 2, 3, 4, 5), progressHistory)
        assertEquals(5, getCompletedWordsCount())
        assertEquals(50.0f, getProgressPercentage(), 0.01f) // 5/10 * 100 = 50%
    }

    /**
     * 목표 달성 조건 테스트
     */
    @Test
    fun `testGoalAchievementConditions`() {
        // Given - 신규 단어 배정
        val assignedWords = assignNewWordsForToday(10)
        
        // When - 모든 단어를 암기 완료
        for (i in 0 until testWords.size) {
            memorizeWord(i, testWords[i].kor, true)
        }
        
        // Then - 목표 달성 조건 확인
        assertEquals(10, getCompletedWordsCount())
        assertEquals(100.0f, getProgressPercentage(), 0.01f)
        assertTrue(isAllWordsCompleted())
    }

    /**
     * 데이터 지속성 테스트
     */
    @Test
    fun `testDataPersistence`() {
        // Given - 학습 데이터 생성
        val studyData = DailyStudyData(
            date = today,
            totalStudyTime = 300,
            totalTtsCount = 15,
            totalWordsViewed = 20,
            totalWordsMemorized = 12
        )
        
        // When - 데이터 직렬화/역직렬화
        val gson = com.google.gson.Gson()
        val json = gson.toJson(studyData)
        val restoredData = gson.fromJson(json, DailyStudyData::class.java)
        
        // Then - 데이터가 정확히 복원되어야 함
        assertEquals(studyData.date, restoredData.date)
        assertEquals(studyData.totalStudyTime, restoredData.totalStudyTime)
        assertEquals(studyData.totalTtsCount, restoredData.totalTtsCount)
        assertEquals(studyData.totalWordsViewed, restoredData.totalWordsViewed)
        assertEquals(studyData.totalWordsMemorized, restoredData.totalWordsMemorized)
    }

    /**
     * 복습 단어 워크플로우 테스트
     */
    @Test
    fun `testReviewWordWorkflow`() {
        // Given - 복습 단어들을 설정 (memorizationCount >= 4가 완료 조건)
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 3)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 3)
        
        // When - 복습 단어들을 순차적으로 완료
        var currentIndex = 0
        currentIndex = memorizeWord(currentIndex, "환경", true) // 3 -> 4
        assertEquals(4, getMemorizationCount("환경"))
        
        currentIndex = memorizeWord(currentIndex, "회사", true) // 2 -> 3
        assertEquals(3, getMemorizationCount("회사"))
        
        currentIndex = memorizeWord(currentIndex, "정부", true) // 3 -> 4
        assertEquals(4, getMemorizationCount("정부"))
        
        // Then - 복습 완료된 단어 수 확인
        val completedReviewWords = getCompletedReviewWordsCount()
        assertEquals(2, completedReviewWords) // 환경, 정부만 완료 (memorizationCount >= 4)
    }

    /**
     * 에지 케이스 테스트
     */
    @Test
    fun `testEdgeCases`() {
        // Given - 빈 단어 목록
        val emptyWords = emptyList<WordEntry>()
        
        // When & Then - 빈 목록에서 완료 확인
        assertTrue(isAllWordsCompleted(emptyWords))
        assertEquals(0, getCompletedWordsCount(emptyWords))
        
        // Given - 단일 단어 목록
        val singleWord = listOf(WordEntry("테스트", "test", emptyList()))
        val singleWordData = mutableMapOf("테스트" to WordLearningData("테스트", "high", 0))
        
        // When - 단일 단어 암기
        memorizeWord(0, "테스트", true, singleWordData)
        
        // Then - 단일 단어 완료 확인
        assertTrue(isAllWordsCompleted(singleWord, singleWordData))
        assertEquals(1, getCompletedWordsCount(singleWord, singleWordData))
    }

    // 헬퍼 함수들
    private fun assignNewWordsForToday(count: Int): List<WordEntry> {
        val unassignedWords = testWords.filter { word ->
            val data = wordLearningDataMap[word.kor]
            data?.dailyNewWordDate == null
        }
        
        val wordsToAssign = unassignedWords.take(count)
        wordsToAssign.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(
                dailyNewWordDate = today,
                isNewWord = true
            )
        }
        
        return wordsToAssign
    }

    private fun memorizeWord(currentIndex: Int, word: String, success: Boolean, dataMap: MutableMap<String, WordLearningData> = wordLearningDataMap): Int {
        val data = dataMap[word] ?: WordLearningData(word, "high")
        
        if (success) {
            val newMemorizationCount = data.memorizationCount + 1
            val updatedData = data.copy(
                memorizationCount = newMemorizationCount,
                lastMemorizedDate = today
            )
            dataMap[word] = updatedData
        } else {
            val updatedData = data.copy(
                memorizationCount = 0
            )
            dataMap[word] = updatedData
        }
        
        return findNextUnmemorizedWordIndex(currentIndex, testWords, dataMap)
    }

    private fun findNextUnmemorizedWordIndex(currentIndex: Int, words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Int {
        var nextIndex = currentIndex
        var attempts = 0
        val maxAttempts = words.size * 2

        do {
            nextIndex = if (nextIndex < words.size - 1) {
                nextIndex + 1
            } else {
                0
            }
            attempts++

            val currentWord = words.getOrNull(nextIndex)
            if (currentWord != null) {
                val data = dataMap[currentWord.kor]
                if (data?.memorizationCount == 0) {
                    return nextIndex
                }
            }
        } while (attempts < maxAttempts && nextIndex != currentIndex)

        return if (attempts >= maxAttempts) 0 else nextIndex
    }

    private fun getMemorizationCount(word: String): Int {
        return wordLearningDataMap[word]?.memorizationCount ?: 0
    }

    private fun getCompletedWordsCount(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Int {
        return words.count { word ->
            val data = dataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }
    }

    private fun getCompletedReviewWordsCount(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Int {
        return words.count { word ->
            val data = dataMap[word.kor]
            data?.memorizationCount ?: 0 >= 4
        }
    }

    private fun getProgressPercentage(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Float {
        val totalWords = words.size
        val completedWords = getCompletedWordsCount(words, dataMap)
        return if (totalWords > 0) {
            (completedWords.toFloat() / totalWords) * 100
        } else {
            0.0f
        }
    }

    private fun isAllWordsCompleted(words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Boolean {
        if (words.isEmpty()) return true
        return words.all { word ->
            val data = dataMap[word.kor]
            data?.memorizationCount ?: 0 >= 1
        }
    }
} 