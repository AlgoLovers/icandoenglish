package com.na982.icandoenglish.data.local

import androidx.room.*
import com.na982.icandoenglish.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 학습 세션 데이터 접근 객체 (DAO)
 * 일일 학습 세션 데이터를 조작하는 인터페이스
 */
@Dao
interface StudySessionDao {
    
    @Query("SELECT * FROM study_sessions WHERE date = :date")
    suspend fun getStudySessionByDate(date: String): StudySessionEntity?
    
    @Query("SELECT * FROM study_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getStudySessionsByDateRange(startDate: String, endDate: String): List<StudySessionEntity>
    
    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    suspend fun getAllStudySessions(): List<StudySessionEntity>
    
    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getTotalStudyDays(): Int
    
    @Query("SELECT SUM(totalStudyTime) FROM study_sessions")
    suspend fun getTotalStudyTime(): Long?
    
    @Query("SELECT SUM(wordsViewed) FROM study_sessions")
    suspend fun getTotalWordsViewed(): Int?
    
    @Query("SELECT SUM(wordsMemorized) FROM study_sessions")
    suspend fun getTotalWordsMemorized(): Int?
    
    @Query("SELECT AVG(wordsViewed) FROM study_sessions")
    suspend fun getAverageWordsPerDay(): Double?
    
    @Query("SELECT AVG(totalStudyTime) FROM study_sessions")
    suspend fun getAverageStudyTimePerDay(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity)
    
    @Update
    suspend fun updateStudySession(session: StudySessionEntity)
    
    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)
    
    @Query("DELETE FROM study_sessions WHERE date = :date")
    suspend fun deleteStudySessionByDate(date: String)
    
    @Query("DELETE FROM study_sessions")
    suspend fun deleteAllStudySessions()
    
    // Flow를 사용한 실시간 데이터 감지
    @Query("SELECT * FROM study_sessions WHERE date = :date")
    fun observeStudySessionByDate(date: String): Flow<StudySessionEntity?>
    
    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    fun observeAllStudySessions(): Flow<List<StudySessionEntity>>
} 