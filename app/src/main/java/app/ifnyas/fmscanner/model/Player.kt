package app.ifnyas.fmscanner.model

import kotlin.math.roundToInt
import kotlin.random.Random


@Suppress("MemberVisibilityCanBePrivate")
class Player(
    var positionId: Int? = null,
    var positionName: String? = null,
    var preferredFootId: Int? = null,
    var preferredFootName: String? = null,
    var attributes: List<PA> = listOf(
        // GoalKeeping
        PA("Goalkeeping", "Aerial Reach"),
        PA("Goalkeeping", "Command Of Area"),
        PA("Goalkeeping", "Communication"),
        PA("Goalkeeping", "Eccentricity"),
        PA("Goalkeeping", "Handling"),
        PA("Goalkeeping", "Kicking"),
        PA("Goalkeeping", "One On Ones"),
        PA("Goalkeeping", "Punching (Tendency)"),
        PA("Goalkeeping", "Reflexes"),
        PA("Goalkeeping", "Rushing Out (Tendency)"),
        PA("Goalkeeping", "Throwing"),

        // Technical
        PA("Technical", "Corners"),
        PA("Technical", "Crossing"),
        PA("Technical", "Dribbling"),
        PA("Technical", "Finishing"),
        PA("Technical", "First Touch"),
        PA("Technical", "Free Kick Taking"),
        PA("Technical", "Heading"),
        PA("Technical", "Long Shots"),
        PA("Technical", "Long Throws"),
        PA("Technical", "Marking"),
        PA("Technical", "Passing"),
        PA("Technical", "Penalty Taking"),
        PA("Technical", "Tackling"),
        PA("Technical", "Technique"),

        // Mental
        PA("Mental", "Aggression"),
        PA("Mental", "Anticipation"),
        PA("Mental", "Bravery"),
        PA("Mental", "Composure"),
        PA("Mental", "Concentration"),
        PA("Mental", "Decisions"),
        PA("Mental", "Determination"),
        PA("Mental", "Flair"),
        PA("Mental", "Leadership"),
        PA("Mental", "Off The Ball"),
        PA("Mental", "Positioning"),
        PA("Mental", "Teamwork"),
        PA("Mental", "Vision"),
        PA("Mental", "Work Rate"),

        // Physical
        PA("Physical", "Acceleration"),
        PA("Physical", "Agility"),
        PA("Physical", "Balance"),
        PA("Physical", "Jumping Reach"),
        PA("Physical", "Natural Fitness"),
        PA("Physical", "Pace"),
        PA("Physical", "Stamina"),
        PA("Physical", "Strength"),
        PA("Physical", "Weaker Foot")
    )
) {
    fun setHiddenAttrsToOne() {
        hiddenAttrs().onEach { item ->
            attributes.find { it.name == item }?.point = 1
        }
    }

    fun setZeroAttrsToOne() {
        if (positionId == null) setPosition("Striker")
        if (preferredFootId == null) setPreferredFoot("Either")
        attributes.onEach { it.point = if (it.point == 0) 1 else it.point }
    }

    fun setAttrsWeight() {
        when (positionId) {
            POS_GK -> listOf(
                6, 6, 5, 0, 8, 5, 4, 0, 8, 0, 3,
                0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 3, 0, 0, 1,
                0, 3, 6, 2, 6, 10, 0, 0, 2, 0, 5, 2, 1, 1,
                6, 8, 2, 1, 0, 3, 1, 4, 3
            )
            POS_DRL -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 2, 1, 1, 3, 1, 2, 1, 1, 3, 2, 1, 4, 2,
                0, 3, 2, 2, 4, 7, 0, 0, 1, 1, 4, 2, 2, 2,
                7, 6, 2, 2, 0, 5, 6, 4, 4
            )
            POS_DC -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 2, 1, 5, 1, 1, 8, 2, 1, 5, 1,
                0, 5, 2, 2, 4, 10, 0, 0, 2, 1, 8, 1, 1, 2,
                6, 6, 2, 6, 0, 5, 3, 6, 4.5
            )
            POS_WBRL -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 3, 2, 1, 3, 1, 1, 1, 1, 2, 3, 1, 3, 3,
                0, 3, 1, 2, 3, 5, 0, 0, 1, 2, 3, 2, 2, 2,
                8, 5, 2, 1, 0, 6, 7, 4, 4
            )
            POS_DM -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 2, 2, 4, 1, 1, 3, 1, 3, 4, 1, 7, 3,
                0, 5, 1, 2, 3, 8, 0, 0, 1, 1, 5, 2, 4, 4,
                6, 6, 2, 1, 0, 4, 4, 5, 5
            )
            POS_MRL -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 5, 3, 2, 4, 1, 1, 2, 1, 1, 3, 1, 2, 4,
                0, 3, 1, 3, 2, 5, 0, 0, 1, 2, 1, 2, 3, 3,
                8, 6, 2, 1, 0, 6, 5, 3, 5
            )
            POS_MC -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 2, 2, 6, 1, 1, 3, 1, 3, 6, 1, 3, 4,
                0, 3, 1, 3, 2, 7, 0, 0, 1, 3, 3, 2, 6, 3,
                6, 6, 2, 1, 0, 5, 6, 4, 6
            )
            POS_AMRL -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 5, 5, 2, 5, 1, 1, 2, 1, 1, 2, 1, 2, 4,
                0, 3, 1, 3, 2, 5, 0, 0, 1, 2, 1, 2, 3, 3,
                10, 6, 2, 1, 0, 10, 7, 3, 5.5
            )
            POS_AMC -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 3, 3, 5, 1, 1, 3, 1, 1, 4, 1, 2, 5,
                0, 3, 1, 3, 2, 6, 0, 0, 1, 3, 2, 2, 6, 3,
                9, 6, 2, 1, 0, 7, 6, 3, 7
            )
            POS_STC -> listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 2, 5, 8, 6, 1, 6, 2, 1, 1, 2, 1, 1, 4,
                0, 5, 1, 6, 2, 5, 0, 0, 1, 6, 2, 1, 2, 2,
                10, 6, 2, 5, 0, 7, 6, 6, 7.5
            )
            else -> emptyList()
        }.forEachIndexed { index, i -> attributes[index].weight = i.toDouble() }
    }

    fun getReportAttrs(): List<String> {
        return mutableListOf<String>().apply {
            // add position
            add("// General //")
            add("Position: $positionName")

            // add attributes
            attributes.forEach { attr ->
                val addCat = if (isEmpty()) true else attributes.find {
                    this[lastIndex].contains(it.name.split(":")[0])
                }?.category != attr.category

                if (addCat) add("// ${attr.category} //")
                add("${attr.name}: ${attr.point}")
            }
        }
    }

    fun getPositionList(): List<Pair<String, Int>> {
        return listOf(
            Pair("Goalkeeper", POS_GK),
            Pair("Defender (Right)", POS_DRL),
            Pair("Defender (Left)", POS_DRL),
            Pair("Defender (Centre)", POS_DC),
            Pair("Wing Back (Right)", POS_WBRL),
            Pair("Wing Back (Left)", POS_WBRL),
            Pair("Defensive Midfielder", POS_DM),
            Pair("Midfielder (Right)", POS_MRL),
            Pair("Midfielder (Left)", POS_MRL),
            Pair("Midfielder (Centre)", POS_MC),
            Pair("Attacking Midfielder (Right)", POS_AMRL),
            Pair("Attacking Midfielder (Left)", POS_AMRL),
            Pair("Attacking Midfielder (Centre)", POS_AMC),
            Pair("Striker (Centre)", POS_STC)
        )
    }

    fun getPreferredFootList(): List<Pair<String, Int>> {
        return listOf(
            Pair("Right Only", PF_ONE_SIDE_ONLY),
            Pair("Left Only", PF_ONE_SIDE_ONLY),
            Pair("Right", PF_ONE_SIDE),
            Pair("Left", PF_ONE_SIDE),
            Pair("Either", PF_EITHER)
        )
    }

    fun setPosition(name: String) {
        positionId = getPositionList().find { it.first == name }?.second
        positionName = name
        setHiddenAttrsToOne()
        setAttrsWeight()
    }

    fun setPreferredFoot(name: String) {
        preferredFootId = getPreferredFootList().find { it.first == name }?.second
        preferredFootName = name
        attributes[attributes.lastIndex].point = when (preferredFootId) {
            PF_ONE_SIDE_ONLY -> Random.nextInt(1, 6)
            PF_ONE_SIDE -> Random.nextInt(7, 12)
            PF_EITHER -> Random.nextInt(13, 18)
            else -> 1
        }
    }

    fun getRating(): Int {
        val sumAttrs = attributes.map { it.point }.sum()
        val sumWeights = attributes.map { it.weight }.sum()
        val avgCaWeighted = sumAttrs / sumWeights

        val sumAttrMod = attributes.map {
            (it.point * it.weight) * (sumWeights + avgCaWeighted) / sumWeights
        }.sum()

        val finalMod = sumAttrMod / sumWeights * (sumWeights + avgCaWeighted) / sumWeights * 10
        return finalMod.roundToInt()
    }

    private fun hiddenAttrs(): List<String> = if (positionId == POS_GK) listOf(
        "Corners", "Crossing",
        "Dribbling", "Finishing",
        "First Touch", "Heading",
        "Long Shots", "Long Throws",
        "Marking", "Passing", "Tackling"
    ) else listOf(
        "Aerial Reach", "Command Of Area",
        "Communication", "Eccentricity",
        "Handling", "Kicking", "One On Ones",
        "Punching (Tendency)", "Reflexes",
        "Rushing Out (Tendency)", "Throwing"
    )

    companion object {
        const val POS_GK = 0
        const val POS_DRL = 1
        const val POS_DC = 2
        const val POS_WBRL = 3
        const val POS_DM = 4
        const val POS_MRL = 5
        const val POS_MC = 6
        const val POS_AMRL = 7
        const val POS_AMC = 8
        const val POS_STC = 9

        const val PF_ONE_SIDE_ONLY = 0
        const val PF_ONE_SIDE = 1
        const val PF_EITHER = 2
    }
}