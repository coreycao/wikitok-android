package com.sy.wikitok.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sy.wikitok.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * @author Yeung
 * @date 2025/7/31
 */
@Dao
interface LangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLanguages(languages: List<LanguageEntity>): List<Long>

    @Transaction
    suspend fun saveAndMergeLanguages(languages: List<LanguageEntity>) {
        val cur = observeLanguages().first()

        // 留下该留的
        val filteredList = cur.filter { old ->
            languages.any { it.id == old.id }
        }

        Logger.d(tag = "LangDao", message = "saveAndMergeLanguages: current size: ${cur.size}, filtered size: ${filteredList.size}, new size: ${languages.size}")

        val mergedList = languages.map { new ->
            val cur = filteredList.find { it.id == new.id }
            return@map if (cur != null) {
                new.copy(selected = cur.selected)
            } else {
                new
            }
        }

        Logger.d(tag = "LangDao", message = "saveAndMergeLanguages: merged size: ${mergedList.size}")
        saveLanguages(mergedList)
    }

    @Query("SELECT * FROM tb_languages")
    fun observeLanguages(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM tb_languages WHERE selected = 1 LIMIT 1")
    fun observeSelectedLanguage(): Flow<LanguageEntity?>

    @Update
    suspend fun updateSelectedLanguage(languages: List<LanguageEntity>)

    @Transaction
    suspend fun switchSelectedLanguage(from: LanguageEntity, to: LanguageEntity) {
        updateSelectedLanguage(listOf(from.copy(selected = 0), to.copy(selected = 1)))
    }

}