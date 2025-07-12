package com.na982.icandoenglish.domain.usecase

import com.na982.icandoenglish.domain.model.Word
import com.na982.icandoenglish.domain.repository.WordRepository
import javax.inject.Inject

/**
 * 오늘의 단어들을 가져오는 UseCase
 * 신규 단어와 복습 단어를 모두 포함
 */
class GetTodayWordsUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    
    /**
     * 오늘의 모든 단어들을 가져옴
     * @param grade 학년 (high, middle, low)
     * @param maxNewWords 하루 최대 신규 단어 수
     * @return 오늘의 단어들 (신규 + 복습)
     */
    suspend operator fun invoke(grade: String, maxNewWords: Int = 10): TodayWordsResult {
        return try {
            // 신규 단어 배정 (아직 배정되지 않았다면)
            wordRepository.assignNewWordsForToday(grade, maxNewWords)
            
            // 오늘의 신규 단어들
            val newWords = wordRepository.getTodayNewWords(grade, maxNewWords)
            
            // 오늘의 복습 단어들
            val reviewWords = wordRepository.getTodayReviewWords(grade)
            
            TodayWordsResult.Success(
                newWords = newWords,
                reviewWords = reviewWords,
                totalWords = newWords.size + reviewWords.size
            )
        } catch (e: Exception) {
            TodayWordsResult.Error(e.message ?: "단어를 가져오는 중 오류가 발생했습니다.")
        }
    }
    
    sealed class TodayWordsResult {
        data class Success(
            val newWords: List<Word>,
            val reviewWords: List<Word>,
            val totalWords: Int
        ) : TodayWordsResult()
        
        data class Error(val message: String) : TodayWordsResult()
    }
} 