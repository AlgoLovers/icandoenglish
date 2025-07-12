package com.na982.icandoenglish.domain.usecase

import com.na982.icandoenglish.domain.model.WordLearningData
import com.na982.icandoenglish.domain.repository.WordRepository
import javax.inject.Inject

/**
 * 단어 암기 처리를 위한 UseCase
 * 간격 반복 알고리즘을 적용하여 학습 데이터를 업데이트
 */
class MemorizeWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    
    /**
     * 단어 암기 처리
     * @param wordId 단어 ID
     * @param grade 학년
     * @param success 암기 성공 여부
     * @return 처리 결과
     */
    suspend operator fun invoke(
        wordId: String, 
        grade: String, 
        success: Boolean
    ): MemorizeResult {
        return try {
            // 기존 학습 데이터 가져오기
            val currentData = wordRepository.getWordLearningData(wordId, grade)
                ?: WordLearningData(wordId, grade)
            
            val updatedData = if (success) {
                // 암기 성공: 간격 반복 알고리즘 적용
                processSuccessfulMemorization(currentData)
            } else {
                // 암기 실패: 암기 횟수 초기화
                processFailedMemorization(currentData)
            }
            
            // 업데이트된 데이터 저장
            wordRepository.saveWordLearningData(updatedData)
            
            MemorizeResult.Success(
                updatedData = updatedData,
                isMemorized = updatedData.isMemorized(),
                memorizationCount = updatedData.memorizationCount
            )
        } catch (e: Exception) {
            MemorizeResult.Error(e.message ?: "단어 암기 처리 중 오류가 발생했습니다.")
        }
    }
    
    /**
     * 암기 성공 시 처리
     * 간격 반복 알고리즘: 3일, 6일, 12일, 24일 후 복습
     */
    private fun processSuccessfulMemorization(data: WordLearningData): WordLearningData {
        val newMemorizationCount = data.memorizationCount + 1
        val reviewIntervals = listOf(3, 6, 12, 24)
        
        val nextReviewInterval = if (newMemorizationCount <= reviewIntervals.size) {
            reviewIntervals[newMemorizationCount - 1]
        } else {
            reviewIntervals.last() // 24일
        }
        
        val nextReviewDate = calculateNextReviewDate(nextReviewInterval)
        val today = getCurrentDate()
        
        return data.copy(
            memorizationCount = newMemorizationCount,
            lastMemorizedDate = today,
            nextReviewDate = nextReviewDate
        )
    }
    
    /**
     * 암기 실패 시 처리
     * 암기 횟수를 0으로 초기화하고 다음날 복습
     */
    private fun processFailedMemorization(data: WordLearningData): WordLearningData {
        val tomorrow = calculateNextReviewDate(1)
        
        return data.copy(
            memorizationCount = 0,
            nextReviewDate = tomorrow
        )
    }
    
    /**
     * 다음 복습 날짜 계산
     */
    private fun calculateNextReviewDate(days: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    /**
     * 현재 날짜 가져오기
     */
    private fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
    
    sealed class MemorizeResult {
        data class Success(
            val updatedData: WordLearningData,
            val isMemorized: Boolean,
            val memorizationCount: Int
        ) : MemorizeResult()
        
        data class Error(val message: String) : MemorizeResult()
    }
} 