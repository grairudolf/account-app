package com.example.domain.models

enum class DomainType {
    DDEWG,
    BIBLE_READING,
    PRAYER_ALONE,
    PRAYER_WITH_OTHERS,
    FASTING,
    GIVING,
    ACCOUNTABILITY,
    CHRISTIAN_LITERATURE,
    CHRISTIAN_LIT_MEM,
    BIBLE_MEM,
    SOUL_WINNING,
    PROCLAMATION_IMPORTUNITY,
    RETREATS,
    MAKING_DISCIPLES,
    CUSTOM
}

data class AccountabilityDomainModel(
    val id: String,
    val type: DomainType,
    val titleKey: String,
    val descKey: String,
    val iconName: String,
    val isCustom: Boolean = false,
    val measurementUnit: String = ""
)

object PredefinedDomains {
    val ALL = listOf(
        AccountabilityDomainModel("ddewg", DomainType.DDEWG, "ddewgTitle", "ddewgDesc", "wb_sunny"),
        AccountabilityDomainModel("bible_reading", DomainType.BIBLE_READING, "bibleReadingTitle", "bibleReadingDesc", "menu_book"),
        AccountabilityDomainModel("prayer_alone", DomainType.PRAYER_ALONE, "prayerAloneTitle", "prayerAloneDesc", "self_improvement"),
        AccountabilityDomainModel("prayer_with_others", DomainType.PRAYER_WITH_OTHERS, "prayerWithOthersTitle", "prayerWithOthersDesc", "groups"),
        AccountabilityDomainModel("proclamation_importunity", DomainType.PROCLAMATION_IMPORTUNITY, "proclamationTitle", "proclamationDesc", "campaign"),
        AccountabilityDomainModel("fasting", DomainType.FASTING, "fastingTitle", "fastingDesc", "no_food"),
        AccountabilityDomainModel("giving", DomainType.GIVING, "givingTitle", "givingDesc", "volunteer_activism"),
        AccountabilityDomainModel("making_disciples", DomainType.MAKING_DISCIPLES, "makingDisciplesTitle", "makingDisciplesDesc", "group_add"),
        AccountabilityDomainModel("soul_winning", DomainType.SOUL_WINNING, "soulWinningTitle", "soulWinningDesc", "directions_walk"),
        AccountabilityDomainModel("christian_lit", DomainType.CHRISTIAN_LITERATURE, "christianLitTitle", "christianLitDesc", "library_books"),
        AccountabilityDomainModel("christian_lit_mem", DomainType.CHRISTIAN_LIT_MEM, "christianLitMemTitle", "christianLitMemDesc", "psychology"),
        AccountabilityDomainModel("bible_mem", DomainType.BIBLE_MEM, "bibleMemTitle", "bibleMemDesc", "bookmark_added"),
        AccountabilityDomainModel("retreats", DomainType.RETREATS, "retreatsTitle", "retreatsDesc", "landscape")
    )
}
