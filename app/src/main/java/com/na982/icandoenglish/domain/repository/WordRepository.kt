package com.na982.icandoenglish.domain.repository

import com.na982.icandoenglish.domain.model.Word
import com.na982.icandoenglish.domain.model.WordLearningData
import kotlinx.coroutines.flow.Flow

/**
 * 단어 Repository 인터페이스
 * 도메인 레이어에서 데이터 접근을 위한 추상화
 */
interface WordRepository {
    
    /**
     * 모든 단어 가져오기
     */
    suspend fun getAllWords(): List<Word>
    
    /**
     * 특정 학년의 단어들 가져오기
     */
    suspend fun getWordsByGrade(grade: String): List<Word>
    
    /**
     * 단어 학습 데이터 가져오기
     */
    suspend fun getWordLearningData(wordId: String, grade: String): WordLearningData?
    
    /**
     * 단어 학습 데이터 저장
     */
    suspend fun saveWordLearningData(learningData: WordLearningData)
    
    /**
     * 오늘의 신규 단어들 가져오기
     */
    suspend fun getTodayNewWords(grade: String, maxCount: Int = 10): List<Word>
    
    /**
     * 오늘의 복습 단어들 가져오기
     */
    suspend fun getTodayReviewWords(grade: String): List<Word>
    
    /**
     * 신규 단어 배정
     */
    suspend fun assignNewWordsForToday(grade: String, maxCount: Int = 10)
    
    /**
     * 단어 암기 처리
     */
    suspend fun processWordMemorization(wordId: String, grade: String, success: Boolean)
    
    /**
     * 암기 완료된 단어들 가져오기
     */
    suspend fun getMemorizedWords(grade: String): List<Word>
    
    /**
     * 암기 완료되지 않은 단어들 가져오기
     */
    suspend fun getUnmemorizedWords(grade: String): List<Word>
    
    /**
     * 단어 학습 데이터 변화 감지
     */
    fun observeWordLearningData(wordId: String, grade: String): Flow<WordLearningData?>
} 