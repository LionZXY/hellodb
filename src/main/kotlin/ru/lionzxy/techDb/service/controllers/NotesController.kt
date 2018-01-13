package ru.lionzxy.techDb.service.controllers

import javassist.tools.web.BadHttpRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import ru.lionzxy.techDb.hello.api.ApiApi
import ru.lionzxy.techDb.hello.model.Item
import ru.lionzxy.techDb.service.data.NotesData
import ru.lionzxy.techDb.service.models.NotFoundNoteException
import java.math.BigDecimal


@Controller
class NotesController : ApiApi {

    @Autowired
    private lateinit var notesData: NotesData

    override fun addMulti(body: MutableList<Item>?): ResponseEntity<MutableList<Item>> {
        var list = listOf<Item>()
        if (body != null) {
            list = notesData.put(body)
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(list.toMutableList())
    }

    override fun destroyOne(id: BigDecimal?): ResponseEntity<Void> {
        return try {
            notesData.removeNote(id ?: throw BadHttpRequest())
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } catch (e: NotFoundNoteException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    override fun find(since: BigDecimal?, desc: Boolean?, limit: BigDecimal?): ResponseEntity<MutableList<Item>> {
        val list = try {
            notesData.get(since, desc ?: false, limit)
        } catch (e: EmptyResultDataAccessException) {
            listOf<Item>()
        }

        return ResponseEntity.ok(list.toMutableList())
    }

    override fun getOne(id: BigDecimal?): ResponseEntity<Item> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(notesData.getById(id ?: throw BadHttpRequest()))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    override fun updateOne(id: BigDecimal?, body: Item?): ResponseEntity<Item> {
        body?.id = (id ?: throw BadHttpRequest()).toLong()
        if (body == null || body.id == -1L) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        return try {
            ResponseEntity.ok(notesData.editNote(body))
        } catch (e: EmptyResultDataAccessException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

}