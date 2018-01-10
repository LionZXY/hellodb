package technopark_db.utils


fun String.isNumeric() = this.toCharArray().none { it !in '0'..'9' }
