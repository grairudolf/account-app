package com.example.domain.models

data class BibleBookInfo(
    val name: String,
    val chapters: Int,
    val testament: String
)

object BibleMetadata {
    val BOOKS = listOf(
        // Old Testament
        BibleBookInfo("Genesis", 50, "OT"),
        BibleBookInfo("Exodus", 40, "OT"),
        BibleBookInfo("Leviticus", 27, "OT"),
        BibleBookInfo("Numbers", 36, "OT"),
        BibleBookInfo("Deuteronomy", 34, "OT"),
        BibleBookInfo("Joshua", 24, "OT"),
        BibleBookInfo("Judges", 21, "OT"),
        BibleBookInfo("Ruth", 4, "OT"),
        BibleBookInfo("1 Samuel", 31, "OT"),
        BibleBookInfo("2 Samuel", 24, "OT"),
        BibleBookInfo("1 Kings", 22, "OT"),
        BibleBookInfo("2 Kings", 25, "OT"),
        BibleBookInfo("1 Chronicles", 29, "OT"),
        BibleBookInfo("2 Chronicles", 36, "OT"),
        BibleBookInfo("Ezra", 10, "OT"),
        BibleBookInfo("Nehemiah", 13, "OT"),
        BibleBookInfo("Esther", 10, "OT"),
        BibleBookInfo("Job", 42, "OT"),
        BibleBookInfo("Psalms", 150, "OT"),
        BibleBookInfo("Proverbs", 31, "OT"),
        BibleBookInfo("Ecclesiastes", 12, "OT"),
        BibleBookInfo("Song of Solomon", 8, "OT"),
        BibleBookInfo("Isaiah", 66, "OT"),
        BibleBookInfo("Jeremiah", 52, "OT"),
        BibleBookInfo("Lamentations", 5, "OT"),
        BibleBookInfo("Ezekiel", 48, "OT"),
        BibleBookInfo("Daniel", 12, "OT"),
        BibleBookInfo("Hosea", 14, "OT"),
        BibleBookInfo("Joel", 3, "OT"),
        BibleBookInfo("Amos", 9, "OT"),
        BibleBookInfo("Obadiah", 1, "OT"),
        BibleBookInfo("Jonah", 4, "OT"),
        BibleBookInfo("Micah", 7, "OT"),
        BibleBookInfo("Nahum", 3, "OT"),
        BibleBookInfo("Habakkuk", 3, "OT"),
        BibleBookInfo("Zephaniah", 3, "OT"),
        BibleBookInfo("Haggai", 2, "OT"),
        BibleBookInfo("Zechariah", 14, "OT"),
        BibleBookInfo("Malachi", 4, "OT"),

        // New Testament
        BibleBookInfo("Matthew", 28, "NT"),
        BibleBookInfo("Mark", 16, "NT"),
        BibleBookInfo("Luke", 24, "NT"),
        BibleBookInfo("John", 21, "NT"),
        BibleBookInfo("Acts", 28, "NT"),
        BibleBookInfo("Romans", 16, "NT"),
        BibleBookInfo("1 Corinthians", 16, "NT"),
        BibleBookInfo("2 Corinthians", 13, "NT"),
        BibleBookInfo("Galatians", 6, "NT"),
        BibleBookInfo("Ephesians", 6, "NT"),
        BibleBookInfo("Philippians", 4, "NT"),
        BibleBookInfo("Colossians", 4, "NT"),
        BibleBookInfo("1 Thessalonians", 5, "NT"),
        BibleBookInfo("2 Thessalonians", 3, "NT"),
        BibleBookInfo("1 Timothy", 6, "NT"),
        BibleBookInfo("2 Timothy", 4, "NT"),
        BibleBookInfo("Titus", 3, "NT"),
        BibleBookInfo("Philemon", 1, "NT"),
        BibleBookInfo("Hebrews", 13, "NT"),
        BibleBookInfo("James", 5, "NT"),
        BibleBookInfo("1 Peter", 5, "NT"),
        BibleBookInfo("2 Peter", 3, "NT"),
        BibleBookInfo("1 John", 5, "NT"),
        BibleBookInfo("2 John", 1, "NT"),
        BibleBookInfo("3 John", 1, "NT"),
        BibleBookInfo("Jude", 1, "NT"),
        BibleBookInfo("Revelation", 22, "NT")
    )

    fun getChaptersForBook(bookName: String): Int {
        return BOOKS.find { it.name.equals(bookName, ignoreCase = true) }?.chapters ?: 50
    }
}
