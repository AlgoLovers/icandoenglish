package com.na982.icandoenglish.domain.model

/**
 * 단어 학습 데이터 도메인 모델
 * 간격 반복 학습 알고리즘을 위한 데이터
 */
data class WordLearningData(
    val wordId: String,
    val grade: String,
    val memorizationCount: Int = 0, // 암기 성공 횟수 (0, 1, 2, 3...)
    val lastMemorizedDate: String? = null, // 마지막 암기 완료 날짜
    val nextReviewDate: String? = null, // 다음 복습 날짜
    val isNewWord: Boolean = true, // 신규 단어 여부
    val dailyNewWordDate: String? = null // 신규 단어로 배정된 날짜
) {
    /**
     * 암기가 완료되었는지 확인
     * 4회 암기 성공 시 완료로 간주
     */
    fun isMemorized(): Boolean = memorizationCount >= 4
    
    /**
     * 오늘 복습해야 하는지 확인
     */
    fun shouldReviewToday(today: String): Boolean {
        return nextReviewDate == today && memorizationCount > 0 && memorizationCount < 4
    }
    
    /**
     * 오늘 신규 단어인지 확인
     */
    fun isNewWordToday(today: String): Boolean {
        return dailyNewWordDate == today && memorizationCount == 0
    }
} 