package com.willykez.liturgx.data.bible

/**
 * Maps the many ways a Bible book shows up in a citation (English full name,
 * common abbreviation, or the Swahili name already used inside the bundled
 * database) to the fixed `chapters._id` used by bible_swahili.sqlite.
 *
 * The database's book ids run 8022 (Mwanzo/Genesis) through 8087 (Ufunuo wa
 * Yohana/Revelation) — 66 books, i.e. no Deuterocanonical/Apocryphal books.
 * That means citations to Wisdom (Hekima), Sirach (Yoshua bin Sira), Tobit
 * (Tobiti), Judith, Baruch (Baruku), or 1–2 Maccabees (Wamakabayo) — all of
 * which appear regularly as First Readings in the Catholic lectionary —
 * cannot be resolved against this database — [resolve] returns null for
 * those and the caller should fall back to showing the citation without
 * inline text.
 */
object BibleBooks {

    /** English canonical name -> chapters._id, in database order. */
    private val ID_BY_CANONICAL: Map<String, Int> = listOf(
        "genesis", "exodus", "leviticus", "numbers", "deuteronomy",
        "joshua", "judges", "ruth", "1 samuel", "2 samuel",
        "1 kings", "2 kings", "1 chronicles", "2 chronicles", "ezra",
        "nehemiah", "esther", "job", "psalms", "proverbs",
        "ecclesiastes", "song of songs", "isaiah", "jeremiah", "lamentations",
        "ezekiel", "daniel", "hosea", "joel", "amos",
        "obadiah", "jonah", "micah", "nahum", "habakkuk",
        "zephaniah", "haggai", "zechariah", "malachi", "matthew",
        "mark", "luke", "john", "acts", "romans",
        "1 corinthians", "2 corinthians", "galatians", "ephesians", "philippians",
        "colossians", "1 thessalonians", "2 thessalonians", "1 timothy", "2 timothy",
        "titus", "philemon", "hebrews", "james", "1 peter",
        "2 peter", "1 john", "2 john", "3 john", "jude",
        "revelation"
    ).mapIndexed { index, name -> name to (8022 + index) }.toMap()

