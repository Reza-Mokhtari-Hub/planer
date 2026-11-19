package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val day: String,
    val time: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val completed: Boolean,
    val difficulty: String,
    val timeSlot: String,
    val createdAt: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val icon: String
)

@Entity(tableName = "reflection")
data class ReflectionEntity(
    @PrimaryKey val id: Int = 1,
    val wins: String,
    val challenges: String,
    val improvements: String,
    val rating: Int
)

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey val id: String,
    val date: String,
    val title: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val rate: Int,
    val tasksSnapshotJson: String,
    val reflectionJson: String?
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val content: String,
    val presetTag: String?,
    val timestampMillis: Long
)
