package com.na982.icandoenglish.domain.model

/**
 * 단어 도메인 모델
 * 비즈니스 로직에서 사용하는 순수한 데이터 클래스
 */
data class Word(
    val id: String,
    val korean: String,
    val english: String,
    val grade: String,
    val sentences: List<Sentence>
) {
    data class Sentence(
        val korean: String,
        val english: String
    )
} 