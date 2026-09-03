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
    val bookCitationEn: String = "Practical Helps For Overcomers (Book 26)",
    val bookCitationFr: String = "Aides Pratiques Pour Vainqueurs (Livre 26)",
    val authorEn: String = "Pr. Zacharias Tanee Fomum",
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
    val ALL: List<PropheticQuote> = listOf(
        PropheticQuote(
            id = "bertoua_freedom_sin",
            textEn = "This return to the first love for Me will include freedom from all sin, freedom from all love of self, and freedom from all love of the world.",
            textFr = "Ce retour au premier amour pour Moi inclura la libération de tout péché, la libération de tout amour de soi et la libération de tout amour du monde.",
            prophecySourceEn = "The Bertoua Message",
            prophecySourceFr = "Le Message de Bertoua",
            themeTagEn = "FREEDOM FROM ALL SIN",
            themeTagFr = "LIBÉRATION DE TOUT PÉCHÉ",
            bgDrawableRes = R.drawable.img_quote_living_water_1788398711735
        ),
        PropheticQuote(
            id = "beijing_what_you_are",
            textEn = "Son, you must settle in your heart irrevocably that the crucial thing of what a person is, is of far greater importance than what he does. What you are is determinant.",
            textFr = "Mon fils, tu dois régler irrévocablement dans ton cœur que ce qu'une personne est devant Dieu est de bien plus grande importance que ce qu'elle fait. Ce que tu es est déterminant.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "WHAT YOU ARE IS DETERMINANT",
            themeTagFr = "CE QUE TU ES EST DÉTERMINANT",
            bgDrawableRes = R.drawable.img_quote_prayer_mountain_1788398743570
        ),
        PropheticQuote(
            id = "beijing_guide_mature_pilgrims",
            textEn = "I withdraw the youth’s guide and replace it with the guide for mature pilgrims: The Bible, The Cross and The Holy Spirit.",
            textFr = "Je retire le guide de la jeunesse et le remplace par le guide des pèlerins mûrs : la Bible, la Croix et le Saint-Esprit.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "GUIDE FOR MATURE PILGRIMS",
            themeTagFr = "GUIDE POUR PÈLERINS MÛRS",
            bgDrawableRes = R.drawable.quote_bg_radiant_cross_1788139262304
        ),
        PropheticQuote(
            id = "bertoua_uproot_sin",
            textEn = "Uproot from the heart and life: falsehood, sexual immorality, love of gain, a divided heart, self-centeredness, greed, laziness, and goal-lessness. This is the pathway to revival.",
            textFr = "Déracinez du cœur et de la vie : le mensonge, l'impudicité, l'amour du gain, le cœur partagé, l'égocentrisme, la cupidité, la paresse et le manque de but. C'est le chemin du réveil.",
            prophecySourceEn = "The Bertoua Message",
            prophecySourceFr = "Le Message de Bertoua",
            themeTagEn = "UPROOTING ALL CARNALITY",
            themeTagFr = "DÉRACINER TOUTE CARNALITÉ",
            bgDrawableRes = R.drawable.img_quote_golden_path_1788398765839
        ),
        PropheticQuote(
            id = "beijing_logos_unbroken",
            textEn = "Heaven and earth shall pass away but My word shall stand and My word shall fill the earth. The Logos of God is the Logos of God—it shall never be broken in your hand.",
            textFr = "Le ciel et la terre passeront, mais Ma parole subsistera et remplira la terre. Le Logos de Dieu est le Logos de Dieu — elle ne sera jamais brisée dans ta main.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "THE UNBREAKABLE WORD OF GOD",
            themeTagFr = "LA PAROLE DE DIEU INÉBRANLABLE",
            bgDrawableRes = R.drawable.img_quote_bible_light_1788398727365
        ),
        PropheticQuote(
            id = "beijing_abandon_carnality",
            textEn = "God has only one thing on His heart: that all His children should abandon all carnality and be clothed with His spirituality.",
            textFr = "Dieu n'a qu'une seule chose sur Son cœur : que tous Ses enfants abandonnent toute carnalité et soient revêtus de Sa spiritualité.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "ABANDON CARNALITY",
            themeTagFr = "ABANDONNER LA CARNALITÉ",
            bgDrawableRes = R.drawable.img_quote_break_dawn_1788398693805
        ),
        PropheticQuote(
            id = "beijing_daniel_communion",
            textEn = "Daniel lived in the presence of God. He knew the burden of God and bore the burden of God. Daniel and God were one.",
            textFr = "Daniel vivait dans la présence de Dieu. Il connaissait le fardeau de Dieu et portait le fardeau de Dieu. Daniel et Dieu ne faisaient qu'un.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "DANIEL'S COMMUNION WITH GOD",
            themeTagFr = "COMMUNION AVEC LE SEIGNEUR",
            bgDrawableRes = R.drawable.quote_bg_prayer_altar_1788139251276
        ),
        PropheticQuote(
            id = "brazzaville_back_to_prayer",
            textEn = "My children will come back to Me and come back to prayer. There will be prayer in pairs, in small groups, in great masses: prayers of confession and prayers of proclamation.",
            textFr = "Mes enfants reviendront à Moi et reviendront à la prière. Il y aura la prière par deux, en petits groupes, en grandes masses : des prières de confession et de proclamation.",
            prophecySourceEn = "The Congo Brazzaville Message",
            prophecySourceFr = "Le Message de Brazzaville",
            themeTagEn = "CRYING OUT IN PRAYER",
            themeTagFr = "CRIER DANS LA PRIÈRE",
            bgDrawableRes = R.drawable.quote_bg_sunrise_1787220672419
        ),
        PropheticQuote(
            id = "brazzaville_abide_presence",
            textEn = "Spend maximum time in My presence so that I can appear to you, minister to you, receive ministry from you, and reveal to you the blueprint of heaven.",
            textFr = "Passe un temps maximal dans Ma présence afin que Je puisse t'apparaître, te secourir, recevoir ton ministère et te révéler le plan du ciel.",
            prophecySourceEn = "The Congo Brazzaville Message",
            prophecySourceFr = "Le Message de Brazzaville",
            themeTagEn = "ABIDING IN HIS PRESENCE",
            themeTagFr = "DEMEURER DANS SA PRÉSENCE",
            bgDrawableRes = R.drawable.quote_bg_heavens_1787220708792
        ),
        PropheticQuote(
            id = "overcomers_cost_discipleship",
            textEn = "Jesus will have nothing to do with half-hearted people. His demands are total. He will only receive and forgive those prepared to follow Him AT ANY COST.",
            textFr = "Jésus ne fera rien avec des personnes tièdes. Ses exigences sont totales. Il ne recevra et ne pardonnera que ceux qui sont prêts à Le suivre À TOUT PRIX.",
            prophecySourceEn = "3B Prophetic Messages • Overcomers Call",
            prophecySourceFr = "Messages Prophétiques 3B • Appel aux Vainqueurs",
            themeTagEn = "FOLLOW HIM AT ANY COST",
            themeTagFr = "SUIVRE JÉSUS À TOUT PRIX",
            bgDrawableRes = R.drawable.quote_bg_cross_1787235555876
        ),
        PropheticQuote(
            id = "beijing_choose_gods_will",
            textEn = "You chose to obey Me at the cost of the work; you chose to obey Me at the cost of your life. You chose My will above everything else.",
            textFr = "Tu as choisi de M'obéir au prix de l'œuvre ; tu as choisi de M'obéir au prix de ta vie. Tu as choisi Ma volonté au-dessus de tout le reste.",
            prophecySourceEn = "The Beijing Prophecy",
            prophecySourceFr = "La Prophétie de Pékin",
            themeTagEn = "RADICAL OBEDIENCE",
            themeTagFr = "OBÉISSANCE RADICALE",
            bgDrawableRes = R.drawable.quote_bg_mountains_1787235541853
        ),
        PropheticQuote(
            id = "bertoua_there_will_be_revival",
            textEn = "There will be revival! There will be revival!! There will be revival!!! When God answers, the community will be radically committed to soul-winning.",
            textFr = "Il y aura un réveil ! Il y aura un réveil !! Il y aura un réveil !!! Lorsque Dieu répondra, la communauté sera radicalement engagée dans le gagnagisme d'âmes.",
            prophecySourceEn = "The Bertoua Message",
            prophecySourceFr = "Le Message de Bertoua",
            themeTagEn = "THERE WILL BE REVIVAL",
            themeTagFr = "IL Y AURA UN RÉVEIL",
            bgDrawableRes = R.drawable.quote_bg_global_harvest_1788139235063
        )
    )

    fun getQuoteForDay(dayOfYear: Int, offset: Int = 0): PropheticQuote {
        val index = ((dayOfYear + offset) % ALL.size + ALL.size) % ALL.size
        return ALL[index]
    }
}
