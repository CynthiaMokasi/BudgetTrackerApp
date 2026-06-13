package com.example.budgettrackerapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class BadgeRepository private constructor(private val db: AppDatabase) {

    private val badgeDao = db.badgeDao()

    suspend fun awardBadge(badge: Badge) = badgeDao.insert(badge)
    fun getBadges(): Flow<List<Badge>> = badgeDao.getAllBadges()
    suspend fun getBadge(name: String): Badge? = badgeDao.getBadge(name)

    suspend fun ensureDefaultBadges(defaults: List<Badge>) {
        defaults.forEach { badge ->
            val existing = badgeDao.getBadge(badge.name)
            if (existing == null) {
                badgeDao.insert(badge)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BadgeRepository? = null

        fun getInstance(context: Context): BadgeRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BadgeRepository(AppDatabase.getDatabase(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
