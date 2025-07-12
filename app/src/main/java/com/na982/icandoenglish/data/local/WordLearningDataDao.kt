package com.na982.icandoenglish.data.local

import androidx.room.*
import com.na982.icandoenglish.data.local.entity.WordLearningDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * 단어 학습 데이터 접근 객체 (DAO)
 * 간격 반복 학습 데이터를 조작하는 인터페이스
 */
@Dao
interface WordLearningDataDao {
    
    @Query("SELECT * FROM word_learning_data WHERE wordId = :wordId AND grade = :grade")
    suspend fun getWordLearningData(wordId: String, grade: String): WordLearningDataEntity?
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade")
    suspend fun getAllLearningDataByGrade(grade: String): List<WordLearningDataEntity>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade AND memorizationCount >= 4")
    suspend fun getMemorizedWords(grade: String): List<WordLearningDataEntity>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade AND memorizationCount < 4")
    suspend fun getUnmemorizedWords(grade: String): List<WordLearningDataEntity>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade AND dailyNewWordDate = :date AND memorizationCount = 0")
    suspend fun getNewWordsForDate(grade: String, date: String): List<WordLearningDataEntity>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade AND nextReviewDate = :date AND memorizationCount > 0 AND memorizationCount < 4")
    suspend fun getReviewWordsForDate(grade: String, date: String): List<WordLearningDataEntity>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade AND dailyNewWordDate IS NULL")
    suspend fun getUnassignedWords(grade: String): List<WordLearningDataEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordLearningData(data: WordLearningDataEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordLearningDataList(dataList: List<WordLearningDataEntity>)
    
    @Update
    suspend fun updateWordLearningData(data: WordLearningDataEntity)
    
    @Delete
    suspend fun deleteWordLearningData(data: WordLearningDataEntity)
    
    @Query("DELETE FROM word_learning_data WHERE grade = :grade")
    suspend fun deleteAllLearningDataByGrade(grade: String)
    
    // Flow를 사용한 실시간 데이터 감지
    @Query("SELECT * FROM word_learning_data WHERE wordId = :wordId AND grade = :grade")
    fun observeWordLearningData(wordId: String, grade: String): Flow<WordLearningDataEntity?>
    
    @Query("SELECT * FROM word_learning_data WHERE grade = :grade")
    fun observeAllLearningDataByGrade(grade: String): Flow<List<WordLearningDataEntity>>
} 