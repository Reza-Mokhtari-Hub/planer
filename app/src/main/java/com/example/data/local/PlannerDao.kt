package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, time ASC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks WHERE completed = 1")
    suspend fun deleteCompletedTasks()

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("UPDATE tasks SET completed = 0")
    suspend fun uncheckAllTasks()

    // Categories
    @Query("SELECT * FROM categories")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :catId")
    suspend fun deleteCategoryById(catId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // Reflection
    @Query("SELECT * FROM reflection WHERE id = 1")
    fun observeReflection(): Flow<ReflectionEntity?>

    @Query("SELECT * FROM reflection WHERE id = 1")
    suspend fun getReflectionOnce(): ReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReflection(reflection: ReflectionEntity)

    // Archives
    @Query("SELECT * FROM archives ORDER BY date DESC")
    fun observeArchives(): Flow<List<ArchiveEntity>>

    @Query("SELECT * FROM archives")
    suspend fun getArchivesOnce(): List<ArchiveEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchives(archives: List<ArchiveEntity>)

    @Query("DELETE FROM archives WHERE id = :archiveId")
    suspend fun deleteArchiveById(archiveId: String)

    @Query("DELETE FROM archives")
    suspend fun deleteAllArchives()

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis ASC, id ASC")
    fun observeChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    @Transaction
    suspend fun replaceAllTasks(newTasks: List<TaskEntity>) {
        deleteAllTasks()
        insertTasks(newTasks)
    }

    @Transaction
    suspend fun replaceFullBackup(
        tasks: List<TaskEntity>,
        categories: List<CategoryEntity>,
        reflection: ReflectionEntity,
        archives: List<ArchiveEntity>
    ) {
        deleteAllTasks()
        insertTasks(tasks)
        if (categories.isNotEmpty()) {
            deleteAllCategories()
            insertCategories(categories)
        }
        upsertReflection(reflection)
        deleteAllArchives()
        if (archives.isNotEmpty()) {
            insertArchives(archives)
        }
    }
}
