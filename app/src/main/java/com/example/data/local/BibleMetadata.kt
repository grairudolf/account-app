package com.example.data.local

data class BibleBook(
    val name: String,
    val totalChapters: Int,
    val isOldTestament: Boolean
)

object BibleMetadata {
    val BOOKS = listOf(
        // Old Testament (39 books, 929 chapters)
        BibleBook("Genesis", 50, true),
        BibleBook("Exodus", 40, true),
        BibleBook("Leviticus", 27, true),
        BibleBook("Numbers", 36, true),
        BibleBook("Deuteronomy", 34, true),
        BibleBook("Joshua", 24, true),
        BibleBook("Judges", 21, true),
        BibleBook("Ruth", 4, true),
        BibleBook("1 Samuel", 31, true),
        BibleBook("2 Samuel", 24, true),
        BibleBook("1 Kings", 22, true),
        BibleBook("2 Kings", 25, true),
        BibleBook("1 Chronicles", 29, true),
        BibleBook("2 Chronicles", 36, true),
        BibleBook("Ezra", 10, true),
        BibleBook("Nehemiah", 13, true),
        BibleBook("Esther", 10, true),
        BibleBook("Job", 42, true),
        BibleBook("Psalms", 150, true),
        BibleBook("Proverbs", 31, true),
        BibleBook("Ecclesiastes", 12, true),
        BibleBook("Song of Solomon", 8, true),
        BibleBook("Isaiah", 66, true),
        BibleBook("Jeremiah", 52, true),
        BibleBook("Lamentations", 5, true),
        BibleBook("Ezekiel", 48, true),
        BibleBook("Daniel", 12, true),
        BibleBook("Hosea", 14, true),
        BibleBook("Joel", 3, true),
        BibleBook("Amos", 9, true),
        BibleBook("Obadiah", 1, true),
        BibleBook("Jonah", 4, true),
        BibleBook("Micah", 7, true),
        BibleBook("Nahum", 3, true),
        BibleBook("Habakkuk", 3, true),
        BibleBook("Zephaniah", 3, true),
        BibleBook("Haggai", 2, true),
        BibleBook("Zechariah", 14, true),
        BibleBook("Malachi", 4, true),

        // New Testament (27 books, 260 chapters)
        BibleBook("Matthew", 28, false),
        BibleBook("Mark", 16, false),
        BibleBook("Luke", 24, false),
        BibleBook("John", 21, false),
        BibleBook("Acts", 28, false),
        BibleBook("Romans", 16, false),
        BibleBook("1 Corinthians", 16, false),
        BibleBook("2 Corinthians", 13, false),
        BibleBook("Galatians", 6, false),
        BibleBook("Ephesians", 6, false),
        BibleBook("Philippians", 4, false),
        BibleBook("Colossians", 4, false),
        BibleBook("1 Thessalonians", 5, false),
        BibleBook("2 Thessalonians", 3, false),
        BibleBook("1 Timothy", 6, false),
        BibleBook("2 Timothy", 4, false),
        BibleBook("Titus", 3, false),
        BibleBook("Philemon", 1, false),
        BibleBook("Hebrews", 13, false),
        BibleBook("James", 5, false),
        BibleBook("1 Peter", 5, false),
        BibleBook("2 Peter", 3, false),
        BibleBook("1 John", 5, false),
        BibleBook("2 John", 1, false),
        BibleBook("3 John", 1, false),
        BibleBook("Jude", 1, false),
        BibleBook("Revelation", 22, false)
    )

    const val TOTAL_BIBLE_CHAPTERS = 1189

    fun getBook(name: String): BibleBook? {
        return BOOKS.find { it.name.equals(name, ignoreCase = true) }
    }

    fun isValidChapterRange(bookName: String, startChapter: Int, endChapter: Int): Boolean {
        val book = getBook(bookName) ?: return false
        if (startChapter < 1 || endChapter < startChapter) return false
        return endChapter <= book.totalChapters
    }

    fun calculateChaptersRead(bookName: String, startChapter: Int, endChapter: Int): Int {
        if (!isValidChapterRange(bookName, startChapter, endChapter)) return 0
        return (endChapter - startChapter) + 1
    }
}
