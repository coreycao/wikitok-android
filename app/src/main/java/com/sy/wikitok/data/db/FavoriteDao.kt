package com.sy.wikitok.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Dao
interface FavoriteDao {

    @Insert
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM tb_favorites WHERE id = :id")
    suspend fun removeFavorite(id:String)

    @Query("SELECT * FROM tb_favorites ORDER BY timestamp DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>
}