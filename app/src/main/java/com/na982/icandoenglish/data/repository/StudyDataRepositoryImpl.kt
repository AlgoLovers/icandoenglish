package com.na982.icandoenglish.data.repository

import com.na982.icandoenglish.data.local.StudySessionDao
import com.na982.icandoenglish.data.local.entity.StudySessionEntity
import com.na982.icandoenglish.domain.model.StudySession
import com.na982.icandoenglish.domain.repository.StudyDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * StudyDataRepository 구현체
 * Room Database를 사용하여 학습 세션 데이터를 관리
 */
class StudyDataRepositoryImpl @Inject constructor(
    private val studySessionDao: StudySessionDao
) : StudyDataRepository {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override suspend fun getTodayStudySession(): StudySession? {
        val today = getCurrentDate()
        return studySessionDao.getStudySessionByDate(today)?.toDomainModel()
    }
    
    override suspend fun saveStudySession(session: StudySession) {
        studySessionDao.insertStudySession(session.toEntity())
    }
    
    override suspend fun updateStudySession(session: StudySession) {
        studySessionDao.updateStudySession(session.toEntity())
    }
    
    override suspend fun getStudySessionByDate(date: String): StudySession? {
        return studySessionDao.getStudySessionByDate(date)?.toDomainModel()
    }
    
    override suspend fun getStudySessionsByDateRange(startDate: String, endDate: String): List<StudySession> {
        return studySessionDao.getStudySessionsByDateRange(startDate, endDate).map { it.toDomainModel() }
    }
    
    override suspend fun getStudyStatistics(): StudyDataRepository.StudyStatistics {
        val totalStudyDays = studySessionDao.getTotalStudyDays()
        val totalStudyTime = studySessionDao.getTotalStudyTime() ?: 0L
        val totalWordsViewed = studySessionDao.getTotalWordsViewed() ?: 0
        val totalWordsMemorized = studySessionDao.getTotalWordsMemorized() ?: 0
        val averageWordsPerDay = studySessionDao.getAverageWordsPerDay() ?: 0.0
        val averageStudyTimePerDay = studySessionDao.getAverageStudyTimePerDay() ?: 0.0
        
        // 연속 학습 일수 계산
        val currentStreak = calculateCurrentStreak()
        
        return StudyDataRepository.StudyStatistics(
            totalStudyDays = totalStudyDays,
            totalStudyTime = totalStudyTime,
            totalWordsViewed = totalWordsViewed,
            totalWordsMemorized = totalWordsMemorized,
            averageWordsPerDay = averageWordsPerDay,
            averageStudyTimePerDay = averageStudyTimePerDay,
            currentStreak = currentStreak
        )
    }
    
    override fun observeTodayStudySession(): Flow<StudySession?> {
        val today = getCurrentDate()
        return studySessionDao.observeStudySessionByDate(today)
            .map { it?.toDomainModel() }
    }
    
    override suspend fun checkDailyGoalAchievement(): Boolean {
        val today = getCurrentDate()
        val todaySession = studySessionDao.getStudySessionByDate(today)
        
        // 일일 목표: 10개 단어 암기 또는 30분 학습
        return todaySession?.let { session ->
            session.wordsMemorized >= 10 || session.totalStudyTime >= 30L
        } ?: false
    }
    
    // Private helper methods
    
    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    private suspend fun calculateCurrentStreak(): Int {
        val allSessions = studySessionDao.getAllStudySessions()
        if (allSessions.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        var streak = 0
        var currentDate = calendar.time
        
        while (true) {
            val dateString = dateFormat.format(currentDate)
            val session = allSessions.find { it.date == dateString }
            
            if (session != null) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentDate = calendar.time
            } else {
                break
            }
        }
        
        return streak
    }
    
    // Entity to Domain Model conversion
    private fun StudySessionEntity.toDomainModel(): StudySession {
        return StudySession(
            id = id,
            date = date,
            startTime = startTime,
            endTime = endTime,
            wordsViewed = wordsViewed,
            wordsMemorized = wordsMemorized,
            ttsCount = ttsCount,
            totalStudyTime = totalStudyTime
        )
    }
    
    private fun StudySession.toEntity(): StudySessionEntity {
        return StudySessionEntity(
            id = id,
            date = date,
            startTime = startTime,
            endTime = endTime,
            wordsViewed = wordsViewed,
            wordsMemorized = wordsMemorized,
            ttsCount = ttsCount,
            totalStudyTime = totalStudyTime
        )
    }
} 