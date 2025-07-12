package com.na982.icandoenglish.domain.repository

import com.na982.icandoenglish.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

/**
 * 학습 데이터 Repository 인터페이스
 * 학습 세션 및 통계 데이터 관리를 위한 추상화
 */
interface StudyDataRepository {
    
    /**
     * 오늘의 학습 세션 가져오기
     */
    suspend fun getTodayStudySession(): StudySession?
    
    /**
     * 학습 세션 저장
     */
    suspend fun saveStudySession(session: StudySession)
    
    /**
     * 학습 세션 업데이트
     */
    suspend fun updateStudySession(session: StudySession)
    
    /**
     * 특정 날짜의 학습 세션 가져오기
     */
    suspend fun getStudySessionByDate(date: String): StudySession?
    
    /**
     * 기간별 학습 세션들 가져오기
     */
    suspend fun getStudySessionsByDateRange(startDate: String, endDate: String): List<StudySession>
    
    /**
     * 전체 학습 통계 가져오기
     */
    suspend fun getStudyStatistics(): StudyStatistics
    
    /**
     * 학습 세션 변화 감지
     */
    fun observeTodayStudySession(): Flow<StudySession?>
    
    /**
     * 일일 학습 목표 달성 여부 확인
     */
    suspend fun checkDailyGoalAchievement(): Boolean
    
    data class StudyStatistics(
        val totalStudyDays: Int,
        val totalStudyTime: Long, // 분 단위
        val totalWordsViewed: Int,
        val totalWordsMemorized: Int,
        val averageWordsPerDay: Double,
        val averageStudyTimePerDay: Double, // 분 단위
        val currentStreak: Int // 연속 학습 일수
    )
} 