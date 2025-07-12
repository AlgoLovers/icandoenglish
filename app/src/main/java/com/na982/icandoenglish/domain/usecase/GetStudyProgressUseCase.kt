package com.na982.icandoenglish.domain.usecase

import com.na982.icandoenglish.domain.model.Word
import com.na982.icandoenglish.domain.repository.WordRepository
import com.na982.icandoenglish.domain.repository.StudyDataRepository
import javax.inject.Inject

/**
 * 학습 진행률을 가져오는 UseCase
 * 현재 세션과 전체 학습 진행 상황을 제공
 */
class GetStudyProgressUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val studyDataRepository: StudyDataRepository
) {
    
    /**
     * 현재 학습 진행률 가져오기
     * @param grade 학년
     * @param currentWords 현재 세션의 단어들
     * @param currentIndex 현재 단어 인덱스
     * @param memorizedCount 현재 세션에서 암기한 단어 수
     * @return 학습 진행률 정보
     */
    suspend operator fun invoke(
        grade: String,
        currentWords: List<Word>,
        currentIndex: Int,
        memorizedCount: Int
    ): StudyProgressResult {
        return try {
            // 전체 통계
            val totalWords = wordRepository.getAllWords().size
            val memorizedWords = wordRepository.getMemorizedWords(grade).size
            val unmemorizedWords = wordRepository.getUnmemorizedWords(grade).size
            
            // 현재 세션 통계
            val sessionProgress = calculateSessionProgress(
                currentWords = currentWords,
                currentIndex = currentIndex,
                memorizedCount = memorizedCount
            )
            
            // 오늘의 학습 세션
            val todaySession = studyDataRepository.getTodayStudySession()
            
            StudyProgressResult.Success(
                sessionProgress = sessionProgress,
                overallProgress = OverallProgress(
                    totalWords = totalWords,
                    memorizedWords = memorizedWords,
                    unmemorizedWords = unmemorizedWords,
                    memorizationRate = if (totalWords > 0) {
                        (memorizedWords.toDouble() / totalWords) * 100
                    } else 0.0
                ),
                todaySession = todaySession
            )
        } catch (e: Exception) {
            StudyProgressResult.Error(e.message ?: "진행률을 가져오는 중 오류가 발생했습니다.")
        }
    }
    
    /**
     * 현재 세션 진행률 계산
     */
    private fun calculateSessionProgress(
        currentWords: List<Word>,
        currentIndex: Int,
        memorizedCount: Int
    ): SessionProgress {
        val totalWords = currentWords.size
        val viewedWords = currentIndex + 1
        val remainingWords = totalWords - viewedWords
        
        return SessionProgress(
            currentIndex = currentIndex,
            totalWords = totalWords,
            viewedWords = viewedWords,
            remainingWords = remainingWords,
            memorizedCount = memorizedCount,
            progressPercentage = if (totalWords > 0) {
                (viewedWords.toDouble() / totalWords) * 100
            } else 0.0
        )
    }
    
    data class SessionProgress(
        val currentIndex: Int,
        val totalWords: Int,
        val viewedWords: Int,
        val remainingWords: Int,
        val memorizedCount: Int,
        val progressPercentage: Double
    )
    
    data class OverallProgress(
        val totalWords: Int,
        val memorizedWords: Int,
        val unmemorizedWords: Int,
        val memorizationRate: Double
    )
    
    sealed class StudyProgressResult {
        data class Success(
            val sessionProgress: SessionProgress,
            val overallProgress: OverallProgress,
            val todaySession: com.na982.icandoenglish.domain.model.StudySession?
        ) : StudyProgressResult()
        
        data class Error(val message: String) : StudyProgressResult()
    }
} 