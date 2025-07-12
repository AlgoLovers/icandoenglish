package com.na982.icandoenglish.data.local

import androidx.room.*
import com.na982.icandoenglish.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 단어 데이터 접근 객체 (DAO)
 * Room Database에서 단어 데이터를 조작하는 인터페이스
 */
@Dao
interface WordDao {
    
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE grade = :grade")
    suspend fun getWordsByGrade(grade: String): List<WordEntity>
    
    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWordById(wordId: String): WordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)
    
    @Update
    suspend fun updateWord(word: WordEntity)
    
    @Delete
    suspend fun deleteWord(word: WordEntity)
    
    @Query("DELETE FROM words")
    suspend fun deleteAllWords()
    
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
    
    @Query("SELECT COUNT(*) FROM words WHERE grade = :grade")
    suspend fun getWordCountByGrade(grade: String): Int
    
    // Flow를 사용한 실시간 데이터 감지
    @Query("SELECT * FROM words WHERE grade = :grade")
    fun observeWordsByGrade(grade: String): Flow<List<WordEntity>>
    
    @Query("SELECT * FROM words WHERE id = :wordId")
    fun observeWordById(wordId: String): Flow<WordEntity?>
} 