package com.soten.todo.data.local.db.dao

import androidx.room.*
import com.soten.todo.data.entity.TodoEntity

@Dao
interface TodoDao {

    @Query("SELECT * FROM TodoEntity")
    suspend fun getAll(): List<TodoEntity>

    @Query("SELECT * FROM TodoEntity WHERE id=:id")
    suspend fun getById(id: Long): TodoEntity?

    @Insert
    suspend fun insert(toDoEntity: TodoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDoEntityList: List<TodoEntity>)

    @Query("DELETE FROM TodoEntity WHERE id=:id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM TodoEntity")
    suspend fun deleteAll()

    @Update
    suspend fun update(toDoEntity: TodoEntity)

}