package com.na982.icandoenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 단어 학습 데이터 엔티티
 * 간격 반복 학습을 위한 데이터 테이블
 */
@Entity(tableName = "word_learning_data")
data class WordLearningDataEntity(
    @PrimaryKey
    val wordId: String,
    val grade: String,
    val memorizationCount: Int = 0,
    val lastMemorizedDate: String? = null,
    val nextReviewDate: String? = null,
    val isNewWord: Boolean = true,
    val dailyNewWordDate: String? = null
) 