package com.na982.icandoenglish.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.na982.icandoenglish.data.local.entity.WordEntity
import com.na982.icandoenglish.data.local.entity.WordLearningDataEntity
import com.na982.icandoenglish.data.local.entity.StudySessionEntity
import com.na982.icandoenglish.data.local.converter.DateConverter

/**
 * Room Database 설정
 * 단어, 학습 데이터, 학습 세션을 저장하는 로컬 데이터베이스
 */
@Database(
    entities = [
        WordEntity::class,
        WordLearningDataEntity::class,
        StudySessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class WordDatabase : RoomDatabase() {
    
    abstract fun wordDao(): WordDao
    abstract fun wordLearningDataDao(): WordLearningDataDao
    abstract fun studySessionDao(): StudySessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null
        
        fun getDatabase(context: Context): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordDatabase::class.java,
                    "word_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 