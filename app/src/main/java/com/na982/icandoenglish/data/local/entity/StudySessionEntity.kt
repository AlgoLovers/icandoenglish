package com.na982.icandoenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 학습 세션 엔티티
 * 일일 학습 세션 데이터를 저장하는 테이블
 */
@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val startTime: Long,
    val endTime: Long? = null,
    val wordsViewed: Int = 0,
    val wordsMemorized: Int = 0,
    val ttsCount: Int = 0,
    val totalStudyTime: Long = 0
) 