package com.example.data.local

data class BibleBook(
    val name: String,
    val totalChapters: Int,
    val isOldTestament: Boolean = true
) {
    val chapters: Int get() = totalChapters
}

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

    data class BibleChapterRef(
        val bookIndex: Int,
        val bookName: String,
        val chapterNumber: Int
    )

    data class BookReadingSegment(
        val bookName: String,
        val startChapter: Int,
        val endChapter: Int,
        val chaptersReadCount: Int
    )

    fun getBookIndex(name: String): Int {
        val idx = BOOKS.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        return if (idx >= 0) idx else 0
    }

    /**
     * Expands a contiguous multi-book span (e.g. Genesis 45 -> Leviticus 3) into
     * individual chapters across canonical Bible order.
     */
    fun expandRange(
        startBookName: String,
        startChapter: Int,
        endBookName: String,
        endChapter: Int
    ): List<BibleChapterRef> {
        var startIdx = getBookIndex(startBookName)
        var endIdx = getBookIndex(endBookName)
        var startCh = startChapter
        var endCh = endChapter

        // Auto-swap if start is after end
        if (startIdx > endIdx || (startIdx == endIdx && startCh > endCh)) {
            val tempIdx = startIdx
            val tempCh = startCh
            startIdx = endIdx
            startCh = endCh
            endIdx = tempIdx
            endCh = tempCh
        }

        val result = mutableListOf<BibleChapterRef>()
        for (b in startIdx..endIdx) {
            val book = BOOKS[b]
            val firstCh = if (b == startIdx) startCh.coerceIn(1, book.totalChapters) else 1
            val lastCh = if (b == endIdx) endCh.coerceIn(1, book.totalChapters) else book.totalChapters

            for (c in firstCh..lastCh) {
                result.add(BibleChapterRef(b, book.name, c))
            }
        }
        return result
    }

    /**
     * Groups contiguous reading into book segments for breakdowns and summaries.
     */
    fun getSpanBreakdown(
        startBookName: String,
        startChapter: Int,
        endBookName: String,
        endChapter: Int
    ): List<BookReadingSegment> {
        var startIdx = getBookIndex(startBookName)
        var endIdx = getBookIndex(endBookName)
        var startCh = startChapter
        var endCh = endChapter

        if (startIdx > endIdx || (startIdx == endIdx && startCh > endCh)) {
            val tempIdx = startIdx
            val tempCh = startCh
            startIdx = endIdx
            startCh = endCh
            endIdx = tempIdx
            endCh = tempCh
        }

        val list = mutableListOf<BookReadingSegment>()
        for (b in startIdx..endIdx) {
            val book = BOOKS[b]
            val firstCh = if (b == startIdx) startCh.coerceIn(1, book.totalChapters) else 1
            val lastCh = if (b == endIdx) endCh.coerceIn(1, book.totalChapters) else book.totalChapters
            val count = (lastCh - firstCh + 1).coerceAtLeast(1)

            list.add(
                BookReadingSegment(
                    bookName = book.name,
                    startChapter = firstCh,
                    endChapter = lastCh,
                    chaptersReadCount = count
                )
            )
        }
        return list
    }

    /**
     * Calculates the exact total number of chapters read across a multi-book span.
     */
    fun calculateSpanChapters(
        startBookName: String,
        startChapter: Int,
        endBookName: String,
        endChapter: Int
    ): Int {
        return getSpanBreakdown(startBookName, startChapter, endBookName, endChapter).sumOf { it.chaptersReadCount }
    }

    /**
     * Formats a multi-book span into a clean, canonical summary string.
     */
    fun formatSpanSummary(
        startBookName: String,
        startChapter: Int,
        endBookName: String,
        endChapter: Int
    ): String {
        val breakdown = getSpanBreakdown(startBookName, startChapter, endBookName, endChapter)
        if (breakdown.isEmpty()) return "$startBookName $startChapter"
        if (breakdown.size == 1) {
            val seg = breakdown[0]
            return if (seg.startChapter == seg.endChapter) "${seg.bookName} ${seg.startChapter}"
            else "${seg.bookName} ${seg.startChapter}-${seg.endChapter}"
        }
        return breakdown.joinToString(", ") { "${it.bookName} ${it.startChapter}-${it.endChapter}" }
    }

    fun getBook(name: String): BibleBook? {
        return BOOKS.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getChaptersForBook(bookName: String): Int {
        return getBook(bookName)?.totalChapters ?: 50
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

    /**
     * Accurately parses the last read position from an entry's bibleBook string
     * which may contain single segment ("Genesis 1-3") or multiple segments ("Genesis 1-50, Exodus 1-5").
     * Returns Pair(BookName, LastEndChapter).
     */
    fun getLastReadPosition(entry: com.example.data.local.entities.AccountabilityEntryEntity): Pair<String, Int> {
        val raw = entry.bibleBook.trim()
        if (raw.isBlank()) {
            return "Genesis" to (if (entry.endChapter > 0) entry.endChapter else if (entry.startChapter > 0) entry.startChapter else 1)
        }

        // If comma-separated, take the last segment
        val lastSegment = raw.split(",").map { it.trim() }.lastOrNull { it.isNotBlank() } ?: raw

        // Find which book matches in this last segment
        val matchedBook = BOOKS.filter { b ->
            lastSegment.contains(b.name, ignoreCase = true)
        }.maxByOrNull { it.name.length }?.name

        if (matchedBook != null) {
            val afterBook = lastSegment.substringAfter(matchedBook, "").trim()
            // Try to extract chapter range like "1-5" or "5"
            val chapterRegex = Regex("""(\d+)(?:\s*-\s*(\d+))?""")
            val match = chapterRegex.find(afterBook)
            val endCh = if (match != null) {
                match.groupValues[2].toIntOrNull() ?: match.groupValues[1].toIntOrNull() ?: (if (entry.endChapter > 0) entry.endChapter else 1)
            } else {
                if (entry.endChapter > 0) entry.endChapter else 1
            }
            return matchedBook to endCh
        }

        // Fallback: match anywhere in raw
        val fallbackBook = BOOKS.filter { b ->
            raw.contains(b.name, ignoreCase = true)
        }.maxByOrNull { it.name.length }?.name ?: "Genesis"
        val endCh = if (entry.endChapter > 0) entry.endChapter else if (entry.startChapter > 0) entry.startChapter else 1
        return fallbackBook to endCh
    }

    /**
     * Computes the next suggested book and chapter to continue reading.
     */
    fun getNextReadPosition(entry: com.example.data.local.entities.AccountabilityEntryEntity): Pair<String, Int> {
        val (lastBook, lastEndCh) = getLastReadPosition(entry)
        val maxCh = getChaptersForBook(lastBook)
        return if (lastEndCh >= maxCh) {
            val bookIndex = BOOKS.indexOfFirst { it.name.equals(lastBook, ignoreCase = true) }
            if (bookIndex in 0 until BOOKS.lastIndex) {
                BOOKS[bookIndex + 1].name to 1
            } else {
                "Genesis" to 1
            }
        } else {
            lastBook to (lastEndCh + 1).coerceAtMost(maxCh)
        }
    }
}
