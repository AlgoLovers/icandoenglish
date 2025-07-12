package com.na982.icandoenglish

import org.junit.Assert.*
import org.junit.Test
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * 학습 데이터 관리에 대한 포괄적인 테스트
 * 
 * 이 테스트는 다음 기능들을 검증합니다:
 * - 일일 학습 데이터 직렬화/역직렬화
 * - 학습 진행률 추적
 * - 데이터 지속성
 * - 통계 계산
 */
class StudyDataTest {
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Test
    fun `testDailyStudyDataSerialization`() {
        // Given
        val data = DailyStudyData(
            date = "2024-05-01",
            totalStudyTime = 120,
            totalTtsCount = 5,
            totalWordsViewed = 10,
            totalWordsMemorized = 7
        )
        
        // When
        val json = gson.toJson(data)
        val restored = gson.fromJson(json, DailyStudyData::class.java)
        
        // Then
        assertEquals(data.date, restored.date)
        assertEquals(data.totalStudyTime, restored.totalStudyTime)
        assertEquals(data.totalTtsCount, restored.totalTtsCount)
        assertEquals(data.totalWordsViewed, restored.totalWordsViewed)
        assertEquals(data.totalWordsMemorized, restored.totalWordsMemorized)
    }

    @Test
    fun `testProgressCalculation`() {
        // Given
        val allWords = listOf(
            WordEntry("사과", "apple", emptyList()),
            WordEntry("바나나", "banana", emptyList()),
            WordEntry("포도", "grape", emptyList())
        )
        val memorized = setOf("사과", "포도")
        
        // When
        val progress = allWords.count { memorized.contains(it.kor) }
        
        // Then
        assertEquals(2, progress)
    }

    @Test
    fun `testDailyStudyDataDefaultValues`() {
        // Given & When
        val data = DailyStudyData("2024-05-01")
        
        // Then
        assertEquals("2024-05-01", data.date)
        assertEquals(0, data.totalStudyTime)
        assertEquals(0, data.totalTtsCount)
        assertEquals(0, data.totalWordsViewed)
        assertEquals(0, data.totalWordsMemorized)
    }

    @Test
    fun `testDailyStudyDataWithAllValues`() {
        // Given
        val date = "2024-05-01"
        val totalStudyTime = 3600 // 1시간
        val totalTtsCount = 25
        val totalWordsViewed = 50
        val totalWordsMemorized = 15
        
        // When
        val data = DailyStudyData(
            date = date,
            totalStudyTime = totalStudyTime,
            totalTtsCount = totalTtsCount,
            totalWordsViewed = totalWordsViewed,
            totalWordsMemorized = totalWordsMemorized
        )
        
        // Then
        assertEquals(date, data.date)
        assertEquals(totalStudyTime, data.totalStudyTime)
        assertEquals(totalTtsCount, data.totalTtsCount)
        assertEquals(totalWordsViewed, data.totalWordsViewed)
        assertEquals(totalWordsMemorized, data.totalWordsMemorized)
    }

    @Test
    fun `testDailyStudyDataEquality`() {
        // Given
        val data1 = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        val data2 = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        val data3 = DailyStudyData("2024-05-01", 120, 5, 10, 8)
        
        // When & Then
        assertEquals(data1, data2)
        assertNotEquals(data1, data3)
        assertNotEquals(data1, "string")
    }

