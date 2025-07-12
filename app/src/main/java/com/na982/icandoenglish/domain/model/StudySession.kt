package com.na982.icandoenglish.domain.model

/**
 * 학습 세션 도메인 모델
 * 학습 진행 상황을 추적하는 데이터
 */
data class StudySession(
    val id: String,
    val date: String,
    val startTime: Long,
    val endTime: Long? = null,
    val wordsViewed: Int = 0,
    val wordsMemorized: Int = 0,
    val ttsCount: Int = 0,
    val totalStudyTime: Long = 0
) {
    /**
     * 세션이 완료되었는지 확인
     */
    fun isCompleted(): Boolean = endTime != null
    
    /**
     * 학습 시간 계산 (분 단위)
     */
    fun getStudyTimeMinutes(): Long {
        val end = endTime ?: System.currentTimeMillis()
        return (end - startTime) / (1000 * 60)
    }
    
    /**
     * 평균 학습 시간 계산 (단어당 분)
     */
    fun getAverageTimePerWord(): Double {
        return if (wordsViewed > 0) {
            getStudyTimeMinutes().toDouble() / wordsViewed
        } else {
            0.0
        }
    }
} 