package com.sy.wikitok.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sy.wikitok.data.DEFAULT_LANGUAGE
import com.sy.wikitok.data.DEFAULT_LANG_ID
import com.sy.wikitok.data.Langs
import com.sy.wikitok.data.Language
import kotlinx.coroutines.flow.map

/**
 * @author Yeung
 * @date 2025/3/31
 *
 * Repo for user info and settings.
 *
 */
class UserRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val KEY_LANG = stringPreferencesKey("lang")
    }

    suspend fun updateLanguage(lang: Language) {
        dataStore.edit { preference ->
            preference[KEY_LANG] = lang.id
        }
    }

    fun observeLanguageSetting() = dataStore.data.map { preference ->
        val langId = preference[KEY_LANG] ?: DEFAULT_LANG_ID
        Langs[langId] ?: DEFAULT_LANGUAGE
    }
}