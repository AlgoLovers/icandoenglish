# 🏗️ 영어 학습 앱 아키텍처 제안서

## 📋 현재 상황 분석

### 현재 문제점
- MainActivity가 980줄로 비대함
- UI, 비즈니스 로직, 데이터 관리가 혼재
- 테스트 어려움
- 확장성 부족

### 앱 특성
- 복잡한 단어 학습 알고리즘 (간격 반복)
- AI API 연동 예정
- 다양한 화면 확장 계획
- 데이터 지속성 중요

## 🎯 추천 아키텍처: MVVM + Clean Architecture + MVI

### 1. MVVM + Clean Architecture (기본)
```
📦 com.na982.icandoenglish
├── 📂 data
│   ├── 📂 repository
│   │   ├── WordRepositoryImpl.kt
│   │   ├── StudyDataRepositoryImpl.kt
│   │   └── AIServiceRepositoryImpl.kt
│   ├── 📂 local
│   │   ├── WordDatabase.kt (Room)
│   │   ├── WordDao.kt
│   │   └── StudyDataDao.kt
│   └── 📂 remote
│       ├── AIService.kt (Retrofit)
│       └── ApiResponse.kt
├── 📂 domain
│   ├── 📂 model
│   │   ├── Word.kt
│   │   ├── WordLearningData.kt
│   │   └── StudySession.kt
│   ├── 📂 repository (interface)
│   │   ├── WordRepository.kt
│   │   ├── StudyDataRepository.kt
│   │   └── AIServiceRepository.kt
│   └── 📂 usecase
│       ├── GetTodayWordsUseCase.kt
│       ├── MemorizeWordUseCase.kt
│       ├── GetStudyProgressUseCase.kt
│       └── GenerateAISentenceUseCase.kt
├── 📂 presentation
│   ├── 📂 screen
│   │   ├── 📂 learning
│   │   │   ├── LearningViewModel.kt
│   │   │   ├── LearningActivity.kt
│   │   │   └── LearningState.kt
│   │   ├── 📂 statistics
│   │   │   ├── StatisticsViewModel.kt
│   │   │   └── StatisticsActivity.kt
│   │   └── 📂 settings
│   │       ├── SettingsViewModel.kt
│   │       └── SettingsActivity.kt
│   └── 📂 component
│       ├── WordCard.kt
│       └── ProgressBar.kt
└── 📂 di
    └── AppModule.kt
```

### 2. MVI 패턴 추가 (상태 관리 개선)
```kotlin
// 상태 관리 예시
sealed class LearningState {
    object Loading : LearningState()
    data class Success(
        val currentWord: Word,
        val progress: Int,
        val totalWords: Int,
        val memorizedCount: Int
    ) : LearningState()
    data class Error(val message: String) : LearningState()
}

sealed class LearningIntent {
    object LoadTodayWords : LearningIntent()
    data class MemorizeWord(val word: String, val success: Boolean) : LearningIntent()
    object NextWord : LearningIntent()
    object PreviousWord : LearningIntent()
}
```

## 🚀 단계별 마이그레이션 계획

### Phase 1: 데이터 레이어 분리 (1-2주)
1. **Room Database 설정**
   - Word 엔티티 정의
   - WordLearningData 엔티티 정의
   - DAO 인터페이스 작성

2. **Repository 패턴 구현**
   - WordRepository 인터페이스
   - WordRepositoryImpl 구현
   - SharedPreferences → Room 마이그레이션

### Phase 2: 도메인 레이어 구현 (1주)
1. **UseCase 작성**
   - GetTodayWordsUseCase
   - MemorizeWordUseCase
   - GetStudyProgressUseCase

2. **비즈니스 로직 분리**
   - 간격 반복 알고리즘
   - 단어 배정 로직
   - 진행률 계산 로직

### Phase 3: Presentation 레이어 리팩토링 (1-2주)
1. **ViewModel 분리**
   - MainActivity → LearningViewModel
   - 상태 관리 개선

2. **UI 컴포넌트 분리**
   - WordCard 컴포넌트
   - ProgressBar 컴포넌트

### Phase 4: AI 통합 및 확장 (2-3주)
1. **AI 서비스 통합**
   - AIServiceRepository
   - 문장 생성 UseCase

2. **추가 화면 구현**
   - 통계 화면
   - 설정 화면

## 📊 기술 스택 추천

### 필수 라이브러리
```gradle
// Architecture Components
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2"

// Room Database
implementation "androidx.room:room-runtime:2.6.0"
implementation "androidx.room:room-ktx:2.6.0"
kapt "androidx.room:room-compiler:2.6.0"

// Dependency Injection
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"

// Network (AI API용)
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
```

### 선택적 라이브러리
```gradle
// UI (향후 Jetpack Compose 도입 시)
implementation "androidx.compose.ui:ui:1.5.4"
implementation "androidx.compose.material3:material3:1.1.2"

// Testing
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
testImplementation "androidx.arch.core:core-testing:2.2.0"
```

## 🎯 장점

### 1. 테스트 용이성
- UseCase 단위 테스트 가능
- ViewModel 테스트 가능
- Repository 테스트 가능

### 2. 유지보수성
- 각 레이어가 명확한 책임
- 기능 추가/수정이 쉬움
- 버그 추적이 용이

### 3. 확장성
- AI 기능 추가 용이
- 새로운 화면 추가 쉬움
- 다른 플랫폼으로 확장 가능

### 4. AI 리팩토링 친화적
- 명확한 구조로 AI가 이해하기 쉬움
- 각 컴포넌트가 독립적
- 점진적 리팩토링 가능

## ⚠️ 고려사항

### 초기 설정 복잡도
- DI 설정 필요
- Room Database 설정 필요
- 테스트 환경 구성 필요

### 학습 곡선
- Clean Architecture 개념 학습
- MVI 패턴 이해
- Coroutines/Flow 학습

## 🎯 결론

**MVVM + Clean Architecture + MVI** 조합을 강력히 추천합니다.

현재 앱의 복잡성과 향후 확장 계획을 고려할 때, 이 아키텍처가 가장 적합합니다. 초기 설정에 시간이 걸리더라도, 장기적으로 큰 이익을 가져올 것입니다.

특히 AI와의 협업을 고려할 때, 명확한 구조는 매우 중요합니다. 