package com.na982.icandoenglish

import java.util.*

data class StudySession(
    val date: String, // YYYY-MM-DD 형식
    val studyTimeSeconds: Long = 0,
    val ttsCount: Int = 0,
    val wordsViewed: Int = 0,
    val wordsMemorized: Int = 0,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

data class DailyStudyData(
    val date: String,
    var totalStudyTime: Long = 0,
    var totalTtsCount: Int = 0,
    var totalWordsViewed: Int = 0,
    var totalWordsMemorized: Int = 0,
    var newWordsAssigned: Int = 0, // 해당 날짜에 배정된 신규 단어 수
    var reviewWordsCompleted: Int = 0, // 해당 날짜에 완료한 복습 단어 수
    val sessions: MutableList<StudySession> = mutableListOf()
) 