    @Test
    fun `testDailyStudyDataHashCode`() {
        // Given
        val data1 = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        val data2 = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        
        // When & Then
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun `testDailyStudyDataToString`() {
        // Given
        val data = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        
        // When
        val result = data.toString()
        
        // Then
        assertTrue(result.contains("2024-05-01"))
        assertTrue(result.contains("120"))
        assertTrue(result.contains("5"))
        assertTrue(result.contains("10"))
        assertTrue(result.contains("7"))
    }

    @Test
    fun `testStudyDataAccumulation`() {
        // Given
        val initialData = DailyStudyData("2024-05-01", 60, 2, 5, 3)
        
        // When - 추가 학습 데이터 누적
        val updatedData = initialData.copy(
            totalStudyTime = initialData.totalStudyTime + 30,
            totalTtsCount = initialData.totalTtsCount + 1,
            totalWordsViewed = initialData.totalWordsViewed + 2,
            totalWordsMemorized = initialData.totalWordsMemorized + 1
        )
        
        // Then
        assertEquals(90, updatedData.totalStudyTime)
        assertEquals(3, updatedData.totalTtsCount)
        assertEquals(7, updatedData.totalWordsViewed)
        assertEquals(4, updatedData.totalWordsMemorized)
    }

    @Test
    fun `testStudyDataEdgeCases`() {
        // Given & When - 최대값 테스트
        val maxData = DailyStudyData(
            date = "2024-05-01",
            totalStudyTime = Long.MAX_VALUE,
            totalTtsCount = Int.MAX_VALUE,
            totalWordsViewed = Int.MAX_VALUE,
            totalWordsMemorized = Int.MAX_VALUE
        )
        
        // Then
        assertEquals(Long.MAX_VALUE, maxData.totalStudyTime)
        assertEquals(Int.MAX_VALUE, maxData.totalTtsCount)
        assertEquals(Int.MAX_VALUE, maxData.totalWordsViewed)
        assertEquals(Int.MAX_VALUE, maxData.totalWordsMemorized)
    }

    @Test
    fun `testStudyDataNegativeValues`() {
        // Given & When - 음수값 테스트
        val negativeData = DailyStudyData(
            date = "2024-05-01",
            totalStudyTime = -10,
            totalTtsCount = -5,
            totalWordsViewed = -3,
            totalWordsMemorized = -1
        )
        
        // Then
        assertEquals(-10, negativeData.totalStudyTime)
        assertEquals(-5, negativeData.totalTtsCount)
        assertEquals(-3, negativeData.totalWordsViewed)
        assertEquals(-1, negativeData.totalWordsMemorized)
    }

    @Test
    fun `testStudyDataSerializationWithNullValues`() {
        // Given
        val data = DailyStudyData("2024-05-01")
        
        // When
        val json = gson.toJson(data)
        val restored = gson.fromJson(json, DailyStudyData::class.java)
        
        // Then
        assertEquals(data.date, restored.date)
        assertEquals(data.totalStudyTime, restored.totalStudyTime)
        assertEquals(data.totalTtsCount, restored.totalTtsCount)
        assertEquals(data.totalWordsViewed, restored.totalWordsViewed)
        assertEquals(data.totalWordsMemorized, restored.totalWordsMemorized)
    }

    @Test
    fun `testStudyDataCopy`() {
        // Given
        val originalData = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        
        // When
        val copiedData = originalData.copy(
            totalStudyTime = 180,
            totalWordsMemorized = 8
        )
        
        // Then
        assertEquals(originalData.date, copiedData.date)
        assertEquals(originalData.totalTtsCount, copiedData.totalTtsCount)
        assertEquals(originalData.totalWordsViewed, copiedData.totalWordsViewed)
        assertEquals(180, copiedData.totalStudyTime)
        assertEquals(8, copiedData.totalWordsMemorized)
    }

    @Test
    fun `testStudyDataProgressCalculation`() {
        // Given
        val data = DailyStudyData("2024-05-01", 120, 5, 10, 7)
        
        // When - 진행률 계산
        val memorizationRate = (data.totalWordsMemorized.toFloat() / data.totalWordsViewed) * 100
        val averageStudyTimePerWord = data.totalStudyTime.toFloat() / data.totalWordsViewed
        val averageTtsPerWord = data.totalTtsCount.toFloat() / data.totalWordsViewed
        
        // Then
        assertEquals(70.0f, memorizationRate, 0.01f) // 7/10 * 100 = 70%
        assertEquals(12.0f, averageStudyTimePerWord, 0.01f) // 120/10 = 12초
        assertEquals(0.5f, averageTtsPerWord, 0.01f) // 5/10 = 0.5
    }

    @Test
    fun `testStudyDataZeroDivisionHandling`() {
        // Given
        val data = DailyStudyData("2024-05-01", 0, 0, 0, 0)
        
        // When & Then - 0으로 나누기 방지
        val memorizationRate = if (data.totalWordsViewed > 0) {
            (data.totalWordsMemorized.toFloat() / data.totalWordsViewed) * 100
        } else {
            0.0f
        }
        
        assertEquals(0.0f, memorizationRate, 0.01f)
    }

    @Test
    fun `testStudyDataDateValidation`() {
        // Given - 다양한 날짜 형식
        val validDates = listOf(
            "2024-05-01",
            "2024-12-31",
            "2024-01-01",
            "2024-02-29" // 윤년
        )
        
        // When & Then - 모든 날짜가 유효해야 함
        validDates.forEach { date ->
            val data = DailyStudyData(date)
            assertEquals(date, data.date)
        }
    }

    @Test
    fun `testStudyDataStatistics`() {
        // Given - 여러 날짜의 학습 데이터
        val studyDataList = listOf(
            DailyStudyData("2024-05-01", 120, 5, 10, 7),
            DailyStudyData("2024-05-02", 180, 8, 15, 12),
            DailyStudyData("2024-05-03", 90, 3, 8, 6)
        )
        
        // When - 통계 계산
        val totalStudyTime = studyDataList.sumOf { it.totalStudyTime }
        val totalTtsCount = studyDataList.sumOf { it.totalTtsCount }
        val totalWordsViewed = studyDataList.sumOf { it.totalWordsViewed }
        val totalWordsMemorized = studyDataList.sumOf { it.totalWordsMemorized }
        
        val averageStudyTime = totalStudyTime.toFloat() / studyDataList.size
        val averageMemorizationRate = (totalWordsMemorized.toFloat() / totalWordsViewed) * 100
        
        // Then
        assertEquals(390, totalStudyTime) // 120 + 180 + 90
        assertEquals(16, totalTtsCount) // 5 + 8 + 3
        assertEquals(33, totalWordsViewed) // 10 + 15 + 8
        assertEquals(25, totalWordsMemorized) // 7 + 12 + 6
        assertEquals(130.0f, averageStudyTime, 0.01f) // 390 / 3
        assertEquals(75.76f, averageMemorizationRate, 0.01f) // (25/33) * 100
    }
} 