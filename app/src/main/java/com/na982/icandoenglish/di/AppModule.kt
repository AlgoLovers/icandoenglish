package com.na982.icandoenglish.di

import android.content.Context
import com.na982.icandoenglish.data.local.WordDatabase
import com.na982.icandoenglish.data.local.WordDao
import com.na982.icandoenglish.data.local.WordLearningDataDao
import com.na982.icandoenglish.data.local.StudySessionDao
import com.na982.icandoenglish.data.repository.WordRepositoryImpl
import com.na982.icandoenglish.data.repository.StudyDataRepositoryImpl
import com.na982.icandoenglish.domain.repository.WordRepository
import com.na982.icandoenglish.domain.repository.StudyDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI 모듈
 * 의존성 주입을 위한 모듈 설정
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideWordDatabase(@ApplicationContext context: Context): WordDatabase {
        return WordDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideWordDao(database: WordDatabase): WordDao {
        return database.wordDao()
    }
    
    @Provides
    @Singleton
    fun provideWordLearningDataDao(database: WordDatabase): WordLearningDataDao {
        return database.wordLearningDataDao()
    }
    
    @Provides
    @Singleton
    fun provideStudySessionDao(database: WordDatabase): StudySessionDao {
        return database.studySessionDao()
    }
    
    @Provides
    @Singleton
    fun provideWordRepository(
        wordDao: WordDao,
        wordLearningDataDao: WordLearningDataDao
    ): WordRepository {
        return WordRepositoryImpl(wordDao, wordLearningDataDao)
    }
    
    @Provides
    @Singleton
    fun provideStudyDataRepository(
        studySessionDao: StudySessionDao
    ): StudyDataRepository {
        return StudyDataRepositoryImpl(studySessionDao)
    }
} 