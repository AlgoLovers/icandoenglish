package com.na982.icandoenglish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 단어 데이터베이스 엔티티
 * Room에서 사용하는 단어 테이블
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey
    val id: String,
    val korean: String,
    val english: String,
    val grade: String,
    val sentences: String // JSON 문자열로 저장
) 