package com.na982.icandoenglish.domain.usecase

import com.na982.icandoenglish.domain.model.WordLearningData
import com.na982.icandoenglish.domain.repository.WordRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * MemorizeWordUseCase 단위 테스트
 */
class MemorizeWordUseCaseTest {
    
    private lateinit var useCase: MemorizeWordUseCase
    private lateinit var mockRepository: WordRepository
    
    @Before
    fun setup() {
        mockRepository = mock()
        useCase = MemorizeWordUseCase(mockRepository)
    }
    
    @Test
    fun `암기 성공 시 간격 반복 알고리즘이 적용되어야 함`() = runTest {
        // Given
        val wordId = "test_word"
        val grade = "high"
        val initialData = WordLearningData(
            wordId = wordId,
            grade = grade,
            memorizationCount = 1,
            lastMemorizedDate = "2024-01-01",
            nextReviewDate = "2024-01-07"
        )
        
        whenever(mockRepository.getWordLearningData(wordId, grade))
            .thenReturn(initialData)
        
        // When
        val result = useCase(wordId, grade, true)
        
        // Then
        assert(result is MemorizeWordUseCase.MemorizeResult.Success)
        val successResult = result as MemorizeWordUseCase.MemorizeResult.Success
        
        assert(successResult.memorizationCount == 2)
        assert(successResult.isMemorized == false) // 4회 미만이므로 아직 완료되지 않음
        
        verify(mockRepository).getWordLearningData(wordId, grade)
        verify(mockRepository).saveWordLearningData(any())
    }
    
    @Test
    fun `암기 실패 시 암기 횟수가 초기화되어야 함`() = runTest {
        // Given
        val wordId = "test_word"
        val grade = "high"
        val initialData = WordLearningData(
            wordId = wordId,
            grade = grade,
            memorizationCount = 2,
            lastMemorizedDate = "2024-01-01",
            nextReviewDate = "2024-01-13"
        )
        
        whenever(mockRepository.getWordLearningData(wordId, grade))
            .thenReturn(initialData)
        
        // When
        val result = useCase(wordId, grade, false)
        
        // Then
        assert(result is MemorizeWordUseCase.MemorizeResult.Success)
        val successResult = result as MemorizeWordUseCase.MemorizeResult.Success
        
        assert(successResult.memorizationCount == 0)
        assert(successResult.isMemorized == false)
        
        verify(mockRepository).getWordLearningData(wordId, grade)
        verify(mockRepository).saveWordLearningData(any())
    }
    
    @Test
    fun `4회 암기 성공 시 암기 완료로 처리되어야 함`() = runTest {
        // Given
        val wordId = "test_word"
        val grade = "high"
        val initialData = WordLearningData(
            wordId = wordId,
            grade = grade,
            memorizationCount = 3,
            lastMemorizedDate = "2024-01-01",
            nextReviewDate = "2024-01-25"
        )
        
        whenever(mockRepository.getWordLearningData(wordId, grade))
            .thenReturn(initialData)
        
        // When
        val result = useCase(wordId, grade, true)
        
        // Then
        assert(result is MemorizeWordUseCase.MemorizeResult.Success)
        val successResult = result as MemorizeWordUseCase.MemorizeResult.Success
        
        assert(successResult.memorizationCount == 4)
        assert(successResult.isMemorized == true)
        
        verify(mockRepository).getWordLearningData(wordId, grade)
        verify(mockRepository).saveWordLearningData(any())
    }
    
    @Test
    fun `학습 데이터가 없을 때 새로 생성되어야 함`() = runTest {
        // Given
        val wordId = "new_word"
        val grade = "high"
        
        whenever(mockRepository.getWordLearningData(wordId, grade))
            .thenReturn(null)
        
        // When
        val result = useCase(wordId, grade, true)
        
        // Then
        assert(result is MemorizeWordUseCase.MemorizeResult.Success)
        val successResult = result as MemorizeWordUseCase.MemorizeResult.Success
        
        assert(successResult.memorizationCount == 1)
        assert(successResult.isMemorized == false)
        
        verify(mockRepository).getWordLearningData(wordId, grade)
        verify(mockRepository).saveWordLearningData(any())
    }
    
    @Test
    fun `Repository 오류 시 Error 결과를 반환해야 함`() = runTest {
        // Given
        val wordId = "test_word"
        val grade = "high"
        val errorMessage = "Database error"
        
        whenever(mockRepository.getWordLearningData(wordId, grade))
            .thenThrow(RuntimeException(errorMessage))
        
        // When
        val result = useCase(wordId, grade, true)
        
        // Then
        assert(result is MemorizeWordUseCase.MemorizeResult.Error)
        val errorResult = result as MemorizeWordUseCase.MemorizeResult.Error
        
        assert(errorResult.message.contains("오류가 발생했습니다"))
        
        verify(mockRepository).getWordLearningData(wordId, grade)
        verify(mockRepository, never()).saveWordLearningData(any())
    }
} 