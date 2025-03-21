package com.sy.wikitok.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Dao
interface FavoriteDao {

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM tb_favorites WHERE id = :id")
    suspend fun removeFavorite(id:String)

    @Query("SELECT * FROM tb_favorites ORDER BY timestamp DESC")
    suspend fun readAllFavorites(): List<FavoriteEntity>
}