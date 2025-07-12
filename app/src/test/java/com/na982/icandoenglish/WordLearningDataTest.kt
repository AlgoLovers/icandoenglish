package com.na982.icandoenglish

import org.junit.Assert.*
import org.junit.Test
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * WordLearningData 클래스에 대한 포괄적인 테스트
 * 
 * 이 테스트는 단어 학습 데이터의 모든 기능을 검증합니다:
 * - 암기 횟수 관리
 * - 복습 일정 관리
 * - 신규 단어 배정
 * - 데이터 직렬화/역직렬화
 */
class WordLearningDataTest {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Test
    fun `testWordLearningDataCreation`() {
        // Given
        val word = "환경"
        val grade = "high"
        
        // When
        val data = WordLearningData(word, grade)
        
        // Then
        assertEquals(word, data.word)
        assertEquals(grade, data.grade)
        assertEquals(0, data.memorizationCount)
        assertNull(data.lastMemorizedDate)
        assertNull(data.nextReviewDate)
        assertTrue(data.isNewWord)
        assertNull(data.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataWithCustomValues`() {
        // Given
        val word = "회사"
        val grade = "middle"
        val memorizationCount = 2
        val lastMemorizedDate = "2024-01-15"
        val nextReviewDate = "2024-01-21"
        val isNewWord = false
        val dailyNewWordDate = "2024-01-10"
        
        // When
        val data = WordLearningData(
            word = word,
            grade = grade,
            memorizationCount = memorizationCount,
            lastMemorizedDate = lastMemorizedDate,
            nextReviewDate = nextReviewDate,
            isNewWord = isNewWord,
            dailyNewWordDate = dailyNewWordDate
        )
        
        // Then
        assertEquals(word, data.word)
        assertEquals(grade, data.grade)
        assertEquals(memorizationCount, data.memorizationCount)
        assertEquals(lastMemorizedDate, data.lastMemorizedDate)
        assertEquals(nextReviewDate, data.nextReviewDate)
        assertEquals(isNewWord, data.isNewWord)
        assertEquals(dailyNewWordDate, data.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataSerialization`() {
        // Given
        val originalData = WordLearningData(
            word = "정부",
            grade = "high",
            memorizationCount = 3,
            lastMemorizedDate = "2024-01-15",
            nextReviewDate = "2024-01-27",
            isNewWord = false,
            dailyNewWordDate = "2024-01-10"
        )
        
        // When
        val json = gson.toJson(originalData)
        val restoredData = gson.fromJson(json, WordLearningData::class.java)
        
        // Then
        assertEquals(originalData.word, restoredData.word)
        assertEquals(originalData.grade, restoredData.grade)
        assertEquals(originalData.memorizationCount, restoredData.memorizationCount)
        assertEquals(originalData.lastMemorizedDate, restoredData.lastMemorizedDate)
        assertEquals(originalData.nextReviewDate, restoredData.nextReviewDate)
        assertEquals(originalData.isNewWord, restoredData.isNewWord)
        assertEquals(originalData.dailyNewWordDate, restoredData.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataCopy`() {
        // Given
        val originalData = WordLearningData(
            word = "얼굴",
            grade = "high",
            memorizationCount = 1,
            lastMemorizedDate = "2024-01-15",
            nextReviewDate = "2024-01-18",
            isNewWord = true,
            dailyNewWordDate = "2024-01-10"
        )
        
        // When
        val updatedData = originalData.copy(
            memorizationCount = 2,
            lastMemorizedDate = "2024-01-16",
            nextReviewDate = "2024-01-22"
        )
        
        // Then
        assertEquals(originalData.word, updatedData.word)
        assertEquals(originalData.grade, updatedData.grade)
        assertEquals(2, updatedData.memorizationCount)
        assertEquals("2024-01-16", updatedData.lastMemorizedDate)
        assertEquals("2024-01-22", updatedData.nextReviewDate)
        assertEquals(originalData.isNewWord, updatedData.isNewWord)
        assertEquals(originalData.dailyNewWordDate, updatedData.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataEquality`() {
        // Given
        val data1 = WordLearningData("경주", "high", 1)
        val data2 = WordLearningData("경주", "high", 1)
        val data3 = WordLearningData("경주", "high", 2)
        
        // When & Then
        assertEquals(data1, data2)
        assertNotEquals(data1, data3)
        assertNotEquals(data1, "string")
    }

    @Test
    fun `testWordLearningDataHashCode`() {
        // Given
        val data1 = WordLearningData("식물", "high", 1)
        val data2 = WordLearningData("식물", "high", 1)
        
        // When & Then
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun `testWordLearningDataToString`() {
        // Given
        val data = WordLearningData("화재", "high", 2)
        
        // When
        val result = data.toString()
        
        // Then
        assertTrue(result.contains("화재"))
        assertTrue(result.contains("high"))
        assertTrue(result.contains("2"))
    }

    @Test
    fun `testWordLearningDataDefaultValues`() {
        // Given & When
        val data = WordLearningData("문제", "high")
        
        // Then
        assertEquals(0, data.memorizationCount)
        assertNull(data.lastMemorizedDate)
        assertNull(data.nextReviewDate)
        assertTrue(data.isNewWord)
        assertNull(data.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataWithNullValues`() {
        // Given & When
        val data = WordLearningData(
            word = "상태",
            grade = "high",
            memorizationCount = 0,
            lastMemorizedDate = null,
            nextReviewDate = null,
            isNewWord = true,
            dailyNewWordDate = null
        )
        
        // Then
        assertNull(data.lastMemorizedDate)
        assertNull(data.nextReviewDate)
        assertNull(data.dailyNewWordDate)
    }

    @Test
    fun `testWordLearningDataEdgeCases`() {
        // Given & When
        val data = WordLearningData(
            word = "",
            grade = "",
            memorizationCount = -1,
            lastMemorizedDate = "",
            nextReviewDate = "",
            isNewWord = false,
            dailyNewWordDate = ""
        )
        
        // Then
        assertEquals("", data.word)
        assertEquals("", data.grade)
        assertEquals(-1, data.memorizationCount)
        assertEquals("", data.lastMemorizedDate)
        assertEquals("", data.nextReviewDate)
        assertFalse(data.isNewWord)
        assertEquals("", data.dailyNewWordDate)
    }
} 