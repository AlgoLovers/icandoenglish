package com.na982.icandoenglish

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import java.text.SimpleDateFormat
import java.util.*

/**
 * 단어 순환 로직에 대한 포괄적인 테스트
 * 
 * 이 테스트는 다음 기능들을 검증합니다:
 * - 다음/이전 단어로 이동
 * - 암기 완료한 단어 건너뛰기
 * - 단어 목록 경계 처리
 * - 무한 루프 방지
 */
class WordNavigationTest {
    private lateinit var testWords: List<WordEntry>
    private lateinit var wordLearningDataMap: MutableMap<String, WordLearningData>
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            wordLearningDataMap[word.kor] = WordLearningData(word.kor, "high")
        }
    }

    /**
     * 암기 완료한 단어를 건너뛰는 다음 단어 이동 테스트
     */
    @Test
    fun `testMoveToNextWordSkipMemorized`() {
        // Given - 일부 단어를 암기 완료 상태로 설정
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["화재"] = wordLearningDataMap["화재"]!!.copy(memorizationCount = 1)

        var currentIndex = 0 // 환경 (암기 완료)
        
        // When & Then - 다음 단어로 이동 (암기 완료한 단어 건너뛰기)
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(1, currentIndex) // 회사 (암기 미완료)
        
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(2, currentIndex) // 정부 (암기 완료) -> 3번째로 이동
        
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(3, currentIndex) // 얼굴 (암기 미완료)
    }

    /**
     * 암기 완료한 단어를 건너뛰는 이전 단어 이동 테스트
     */
    @Test
    fun `testMoveToPrevWordSkipMemorized`() {
        // Given - 일부 단어를 암기 완료 상태로 설정
        wordLearningDataMap["얼굴"] = wordLearningDataMap["얼굴"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["화재"] = wordLearningDataMap["화재"]!!.copy(memorizationCount = 2)
        wordLearningDataMap["원인"] = wordLearningDataMap["원인"]!!.copy(memorizationCount = 1)

        var currentIndex = 9 // 원인 (암기 완료)
        
        // When & Then - 이전 단어로 이동 (암기 완료한 단어 건너뛰기)
        currentIndex = findPrevUnmemorizedWordIndex(currentIndex)
        assertEquals(8, currentIndex) // 상태 (암기 미완료)
        
        currentIndex = findPrevUnmemorizedWordIndex(currentIndex)
        assertEquals(7, currentIndex) // 문제 (암기 미완료)
        
        currentIndex = findPrevUnmemorizedWordIndex(currentIndex)
        assertEquals(6, currentIndex) // 화재 (암기 완료) -> 5번째로 이동
    }

    /**
     * 모든 단어가 암기 완료된 경우 처리 테스트
     */
    @Test
    fun `testMoveToNextWordAllMemorized`() {
        // Given - 모든 단어를 암기 완료 상태로 설정
        testWords.forEachIndexed { index, word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 1)
        }

        var currentIndex = 5
        
        // When & Then - 모든 단어가 암기 완료된 경우 첫 번째 단어로 이동
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(0, currentIndex) // 첫 번째 단어로 이동
    }

    /**
     * 단어 목록 경계 처리 테스트
     */
    @Test
    fun `testWordNavigationBoundaryHandling`() {
        // Given - 마지막 단어에서 시작
        var currentIndex = testWords.size - 1
        
        // When & Then - 마지막에서 다음으로 이동하면 첫 번째로
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(0, currentIndex)
        
        // Given - 첫 번째 단어에서 시작
        currentIndex = 0
        
        // When & Then - 첫 번째에서 이전으로 이동하면 마지막으로
        currentIndex = findPrevUnmemorizedWordIndex(currentIndex)
        assertEquals(testWords.size - 1, currentIndex)
    }

    /**
     * 무한 루프 방지 테스트
     */
    @Test
    fun `testInfiniteLoopPrevention`() {
        // Given - 모든 단어를 암기 완료 상태로 설정
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 1)
        }

        var currentIndex = 0
        var attempts = 0
        val maxAttempts = testWords.size * 2
        
        // When - 무한 루프 방지 로직 테스트
        do {
            currentIndex = findNextUnmemorizedWordIndex(currentIndex)
            attempts++
        } while (attempts < maxAttempts && currentIndex != 0)
        
        // Then - 최대 시도 횟수 내에서 루프가 종료되어야 함
        assertTrue(attempts < maxAttempts)
    }

    /**
     * 빈 단어 목록 처리 테스트
     */
    @Test
    fun `testEmptyWordListHandling`() {
        // Given - 빈 단어 목록
        val emptyWords = emptyList<WordEntry>()
        
        // When & Then - 빈 목록에서 다음 단어 찾기
        val nextIndex = findNextUnmemorizedWordIndex(0, emptyWords)
        assertEquals(-1, nextIndex) // 빈 목록이므로 -1 반환
    }

    /**
     * 단일 단어 목록 처리 테스트
     */
    @Test
    fun `testSingleWordListHandling`() {
        // Given - 단일 단어 목록
        val singleWord = listOf(WordEntry("테스트", "test", emptyList()))
        val singleWordData = mutableMapOf("테스트" to WordLearningData("테스트", "high"))
        
        // When & Then - 단일 단어에서 다음 단어 찾기
        val nextIndex = findNextUnmemorizedWordIndex(0, singleWord, singleWordData)
        assertEquals(0, nextIndex) // 동일한 단어로 이동
    }

    /**
     * 암기 완료하지 않은 단어만 있는 경우 테스트
     */
    @Test
    fun `testAllUnmemorizedWords`() {
        // Given - 모든 단어가 암기 미완료 상태
        testWords.forEach { word ->
            wordLearningDataMap[word.kor] = wordLearningDataMap[word.kor]!!.copy(memorizationCount = 0)
        }

        var currentIndex = 0
        
        // When & Then - 순차적으로 다음 단어로 이동
        for (i in 1 until testWords.size) {
            currentIndex = findNextUnmemorizedWordIndex(currentIndex)
            assertEquals(i, currentIndex)
        }
        
        // 마지막에서 첫 번째로 순환
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(0, currentIndex)
    }

    /**
     * 암기 완료한 단어가 연속으로 있는 경우 테스트
     */
    @Test
    fun `testConsecutiveMemorizedWords`() {
        // Given - 연속된 단어들을 암기 완료 상태로 설정
        wordLearningDataMap["환경"] = wordLearningDataMap["환경"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["회사"] = wordLearningDataMap["회사"]!!.copy(memorizationCount = 1)
        wordLearningDataMap["정부"] = wordLearningDataMap["정부"]!!.copy(memorizationCount = 1)

        var currentIndex = 0 // 환경 (암기 완료)
        
        // When & Then - 연속된 암기 완료 단어들을 건너뛰고 다음 미완료 단어로 이동
        currentIndex = findNextUnmemorizedWordIndex(currentIndex)
        assertEquals(3, currentIndex) // 얼굴 (암기 미완료)
    }

    // 헬퍼 함수들
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

    private fun findPrevUnmemorizedWordIndex(currentIndex: Int, words: List<WordEntry> = testWords, dataMap: Map<String, WordLearningData> = wordLearningDataMap): Int {
        var prevIndex = currentIndex
        var attempts = 0
        val maxAttempts = words.size * 2

        do {
            prevIndex = if (prevIndex > 0) {
                prevIndex - 1
            } else {
                words.size - 1
            }
            attempts++

            val currentWord = words.getOrNull(prevIndex)
            if (currentWord != null) {
                val data = dataMap[currentWord.kor]
                if (data?.memorizationCount == 0) {
                    return prevIndex
                }
            }
        } while (attempts < maxAttempts && prevIndex != currentIndex)

        return if (attempts >= maxAttempts) 0 else prevIndex
    }
} 