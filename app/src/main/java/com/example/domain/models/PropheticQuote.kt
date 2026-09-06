package com.example.domain.models

import androidx.annotation.DrawableRes
import com.example.R

data class PropheticQuote(
    val id: String,
    val textEn: String,
    val textFr: String,
    val prophecySourceEn: String,
    val prophecySourceFr: String,
    val themeTagEn: String,
    val themeTagFr: String,
    val bookCitationEn: String,
    val bookCitationFr: String,
    val authorEn: String = "Prof. Zacharias Tanee Fomum",
    val authorFr: String = "Pr. Zacharias Tanee Fomum",
    @DrawableRes val bgDrawableRes: Int
) {
    fun getText(isFrench: Boolean): String = if (isFrench) textFr else textEn
    fun getProphecySource(isFrench: Boolean): String = if (isFrench) prophecySourceFr else prophecySourceEn
    fun getThemeTag(isFrench: Boolean): String = if (isFrench) themeTagFr else themeTagEn
    fun getBookCitation(isFrench: Boolean): String = if (isFrench) bookCitationFr else bookCitationEn
    fun getAuthor(isFrench: Boolean): String = if (isFrench) authorFr else authorEn
}

object PropheticQuotesRepository {
    // 40 Curated Spiritual Quotes by Prof. Zacharias Tanee Fomum
    // Arranged in a balanced spiritual cycle without repeats
    val ALL: List<PropheticQuote> = listOf(
        PropheticQuote(
            id = "ztf_q1_prayer_communion",
            textEn = "The primary essence of prayer is communion with God.",
            textFr = "L'essence première de la prière est la communion avec Dieu.",
            prophecySourceEn = "Prayer & Communion",
            prophecySourceFr = "Prière et Communion",
            themeTagEn = "COMMUNION WITH GOD",
            themeTagFr = "COMMUNION AVEC DIEU",
            bookCitationEn = "The Way of Victorious Praying",
            bookCitationFr = "Le Chemin de la Prière Victorieuse",
            bgDrawableRes = R.drawable.quote_bg_prayer_altar_1788139251276
        ),
        PropheticQuote(
            id = "ztf_q11_hunger_for_god",
            textEn = "There is only one reason why people don’t spend time with God. It’s not the shortage of time. It is a shortage of the hunger for God.",
            textFr = "Il n'y a qu'une seule raison pour laquelle les gens ne passent pas de temps avec Dieu : ce n'est pas le manque de temps, c'est le manque de faim de Dieu.",
            prophecySourceEn = "The Secret Place",
            prophecySourceFr = "Le Lieu Secret",
            themeTagEn = "HUNGER FOR GOD",
            themeTagFr = "LA FAIM DE DIEU",
            bookCitationEn = "The Complete Works on Leadership, Volume 4",
            bookCitationFr = "Traité sur le Leadership, Volume 4",
            bgDrawableRes = R.drawable.quote_bg_waters_1787220685176
        ),
        PropheticQuote(
            id = "ztf_q17_parting_with_sin",
            textEn = "We cannot really come after Him unless we definitely part with all the sin that is in our lives.",
            textFr = "Nous ne pouvons véritablement Le suivre que si nous nous séparons définitivement de tout le péché dans notre vie.",
            prophecySourceEn = "The Narrow Path",
            prophecySourceFr = "Le Chemin Étroit",
            themeTagEn = "PARTING WITH ALL SIN",
            themeTagFr = "SE SÉPARER DU PÉCHÉ",
            bookCitationEn = "Discipleship at Any Cost",
            bookCitationFr = "Le Disciple à Tout Prix",
            bgDrawableRes = R.drawable.quote_bg_cross_1787235555876
        ),
        PropheticQuote(
            id = "ztf_q23_leading_to_christ",
            textEn = "Leading someone to Christ is the art of turning someone who is following himself into someone who follows the Lord Jesus.",
            textFr = "Amener quelqu'un à Christ est l'art de transformer quelqu'un qui se suit lui-même en quelqu'un qui suit le Seigneur Jésus.",
            prophecySourceEn = "Soul Winning",
            prophecySourceFr = "Gagnagisme d'Âmes",
            themeTagEn = "LEADING TO CHRIST",
            themeTagFr = "AMENER À CHRIST",
            bookCitationEn = "The School of Soul Winners and Soul Winning",
            bookCitationFr = "L'École des Gagneurs d'Âmes et le Gagnagisme d'Âmes",
            bgDrawableRes = R.drawable.img_quote_break_dawn_1788398693805
        ),
        PropheticQuote(
            id = "ztf_q27_fasting_activity",
            textEn = "Fasting is the activity of a man possessed by something that God must do.",
            textFr = "Le jeûne est l'activité d'un homme possédé par quelque chose que Dieu doit accomplir.",
            prophecySourceEn = "Fasting Ministry",
            prophecySourceFr = "Ministère du Jeûne",
            themeTagEn = "ACTIVITY OF FASTING",
            themeTagFr = "L'ACTIVITÉ DU JEÛNE",
            bookCitationEn = "From His Lips on Fasting",
            bookCitationFr = "De Ses Lèvres sur le Jeûne",
            bgDrawableRes = R.drawable.quote_bg_prayer_altar_1788139251276
        ),
        PropheticQuote(
            id = "ztf_q36_leader_in_prayer",
            textEn = "The leader is the person who spends the most time in prayer.",
            textFr = "Le leader est la personne qui passe le plus de temps dans la prière.",
            prophecySourceEn = "Spiritual Leadership",
            prophecySourceFr = "Leadership Spirituel",
            themeTagEn = "LEADERSHIP IN PRAYER",
            themeTagFr = "LEADERSHIP DANS LA PRIÈRE",
            bookCitationEn = "The Way of Victorious Praying",
            bookCitationFr = "Le Chemin de la Prière Victorieuse",
            bgDrawableRes = R.drawable.quote_bg_mountains_1787235541853
        ),
        PropheticQuote(
            id = "ztf_q13_daily_bible_prayer",
            textEn = "Daily Bible study and prayer (the Quiet Time) is indispensable in maintaining the sanctified life.",
            textFr = "L'étude quotidienne de la Bible et la prière (le Culte Personnel) sont indispensables pour maintenir une vie sanctifiée.",
            prophecySourceEn = "Sanctified Life",
            prophecySourceFr = "Vie Sanctifiée",
            themeTagEn = "THE QUIET TIME",
            themeTagFr = "LE CULTE PERSONNEL",
            bookCitationEn = "The Way of Sanctification",
            bookCitationFr = "Le Chemin de la Sanctification",
            bgDrawableRes = R.drawable.img_quote_bible_light_1788398727365
        ),
        PropheticQuote(
            id = "ztf_q30_praise_gratitude",
            textEn = "The Ministry of Praise and Thanksgiving is the flowing forth of gratitude through the lips to the Lord God.",
            textFr = "Le Ministère de Louange et d'Action de Grâces est l'effusion de gratitude à travers les lèvres envers le Seigneur Dieu.",
            prophecySourceEn = "Praise & Thanksgiving",
            prophecySourceFr = "Louange et Action de Grâces",
            themeTagEn = "FLOWING WITH GRATITUDE",
            themeTagFr = "EFFUSION DE GRATITUDE",
            bookCitationEn = "The Ministry of Praise and Thanksgiving",
            bookCitationFr = "Le Ministère de Louange et d'Action de Grâces",
            bgDrawableRes = R.drawable.img_quote_living_water_1788398711735
        ),
        PropheticQuote(
            id = "ztf_q8_fifteen_minutes",
            textEn = "Fifteen minutes is a lot of time! A lot can happen in fifteen minutes.",
            textFr = "Quinze minutes, c'est beaucoup de temps ! Beaucoup de choses peuvent se passer en quinze minutes.",
            prophecySourceEn = "Short Retreats",
            prophecySourceFr = "Courtes Retraites",
            themeTagEn = "15-MINUTE RETREATS",
            themeTagFr = "RETRAITES DE 15 MINUTES",
            bookCitationEn = "Fifteen Minute Prayer Retreats",
            bookCitationFr = "Les Retraites de Prière de Quinze Minutes",
            bgDrawableRes = R.drawable.img_quote_golden_path_1788398765839
        ),
        PropheticQuote(
            id = "ztf_q14_holiness_conformity",
            textEn = "Holiness is not just the absence of sin. It includes conformity to God.",
            textFr = "La sainteté n'est pas seulement l'absence de péché. Elle inclut la conformité à Dieu.",
            prophecySourceEn = "True Holiness",
            prophecySourceFr = "La Vraie Sainteté",
            themeTagEn = "CONFORMITY TO GOD",
            themeTagFr = "CONFORMITÉ À DIEU",
            bookCitationEn = "The Way of Sanctification",
            bookCitationFr = "Le Chemin de la Sanctification",
            bgDrawableRes = R.drawable.quote_bg_radiant_cross_1788139262304
        ),
        PropheticQuote(
            id = "ztf_q2_growing_spiritual_power",
            textEn = "Those who spend time increasingly in prayer grow in spiritual power.",
            textFr = "Ceux qui passent de plus en plus de temps dans la prière croissent en puissance spirituelle.",
            prophecySourceEn = "Spiritual Power",
            prophecySourceFr = "Puissance Spirituelle",
            themeTagEn = "GROWTH IN POWER",
            themeTagFr = "CROISSANCE EN PUISSANCE",
            bookCitationEn = "The Way of Victorious Praying",
            bookCitationFr = "Le Chemin de la Prière Victorieuse",
            bgDrawableRes = R.drawable.img_quote_prayer_mountain_1788398743570
        ),
        PropheticQuote(
            id = "ztf_q18_jesus_our_all",
            textEn = "The disciple makes Jesus his all and forsakes everything that may stand in the way of his fellowship with Christ.",
            textFr = "Le disciple fait de Jésus son tout et renonce à tout ce qui pourrait faire obstacle à sa communion avec Christ.",
            prophecySourceEn = "Fellowship with Christ",
            prophecySourceFr = "Communion avec Christ",
            themeTagEn = "JESUS AS OUR ALL",
            themeTagFr = "JÉSUS NOTRE TOUT",
            bookCitationEn = "Discipleship at Any Cost",
            bookCitationFr = "Le Disciple à Tout Prix",
            bgDrawableRes = R.drawable.img_quote_golden_path_1788398765839
        ),
        PropheticQuote(
            id = "ztf_q24_reason_staying_on_earth",
            textEn = "The one and only reason why the Lord Jesus did not take me to heaven the day I believed was that I should stay here on earth, win the lost to Him and make disciples of all nations.",
            textFr = "La seule et unique raison pour laquelle le Seigneur Jésus ne m'a pas enlevé au ciel le jour où j'ai cru est que je devais rester sur terre pour Lui gagner les perdus et faire des disciples de toutes les nations.",
            prophecySourceEn = "Life Purpose",
            prophecySourceFr = "But de la Vie",
            themeTagEn = "WINNING THE LOST",
            themeTagFr = "GAGNER LES PERDUS",
            bookCitationEn = "Soul-Winning, Volume One",
            bookCitationFr = "Le Gagnagisme d'Âmes, Volume 1",
            bgDrawableRes = R.drawable.quote_bg_global_harvest_1788139235063
        ),
        PropheticQuote(
            id = "ztf_q32_church_gives",
            textEn = "The church is a company of people who give and give and give.",
            textFr = "L'Église est une compagnie de personnes qui donnent, donnent et donnent encore.",
            prophecySourceEn = "Sacrificial Love",
            prophecySourceFr = "Amour Sacrificiel",
            themeTagEn = "SACRIFICIAL GIVING",
            themeTagFr = "LE DON SACRIFICIEL",
            bookCitationEn = "Making Spiritual Progress, Volume One",
            bookCitationFr = "Faire du Progrès Spirituel, Volume 1",
            bgDrawableRes = R.drawable.quote_bg_waters_1787220685176
        ),
        PropheticQuote(
            id = "ztf_q5_prayer_walk_work",
            textEn = "Prayer centred on God will lead to a walk centred on God, and this will lead to a work centred on God.",
            textFr = "Une prière centrée sur Dieu conduira à une marche centrée sur Dieu, et cela conduira à une œuvre centrée sur Dieu.",
            prophecySourceEn = "God-Centred Life",
            prophecySourceFr = "Vie Centrée sur Dieu",
            themeTagEn = "GOD-CENTRED PRAYER",
            themeTagFr = "PRIÈRE CENTRÉE SUR DIEU",
            bookCitationEn = "The Centrality of Prayer",
            bookCitationFr = "La Centralité de la Prière",
            bgDrawableRes = R.drawable.quote_bg_path_1787220696837
        ),
        PropheticQuote(
            id = "ztf_q15_holy_of_holies",
            textEn = "We are permanently in the holy of holies. Be careful about what you are thinking, saying or doing.",
            textFr = "Nous sommes en permanence dans le saint des saints. Fais attention à ce que tu penses, dis ou fais.",
            prophecySourceEn = "Sacred Presence",
            prophecySourceFr = "Présence Sacrée",
            themeTagEn = "IN GOD'S PRESENCE",
            themeTagFr = "DANS LA PRÉSENCE DE DIEU",
            bookCitationEn = "Knowing and Serving God, Volume Two",
            bookCitationFr = "Connaître et Servir Dieu, Volume 2",
            bgDrawableRes = R.drawable.quote_bg_heavens_1787220708792
        ),
        PropheticQuote(
            id = "ztf_q28_fasting_spiritual_ministry",
            textEn = "Fasting is a spiritual ministry.",
            textFr = "Le jeûne est un ministère spirituel.",
            prophecySourceEn = "Spiritual Warfare",
            prophecySourceFr = "Combat Spirituel",
            themeTagEn = "SPIRITUAL FASTING",
            themeTagFr = "LE JEÛNE SPIRITUEL",
            bookCitationEn = "From His Lips on Fasting",
            bookCitationFr = "De Ses Lèvres sur le Jeûne",
            bgDrawableRes = R.drawable.img_quote_break_dawn_1788398693805
        ),
        PropheticQuote(
            id = "ztf_q37_leader_basic_things",
            textEn = "If you want to be a leader someday, those are the basic things of the Christian life. Excel in them.",
            textFr = "Si tu veux être un leader un jour, voici les éléments fondamentaux de la vie chrétienne. Excelle en eux.",
            prophecySourceEn = "Foundations",
            prophecySourceFr = "Fondements",
            themeTagEn = "EXCEL IN THE BASICS",
            themeTagFr = "EXCELLER DANS LES BASES",
            bookCitationEn = "The Complete Works on Leadership, Volume 4",
            bookCitationFr = "Traité sur le Leadership, Volume 4",
            bgDrawableRes = R.drawable.img_quote_bible_light_1788398727365
        ),
        PropheticQuote(
            id = "ztf_q3_overflow_of_life",
            textEn = "Prayer is the overflow of a life; prayer is the overflow of communion with God.",
            textFr = "La prière est le débordement d'une vie ; la prière est le débordement de la communion avec Dieu.",
            prophecySourceEn = "Overflowing Life",
            prophecySourceFr = "Vie Débordante",
            themeTagEn = "OVERFLOW OF LIFE",
            themeTagFr = "DÉBORDEMENT D'UNE VIE",
            bookCitationEn = "Prayer and a Walk with God",
            bookCitationFr = "La Prière et la Marche avec Dieu",
            bgDrawableRes = R.drawable.img_quote_living_water_1788398711735
        ),
        PropheticQuote(
            id = "ztf_q21_rugged_way",
            textEn = "Commit yourself to becoming a real disciple. Follow that rugged way. Follow its austere demands.",
            textFr = "Engage-toi à devenir un vrai disciple. Suis cette voie rude. Suis ses exigences austères.",
            prophecySourceEn = "The Cross",
            prophecySourceFr = "La Croix",
            themeTagEn = "THE RUGGED WAY",
            themeTagFr = "LA VOIE RUDE",
            bookCitationEn = "The Making of Disciples",
            bookCitationFr = "La Formation des Disciples",
            bgDrawableRes = R.drawable.quote_bg_mountains_1787235541853
        ),
        PropheticQuote(
            id = "ztf_q25_holy_spirit_possess",
            textEn = "The Holy Spirit must possess me entirely if I am to be entirely pleasing to God in all things, at all times and for all time.",
            textFr = "Le Saint-Esprit doit me posséder entièrement si je veux être entièrement agréable à Dieu en toutes choses, en tout temps et pour toujours.",
            prophecySourceEn = "Holy Spirit Fullness",
            prophecySourceFr = "Plénitude du Saint-Esprit",
            themeTagEn = "FILLED WITH THE SPIRIT",
            themeTagFr = "REMPLI DU SAINT-ESPRIT",
            bookCitationEn = "Soul-Winning, Volume One",
            bookCitationFr = "Le Gagnagisme d'Âmes, Volume 1",
            bgDrawableRes = R.drawable.quote_bg_heavens_1787220708792
        ),
        PropheticQuote(
            id = "ztf_q9_four_fifteen_minute_retreats",
            textEn = "By having four fifteen-minute retreats a day, one has actually spent one hour before God.",
            textFr = "En ayant quatre retraites de quinze minutes par jour, on a en réalité passé une heure devant Dieu.",
            prophecySourceEn = "Daily Secret Place",
            prophecySourceFr = "Lieu Secret Quotidien",
            themeTagEn = "ONE HOUR BEFORE GOD",
            themeTagFr = "UNE HEURE DEVANT DIEU",
            bookCitationEn = "Fifteen Minute Prayer Retreats",
            bookCitationFr = "Les Retraites de Prière de Quinze Minutes",
            bgDrawableRes = R.drawable.quote_bg_mountains_1787235541853
        ),
        PropheticQuote(
            id = "ztf_q34_investing_money_disciples",
            textEn = "There is only one use for money: to be invested into the making of disciples.",
            textFr = "Il n'y a qu'un seul usage de l'argent : être investi dans la formation des disciples.",
            prophecySourceEn = "Kingdom Economy",
            prophecySourceFr = "Économie du Royaume",
            themeTagEn = "KINGDOM STEWARDSHIP",
            themeTagFr = "GESTION DU ROYAUME",
            bookCitationEn = "The Facets of His Ministry",
            bookCitationFr = "Les Facettes de Son Ministère",
            bgDrawableRes = R.drawable.quote_bg_global_harvest_1788139235063
        ),
        PropheticQuote(
            id = "ztf_q6_communion_or_falsehood",
            textEn = "Without communion with God, everything done for the Lord is falsehood.",
            textFr = "Sans communion avec Dieu, tout ce qui est fait pour le Seigneur n'est que fausseté.",
            prophecySourceEn = "Heart Motives",
            prophecySourceFr = "Motifs du Cœur",
            themeTagEn = "THE ESSENCE OF SERVICE",
            themeTagFr = "L'ESSENCE DU SERVICE",
            bookCitationEn = "The Centrality of Prayer",
            bookCitationFr = "La Centralité de la Prière",
            bgDrawableRes = R.drawable.quote_bg_sunrise_1787220672419
        ),
        PropheticQuote(
            id = "ztf_q19_all_in_all",
            textEn = "Such a one has Christ for his all in all and desires nothing outside of Him.",
            textFr = "Une telle personne a Christ pour son tout en tout et ne désire rien en dehors de Lui.",
            prophecySourceEn = "Pure Devotion",
            prophecySourceFr = "Pure Dévotion",
            themeTagEn = "ALL IN ALL",
            themeTagFr = "TOUT EN TOUT",
            bookCitationEn = "Discipleship at Any Cost",
            bookCitationFr = "Le Disciple à Tout Prix",
            bgDrawableRes = R.drawable.quote_bg_sunrise_1787220672419
        ),
        PropheticQuote(
            id = "ztf_q29_fasting_harvest",
            textEn = "Fasting time is harvest time.",
            textFr = "Le temps du jeûne est le temps de la moisson.",
            prophecySourceEn = "Spiritual Seasons",
            prophecySourceFr = "Saisons Spirituelles",
            themeTagEn = "HARVEST TIME",
            themeTagFr = "LE TEMPS DE MOISSON",
            bookCitationEn = "The Facets of His Ministry",
            bookCitationFr = "Les Facettes de Son Ministère",
            bgDrawableRes = R.drawable.quote_bg_global_harvest_1788139235063
        ),
        PropheticQuote(
            id = "ztf_q38_young_believer_leader",
            textEn = "If you are a young believer, set your mind to be a leader someday and put in everything to grow rapidly and become a leader.",
            textFr = "Si tu es un jeune croyant, prends la résolution d'être un leader un jour et investis tout pour croître rapidement et le devenir.",
            prophecySourceEn = "Spiritual Vision",
            prophecySourceFr = "Vision Spirituelle",
            themeTagEn = "GROW RAPIDLY",
            themeTagFr = "CROÎTRE RAPIDEMENT",
            bookCitationEn = "Laws of Spiritual Leadership",
            bookCitationFr = "Les Lois du Leadership Spirituel",
            bgDrawableRes = R.drawable.img_quote_break_dawn_1788398693805
        ),
        PropheticQuote(
            id = "ztf_q12_transformed_by_seeking",
            textEn = "The time he spent seeking God slowly transformed him into a man who hungered and thirsted for God.",
            textFr = "Le temps qu'il passait à chercher Dieu le transformait lentement en un homme qui avait faim et soif de Dieu.",
            prophecySourceEn = "Seeking God",
            prophecySourceFr = "Chercher Dieu",
            themeTagEn = "TRANSFORMED BY GOD",
            themeTagFr = "TRANSFORMÉ PAR DIEU",
            bookCitationEn = "Daily Dynamic Encounters With God",
            bookCitationFr = "Rendez-vous Quotidiens avec Dieu",
            bgDrawableRes = R.drawable.img_quote_living_water_1788398711735
        ),
        PropheticQuote(
            id = "ztf_q16_holiness_upon_saints",
            textEn = "A sense of the holiness of God must come upon the saints.",
            textFr = "Un sentiment de la sainteté de Dieu doit saisir les saints.",
            prophecySourceEn = "Holiness",
            prophecySourceFr = "Sainteté",
            themeTagEn = "THE HOLINESS OF GOD",
            themeTagFr = "LA SAINTETÉ DE DIEU",
            bookCitationEn = "Knowing and Serving God, Volume Two",
            bookCitationFr = "Connaître et Servir Dieu, Volume 2",
            bgDrawableRes = R.drawable.quote_bg_radiant_cross_1788139262304
        ),
        PropheticQuote(
            id = "ztf_q22_making_disciples_command",
            textEn = "Commit yourself to making disciples as the Lord commanded.",
            textFr = "Engage-toi à faire des disciples comme le Seigneur l'a ordonné.",
            prophecySourceEn = "The Mandate",
            prophecySourceFr = "Le Mandat",
            themeTagEn = "MAKING DISCIPLES",
            themeTagFr = "FAIRE DES DISCIPLES",
            bookCitationEn = "The Making of Disciples",
            bookCitationFr = "La Formation des Disciples",
            bgDrawableRes = R.drawable.quote_bg_global_harvest_1788139235063
        ),
        PropheticQuote(
            id = "ztf_q31_praise_past_present_future",
            textEn = "Praise and Thanksgiving involve the flowing forth in gratitude through the lips to God for His Person in the past, in the present and in the future.",
            textFr = "La louange et l'action de grâces impliquent de déborder de gratitude par les lèvres envers Dieu pour Sa Personne dans le passé, le présent et l'avenir.",
            prophecySourceEn = "Continual Thanksgiving",
            prophecySourceFr = "Action de Grâces Continuelle",
            themeTagEn = "GRATITUDE TO GOD",
            themeTagFr = "GRATITUDE ENVERS DIEU",
            bookCitationEn = "The Ministry of Praise and Thanksgiving",
            bookCitationFr = "Le Ministère de Louange et d'Action de Grâces",
            bgDrawableRes = R.drawable.quote_bg_sunrise_1787220672419
        ),
        PropheticQuote(
            id = "ztf_q7_prayed_through_work",
            textEn = "All work that is not prayed through adequately, several times over, is an activity of the flesh. It will not last.",
            textFr = "Tout travail qui n'a pas été prié de manière adéquate, et ce à plusieurs reprises, est une activité de la chair. Il ne durera pas.",
            prophecySourceEn = "Prayer Behind Work",
            prophecySourceFr = "La Prière Derrière le Travail",
            themeTagEn = "PRAYING THROUGH WORK",
            themeTagFr = "PRIER POUR L'ŒUVRE",
            bookCitationEn = "The Way of Victorious Praying",
            bookCitationFr = "Le Chemin de la Prière Victorieuse",
            bgDrawableRes = R.drawable.img_quote_break_dawn_1788398693805
        ),
        PropheticQuote(
            id = "ztf_q33_pour_all_to_him",
            textEn = "When we come to the Lord Jesus Christ, we come to pour our all to Him.",
            textFr = "Lorsque nous venons au Seigneur Jésus-Christ, nous venons pour répandre notre tout devant Lui.",
            prophecySourceEn = "Unreserved Devotion",
            prophecySourceFr = "Dévouement Sans Réserve",
            themeTagEn = "TOTAL SURRENDER",
            themeTagFr = "ABANDON TOTAL",
            bookCitationEn = "Making Spiritual Progress, Volume One",
            bookCitationFr = "Faire du Progrès Spirituel, Volume 1",
            bgDrawableRes = R.drawable.quote_bg_radiant_cross_1788139262304
        ),
        PropheticQuote(
            id = "ztf_q10_retreats_progress",
            textEn = "You can withdraw constantly, consistently, and conspicuously every day on short and long retreats for spiritual progress.",
            textFr = "Tu peux te retirer constamment, fidèlement et visiblement chaque jour pour des retraites courtes et longues pour le progrès spirituel.",
            prophecySourceEn = "Spiritual Retreats",
            prophecySourceFr = "Retraites Spirituelles",
            themeTagEn = "RETREATS FOR PROGRESS",
            themeTagFr = "RETRAITES POUR LE PROGRÈS",
            bookCitationEn = "Retreats for Spiritual Progress",
            bookCitationFr = "Des Retraites Pour le Progrès Spirituel",
            bgDrawableRes = R.drawable.img_quote_prayer_mountain_1788398743570
        ),
        PropheticQuote(
            id = "ztf_q35_money_producing_disciples",
            textEn = "Money is not to be used to make one more comfortable, but to produce disciples and disciple-makers.",
            textFr = "L'argent ne doit pas être utilisé pour se rendre plus confortable, mais pour produire des disciples et des faiseurs de disciples.",
            prophecySourceEn = "Disciple Making Investment",
            prophecySourceFr = "Investissement pour Faire des Disciples",
            themeTagEn = "PRODUCING DISCIPLES",
            themeTagFr = "PRODUIRE DES DISCIPLES",
            bookCitationEn = "The Facets of His Ministry",
            bookCitationFr = "Les Facettes de Son Ministère",
            bgDrawableRes = R.drawable.img_quote_golden_path_1788398765839
        ),
        PropheticQuote(
            id = "ztf_q4_prayer_intimacy",
            textEn = "Prayer is communion with God; it is the overflow of intimacy with God.",
            textFr = "La prière est communion avec Dieu ; elle est le débordement de l'intimité avec Dieu.",
            prophecySourceEn = "Intimacy in Prayer",
            prophecySourceFr = "Intimité dans la Prière",
            themeTagEn = "INTIMACY WITH GOD",
            themeTagFr = "INTIMITÉ AVEC DIEU",
            bookCitationEn = "Prayer and a Walk with God",
            bookCitationFr = "La Prière et la Marche avec Dieu",
            bgDrawableRes = R.drawable.quote_bg_heavens_1787220708792
        ),
        PropheticQuote(
            id = "ztf_q20_giving_up_all_sin",
            textEn = "Becoming a disciple and remaining one will cost us the giving up of all sin.",
            textFr = "Devenir un disciple et le demeurer nous coûtera l'abandon de tout péché.",
            prophecySourceEn = "Purity of Heart",
            prophecySourceFr = "Pureté du Cœur",
            themeTagEn = "GIVING UP ALL SIN",
            themeTagFr = "ABANDONNER TOUT PÉCHÉ",
            bookCitationEn = "Discipleship at Any Cost",
            bookCitationFr = "Le Disciple à Tout Prix",
            bgDrawableRes = R.drawable.quote_bg_cross_1787235555876
        ),
        PropheticQuote(
            id = "ztf_q26_god_bless_all_in",
            textEn = "God cannot bless someone who cannot put his all into it.",
            textFr = "Dieu ne peut bénir quelqu'un qui ne met pas son tout dans l'œuvre.",
            prophecySourceEn = "Wholehearted Service",
            prophecySourceFr = "Service de Tout Cœur",
            themeTagEn = "PUTTING YOUR ALL",
            themeTagFr = "METTRE SON TOUT",
            bookCitationEn = "The Facets of His Ministry",
            bookCitationFr = "Les Facettes de Son Ministère",
            bgDrawableRes = R.drawable.img_quote_prayer_mountain_1788398743570
        ),
        PropheticQuote(
            id = "ztf_q39_ddewg_disciples_accountable",
            textEn = "We shall have to teach people how to have Daily Dynamic Encounters with God, how to make disciples, and how to be accountable.",
            textFr = "Nous devrons enseigner aux gens comment avoir des Rendez-vous Quotidiens avec Dieu, comment faire des disciples et comment rendre compte.",
            prophecySourceEn = "CMFI Vision",
            prophecySourceFr = "Vision CMFI",
            themeTagEn = "VISION & ACCOUNTABILITY",
            themeTagFr = "VISION ET COMPTE RENDU",
            bookCitationEn = "On Our Vision",
            bookCitationFr = "Sur Notre Vision",
            bgDrawableRes = R.drawable.quote_bg_open_bible_1788139223471
        ),
        PropheticQuote(
            id = "ztf_q40_surrendered_irrevocably",
            textEn = "The minimum condition for satisfying God’s heart is a man’s all surrendered irrevocably and irreversibly to the Lord God Almighty.",
            textFr = "La condition minimale pour satisfaire le cœur de Dieu est que l'homme Lui abandonne tout, de manière irrévocable et irréversible, au Seigneur Dieu Tout-Puissant.",
            prophecySourceEn = "Total Consecration",
            prophecySourceFr = "Consécration Totale",
            themeTagEn = "IRREVOCABLE SURRENDER",
            themeTagFr = "ABANDON IRRÉVOCABLE",
            bookCitationEn = "Soul-Winning",
            bookCitationFr = "Le Gagnagisme d'Âmes",
            bgDrawableRes = R.drawable.quote_bg_radiant_cross_1788139262304
        )
    )

    /**
     * Cycles through the 40 quotes in order, starting from the day-of-year.
     * With 40 items, each quote appears once every 40 days without repetition.
     */
    fun getQuoteForDay(dayOfYear: Int, offset: Int = 0): PropheticQuote {
        val total = ALL.size
        val index = (((dayOfYear - 1 + offset) % total) + total) % total
        return ALL[index]
    }

    /**
     * Returns the total count of quotes in the cycle (40).
     */
    fun getCycleCount(): Int = ALL.size

    /**
     * Returns the 1-based cycle position for UI indication (e.g. 1/40).
     */
    fun getCyclePosition(dayOfYear: Int, offset: Int = 0): Int {
        val total = ALL.size
        return ((((dayOfYear - 1 + offset) % total) + total) % total) + 1
    }
}
