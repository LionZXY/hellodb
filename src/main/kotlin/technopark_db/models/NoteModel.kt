package technopark_db.models

import com.fasterxml.jackson.annotation.JsonProperty

class NoteModel(
        @JsonProperty("description")
        var description: String?,
        @JsonProperty("completed")
        var completed: Boolean = false,
        @JsonProperty("id")
        var id: Int = -1) {
    constructor() : this(null)
}