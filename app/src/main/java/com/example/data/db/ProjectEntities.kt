package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.ModType
import com.example.data.model.ProjectItem
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val modType: String,
    val mcVersion: String,
    val modId: String,
    val packageName: String,
    val lastOpened: Long,
    val isFavorite: Boolean,
    val githubRepo: String
) {
    fun toProjectItem(): ProjectItem {
        val type = try { ModType.valueOf(modType) } catch (e: Exception) { ModType.FABRIC }
        return ProjectItem(
            id = id,
            name = name,
            path = path,
            modType = type,
            mcVersion = mcVersion,
            modId = modId,
            packageName = packageName,
            lastOpened = lastOpened,
            isFavorite = isFavorite,
            githubRepo = githubRepo
        )
    }

    companion object {
        fun fromProjectItem(item: ProjectItem): ProjectEntity {
            return ProjectEntity(
                id = item.id,
                name = item.name,
                path = item.path,
                modType = item.modType.name,
                mcVersion = item.mcVersion,
                modId = item.modId,
                packageName = item.packageName,
                lastOpened = item.lastOpened,
                isFavorite = item.isFavorite,
                githubRepo = item.githubRepo
            )
        }
    }
}

@Entity(tableName = "local_history")
data class LocalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val timestamp: Long,
    val content: String,
    val changeDescription: String
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastOpened DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("SELECT * FROM local_history WHERE filePath = :filePath ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryForFile(filePath: String): Flow<List<LocalHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LocalHistoryEntity)
}

@Database(entities = [ProjectEntity::class, LocalHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
