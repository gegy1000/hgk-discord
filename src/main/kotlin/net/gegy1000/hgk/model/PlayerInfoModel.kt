package net.gegy1000.hgk.model

class PlayerInfoModel(val name: String, val pronoun: Pronoun) {
    enum class Pronoun {
        NEUTRAL,
        MALE,
        FEMALE
    }
}