    /** Aliases (lowercased, punctuation-free) that resolve to the same canonical name. */
    private val ALIASES: Map<String, String> = buildMap {
        fun alias(vararg forms: String, canonical: String) {
            forms.forEach { put(it.lowercase(), canonical) }
        }
        alias("gn", "gen", "genesis", canonical = "genesis")
        alias("ex", "exo", "exodus", canonical = "exodus")
        alias("lv", "lev", "leviticus", canonical = "leviticus")
        alias("nm", "num", "numbers", canonical = "numbers")
        alias("dt", "deut", "deuteronomy", canonical = "deuteronomy")
        alias("jos", "josh", "joshua", canonical = "joshua")
        alias("jgs", "judg", "judges", canonical = "judges")
        alias("ru", "ruth", canonical = "ruth")
        alias("1 sm", "1sam", "1 samuel", "i samuel", canonical = "1 samuel")
        alias("2 sm", "2sam", "2 samuel", "ii samuel", canonical = "2 samuel")
        alias("1 kgs", "1kings", "1 kings", "i kings", canonical = "1 kings")
        alias("2 kgs", "2kings", "2 kings", "ii kings", canonical = "2 kings")
        alias("1 chr", "1chronicles", "1 chronicles", canonical = "1 chronicles")
        alias("2 chr", "2chronicles", "2 chronicles", canonical = "2 chronicles")
        alias("ezr", "ezra", canonical = "ezra")
        alias("neh", "nehemiah", canonical = "nehemiah")
        alias("est", "esther", canonical = "esther")
        alias("jb", "job", canonical = "job")
        alias("ps", "psa", "psalm", "psalms", canonical = "psalms")
        alias("prv", "prov", "proverbs", canonical = "proverbs")
        alias("eccl", "eccles", "ecclesiastes", "qoheleth", canonical = "ecclesiastes")
        alias("sg", "song", "song of songs", "song of solomon", "canticle of canticles", canonical = "song of songs")
        alias("is", "isa", "isaiah", canonical = "isaiah")
        alias("jer", "jeremiah", canonical = "jeremiah")
        alias("lam", "lamentations", canonical = "lamentations")
        alias("ez", "ezek", "ezekiel", canonical = "ezekiel")
        alias("dn", "dan", "daniel", canonical = "daniel")
        alias("hos", "hosea", canonical = "hosea")
        alias("jl", "joel", canonical = "joel")
        alias("am", "amos", canonical = "amos")
        alias("ob", "obad", "obadiah", canonical = "obadiah")
        alias("jon", "jonah", canonical = "jonah")
        alias("mi", "mic", "micah", canonical = "micah")
        alias("na", "nah", "nahum", canonical = "nahum")
        alias("hb", "hab", "habakkuk", canonical = "habakkuk")
        alias("zep", "zeph", "zephaniah", canonical = "zephaniah")
        alias("hg", "hag", "haggai", canonical = "haggai")
        alias("zec", "zech", "zechariah", canonical = "zechariah")
        alias("mal", "malachi", canonical = "malachi")
        alias("mt", "matt", "matthew", canonical = "matthew")
        alias("mk", "mark", canonical = "mark")
        alias("lk", "luke", canonical = "luke")
        alias("jn", "john", canonical = "john")
        alias("acts", "ac", "acts of the apostles", canonical = "acts")
        alias("rom", "romans", canonical = "romans")
        alias("1 cor", "1corinthians", "1 corinthians", canonical = "1 corinthians")
        alias("2 cor", "2corinthians", "2 corinthians", canonical = "2 corinthians")
        alias("gal", "galatians", canonical = "galatians")
        alias("eph", "ephesians", canonical = "ephesians")
        alias("phil", "philippians", canonical = "philippians")
        alias("col", "colossians", canonical = "colossians")
        alias("1 thess", "1thessalonians", "1 thessalonians", canonical = "1 thessalonians")
        alias("2 thess", "2thessalonians", "2 thessalonians", canonical = "2 thessalonians")
        alias("1 tim", "1timothy", "1 timothy", canonical = "1 timothy")
        alias("2 tim", "2timothy", "2 timothy", canonical = "2 timothy")
        alias("ti", "tit", "titus", canonical = "titus")
        alias("phlm", "philem", "philemon", canonical = "philemon")
        alias("heb", "hebrews", canonical = "hebrews")
        alias("jas", "james", canonical = "james")
        alias("1 pt", "1peter", "1 peter", canonical = "1 peter")
        alias("2 pt", "2peter", "2 peter", canonical = "2 peter")
        alias("1 jn", "1john", "1 john", canonical = "1 john")
        alias("2 jn", "2john", "2 john", canonical = "2 john")
        alias("3 jn", "3john", "3 john", canonical = "3 john")
        alias("jude", canonical = "jude")
        alias("rv", "rev", "revelation", "apocalypse", canonical = "revelation")

        // Swahili names (Swahili Union Version), as used by the bundled lectionary citations.
        alias("mwanzo", canonical = "genesis")
        alias("kutoka", canonical = "exodus")
        alias("mambo ya walawi", "walawi", canonical = "leviticus")
        alias("hesabu", canonical = "numbers")
        alias("kumbukumbu la torati", "kumbukumbu", canonical = "deuteronomy")
        alias("yoshua", canonical = "joshua")
        alias("waamuzi", canonical = "judges")
        alias("ruthu", canonical = "ruth")
        alias("1 samweli", canonical = "1 samuel")
        alias("2 samweli", canonical = "2 samuel")
        alias("1 wafalme", canonical = "1 kings")
        alias("2 wafalme", canonical = "2 kings")
        alias("1 mambo ya nyakati", canonical = "1 chronicles")
        alias("2 mambo ya nyakati", canonical = "2 chronicles")
        alias("nehemia", canonical = "nehemiah")
        alias("esta", canonical = "esther")
        alias("ayubu", canonical = "job")
        alias("zaburi", canonical = "psalms")
        alias("mithali", canonical = "proverbs")
        alias("mhubiri", canonical = "ecclesiastes")
        alias("wimbo ulio bora", "wimbo wa sulemani", canonical = "song of songs")
        alias("isaya", canonical = "isaiah")
        alias("yeremia", canonical = "jeremiah")
        alias("maombolezo", canonical = "lamentations")
        alias("ezekieli", canonical = "ezekiel")
        alias("danieli", canonical = "daniel")
        alias("hosea", canonical = "hosea")
        alias("yoeli", canonical = "joel")
        alias("amosi", canonical = "amos")
        alias("obadia", canonical = "obadiah")
        alias("yona", "jon", canonical = "jonah")
        alias("mika", canonical = "micah")
        alias("nahumu", canonical = "nahum")
        alias("habakuki", canonical = "habakkuk")
        alias("sefania", canonical = "zephaniah")
        alias("hagai", canonical = "haggai")
        alias("zekaria", canonical = "zechariah")
        alias("malaki", canonical = "malachi")
        alias("mathayo", canonical = "matthew")
        alias("marko", canonical = "mark")
        alias("luka", canonical = "luke")
        alias("yohana", canonical = "john")
        alias("matendo ya mitume", "matendo", canonical = "acts")
        alias("warumi", canonical = "romans")
        alias("1 wakorintho", canonical = "1 corinthians")
        alias("2 wakorintho", canonical = "2 corinthians")
        alias("wagalatia", canonical = "galatians")
        alias("waefeso", canonical = "ephesians")
        alias("wafilipi", canonical = "philippians")
        alias("wakolosai", canonical = "colossians")
        alias("1 wathesalonike", canonical = "1 thessalonians")
        alias("2 wathesalonike", canonical = "2 thessalonians")
        alias("1 timotheo", canonical = "1 timothy")
        alias("2 timotheo", canonical = "2 timothy")
        alias("tito", canonical = "titus")
        alias("filemoni", canonical = "philemon")
        alias("waebrania", canonical = "hebrews")
        alias("yakobo", canonical = "james")
        alias("1 petro", canonical = "1 peter")
        alias("2 petro", canonical = "2 peter")
        alias("1 yohana", canonical = "1 john")
        alias("2 yohana", canonical = "2 john")
        alias("3 yohana", canonical = "3 john")
        alias("yuda", canonical = "jude")
        alias("ufunuo", "ufunuo wa yohana", canonical = "revelation")
        // Every canonical name also resolves to itself.
        ID_BY_CANONICAL.keys.forEach { put(it, it) }
    }

    /** Normalizes a raw book token from a citation ("Mt", "Matt.", "1 Cor") for lookup. */
    private fun normalize(raw: String): String =
        raw.trim().lowercase().trimEnd('.').replace(Regex("\\s+"), " ")

    /** Resolves a book name/abbreviation to its database id, or null if unresolvable (e.g. Deuterocanon). */
    fun resolveId(bookName: String): Int? {
        val key = normalize(bookName)
        val canonical = ALIASES[key] ?: return null
        return ID_BY_CANONICAL[canonical]
    }

    /** True for books that exist in this Bible edition (Protestant/Swahili Union canon — 66 books). */
    fun isSupported(bookName: String): Boolean = resolveId(bookName) != null
}
