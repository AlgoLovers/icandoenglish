package com.na982.icandoenglish.data.repository

import com.na982.icandoenglish.data.local.WordDao
import com.na982.icandoenglish.data.local.WordLearningDataDao
import com.na982.icandoenglish.data.local.entity.WordEntity
import com.na982.icandoenglish.data.local.entity.WordLearningDataEntity
import com.na982.icandoenglish.domain.model.Word
import com.na982.icandoenglish.domain.model.WordLearningData
import com.na982.icandoenglish.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * WordRepository 구현체
 * Room Database를 사용하여 단어 데이터를 관리
 */
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val wordLearningDataDao: WordLearningDataDao
) : WordRepository {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override suspend fun getAllWords(): List<Word> {
        return wordDao.getAllWords().map { it.toDomainModel() }
    }
    
    override suspend fun getWordsByGrade(grade: String): List<Word> {
        return wordDao.getWordsByGrade(grade).map { it.toDomainModel() }
    }
    
    override suspend fun getWordLearningData(wordId: String, grade: String): WordLearningData? {
        return wordLearningDataDao.getWordLearningData(wordId, grade)?.toDomainModel()
    }
    
    override suspend fun saveWordLearningData(learningData: WordLearningData) {
        wordLearningDataDao.insertWordLearningData(learningData.toEntity())
    }
    
    override suspend fun getTodayNewWords(grade: String, maxCount: Int): List<Word> {
        val today = getCurrentDate()
        val learningDataList = wordLearningDataDao.getNewWordsForDate(grade, today)
        
        return learningDataList
            .take(maxCount)
            .mapNotNull { learningData ->
                wordDao.getWordById(learningData.wordId)?.toDomainModel()
            }
    }
    
    override suspend fun getTodayReviewWords(grade: String): List<Word> {
        val today = getCurrentDate()
        val learningDataList = wordLearningDataDao.getReviewWordsForDate(grade, today)
        
        return learningDataList.mapNotNull { learningData ->
            wordDao.getWordById(learningData.wordId)?.toDomainModel()
        }
    }
    
    override suspend fun assignNewWordsForToday(grade: String, maxCount: Int) {
        val today = getCurrentDate()
        
        // 이미 배정된 단어가 있는지 확인
        val existingNewWords = wordLearningDataDao.getNewWordsForDate(grade, today)
        if (existingNewWords.isNotEmpty()) {
            return // 이미 배정됨
        }
        
        // 배정되지 않은 단어들 가져오기
        val unassignedWords = wordLearningDataDao.getUnassignedWords(grade)
        
        if (unassignedWords.size >= maxCount) {
            // 정확히 maxCount개만 배정
            val wordsToAssign = unassignedWords.take(maxCount)
            
            val learningDataList = wordsToAssign.map { learningData ->
                learningData.copy(
                    dailyNewWordDate = today,
                    isNewWord = true
                )
            }
            
            wordLearningDataDao.insertWordLearningDataList(learningDataList)
        }
    }
    
    override suspend fun processWordMemorization(wordId: String, grade: String, success: Boolean) {
        val currentData = wordLearningDataDao.getWordLearningData(wordId, grade)
            ?: WordLearningDataEntity(wordId, grade)
        
        val updatedData = if (success) {
            processSuccessfulMemorization(currentData)
        } else {
            processFailedMemorization(currentData)
        }
        
        wordLearningDataDao.insertWordLearningData(updatedData)
    }
    
    override suspend fun getMemorizedWords(grade: String): List<Word> {
        val learningDataList = wordLearningDataDao.getMemorizedWords(grade)
        return learningDataList.mapNotNull { learningData ->
            wordDao.getWordById(learningData.wordId)?.toDomainModel()
        }
    }
    
    override suspend fun getUnmemorizedWords(grade: String): List<Word> {
        val learningDataList = wordLearningDataDao.getUnmemorizedWords(grade)
        return learningDataList.mapNotNull { learningData ->
            wordDao.getWordById(learningData.wordId)?.toDomainModel()
        }
    }
    
    override fun observeWordLearningData(wordId: String, grade: String): Flow<WordLearningData?> {
        return wordLearningDataDao.observeWordLearningData(wordId, grade)
            .map { it?.toDomainModel() }
    }
    
    // Private helper methods
    
    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }
    
    private fun processSuccessfulMemorization(data: WordLearningDataEntity): WordLearningDataEntity {
        val newMemorizationCount = data.memorizationCount + 1
        val reviewIntervals = listOf(3, 6, 12, 24)
        
        val nextReviewInterval = if (newMemorizationCount <= reviewIntervals.size) {
            reviewIntervals[newMemorizationCount - 1]
        } else {
            reviewIntervals.last()
        }
        
        val nextReviewDate = calculateNextReviewDate(nextReviewInterval)
        val today = getCurrentDate()
        
        return data.copy(
            memorizationCount = newMemorizationCount,
            lastMemorizedDate = today,
            nextReviewDate = nextReviewDate
        )
    }
    
    private fun processFailedMemorization(data: WordLearningDataEntity): WordLearningDataEntity {
        val tomorrow = calculateNextReviewDate(1)
        
        return data.copy(
            memorizationCount = 0,
            nextReviewDate = tomorrow
        )
    }
    
    private fun calculateNextReviewDate(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return dateFormat.format(calendar.time)
    }
    
    // Entity to Domain Model conversion
    private fun WordEntity.toDomainModel(): Word {
        val sentenceList: List<String> = try {
            Gson().fromJson(sentences, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return Word(
            id = id,
            korean = korean,
            english = english,
            grade = grade,
            sentences = sentenceList.map { sentence ->
                // 문장은 "한국어|영어" 형태로 저장되어 있다고 가정
                val parts = sentence.split("|")
                if (parts.size == 2) {
                    Word.Sentence(parts[0], parts[1])
                } else {
                    Word.Sentence("", sentence)
                }
            }
        )
    }
    
    private fun WordLearningDataEntity.toDomainModel(): WordLearningData {
        return WordLearningData(
            wordId = wordId,
            grade = grade,
            memorizationCount = memorizationCount,
            lastMemorizedDate = lastMemorizedDate,
            nextReviewDate = nextReviewDate,
            isNewWord = isNewWord,
            dailyNewWordDate = dailyNewWordDate
        )
    }
    
    private fun WordLearningData.toEntity(): WordLearningDataEntity {
        return WordLearningDataEntity(
            wordId = wordId,
            grade = grade,
            memorizationCount = memorizationCount,
            lastMemorizedDate = lastMemorizedDate,
            nextReviewDate = nextReviewDate,
            isNewWord = isNewWord,
            dailyNewWordDate = dailyNewWordDate
        )
    }

    private fun Word.toEntity(): WordEntity {
        val sentenceList = sentences.map { "${'$'}{it.korean}|${'$'}{it.english}" }
        val sentencesJson = Gson().toJson(sentenceList)
        return WordEntity(
            id = id,
            korean = korean,
            english = english,
            grade = grade,
            sentences = sentencesJson
        )
    }
} 