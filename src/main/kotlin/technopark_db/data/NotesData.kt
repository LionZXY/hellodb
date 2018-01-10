package technopark_db.data

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import technopark_db.models.NotFoundNoteException
import technopark_db.models.NoteModel
import java.sql.Connection
import java.sql.Statement

@Component
class NotesData(private val template: JdbcTemplate) {
    companion object {
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_COMPLETED = "completed"
        private const val COLUMN_ID = "id"

        val NOTEMAPPER = RowMapper<NoteModel> { row, _ ->
            NoteModel(row.getString(COLUMN_DESCRIPTION),
                    row.getBoolean(COLUMN_COMPLETED),
                    row.getInt(COLUMN_ID))
        }
    }

    fun get(since: Int, desc: Boolean, limit: Int): List<NoteModel> {
        var sql = "SELECT * FROM notes "
        val argsObject = ArrayList<Any>()

        if (since != -1) {
            sql += if (desc) {
                "WHERE id < ? "
            } else {
                "WHERE id > ? "
            }
            argsObject.add(since)
        }

        sql += if (desc) {
            "ORDER BY id DESC "
        } else {
            "ORDER BY id ASC "
        }

        if (limit != -1) {
            sql += "LIMIT ?"
            argsObject.add(limit)
        }

        return template.query(sql, argsObject.toArray(), NOTEMAPPER)
    }

    fun getById(id: Int): NoteModel {
        return template.queryForObject("SELECT * FROM notes WHERE id = ?;", NOTEMAPPER, id)
    }

    fun put(notes: List<NoteModel>): List<NoteModel> {
        val connection = template.dataSource.connection
        try {
            connection.autoCommit = false
            return putThrowables(connection, notes)
        } finally {
            connection.autoCommit = true
            connection.close()
        }
    }

    private fun putThrowables(connection: Connection, notes: List<NoteModel>): List<NoteModel> {
        val idsResultSet = template.queryForRowSet("SELECT nextval('notes_id_seq') FROM generate_series(1, ?);", notes.size)

        val ps = connection.prepareStatement("INSERT INTO notes(id, description, completed) VALUES (?, ?, ?);", Statement.NO_GENERATED_KEYS)

        notes.forEach({
            ps.apply {
                idsResultSet.next()
                it.id = idsResultSet.getInt(1)
                setInt(1, it.id)
                setString(2, it.description)
                setBoolean(3, it.completed)
                addBatch()
            }
        })

        try {
            val insertCount = ps.executeBatch()
            if (insertCount.contains(0)) {
                throw RuntimeException("Ошибка при заполнение")
            }
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        }

        return notes
    }

    fun editNote(note: NoteModel): NoteModel {
        return template.queryForObject("UPDATE notes SET (description, completed) = (coalesce(?, description), coalesce(?, completed)) WHERE id = ? RETURNING *;",
                NOTEMAPPER,
                note.description,
                note.completed,
                note.id)
    }

    fun removeNote(id: Int) {
        val count = template.update("DELETE FROM notes WHERE id = ?;", id)
        if (count == 0) {
            throw NotFoundNoteException()
        }
    }
}