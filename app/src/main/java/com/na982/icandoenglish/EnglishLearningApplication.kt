package com.na982.icandoenglish

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 영어 학습 앱의 Application 클래스
 * Hilt DI를 위한 설정
 */
@HiltAndroidApp
class EnglishLearningApplication : Application() 