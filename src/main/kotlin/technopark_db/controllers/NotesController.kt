package technopark_db.controllers

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import technopark_db.data.NotesData
import technopark_db.models.NotFoundNoteException
import technopark_db.models.NoteModel

@RestController
class NotesController(private val notesData: NotesData) {

    @GetMapping("/api")
    fun getNotes(
            @RequestParam(required = false, defaultValue = "-1") limit: Int,
            @RequestParam(required = false, defaultValue = "-1") since: Int = -1,
            @RequestParam(required = false, defaultValue = "false") desc: Boolean): List<NoteModel> {
        return try {
            notesData.get(since, desc, limit)
        } catch (e: EmptyResultDataAccessException) {
            listOf()
        }
    }

    @PostMapping("/api")
    fun addNotes(@RequestBody(required = false) notes: List<NoteModel>?): ResponseEntity<List<NoteModel>> {
        var list = listOf<NoteModel>()
        if (notes != null) {
            list = notesData.put(notes)
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(list)
    }

    @GetMapping("/api/{id}")
    fun getNoteById(@PathVariable id: Int): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(notesData.getById(id))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Запись не найдена.")
        }
    }

    @PutMapping("/api/{id}")
    fun editNote(@PathVariable id: Int, @RequestBody(required = false) note: NoteModel?): ResponseEntity<Any> {
        note?.id = id
        if (note == null || note.id == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Запись не найдена.")
        }

        return try {
            ResponseEntity.ok(notesData.editNote(note))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Запись не найдена.")
        }
    }

    @DeleteMapping("/api/{id}")
    fun removeNote(@PathVariable id: Int): ResponseEntity<Void> {
        return try {
            notesData.removeNote(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: NotFoundNoteException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}