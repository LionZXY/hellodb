package technopark_db.controllers

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.web.bind.annotation.*
import technopark_db.data.NotesData
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
    fun addNotes(@RequestBody(required = false) notes: List<NoteModel>?): List<NoteModel> {
        if (notes == null) {
            return listOf()
        }
        return notesData.put(notes)
    }
}