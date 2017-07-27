package net.gegy1000.hgk.session

class Tile(val height: Int, val groundType: GroundType, val vegetationType: VegetationType) {
    val colour: Int
        get() {
            return when (groundType) {
                GroundType.GROUND -> {
                    when (vegetationType) {
                        VegetationType.FOREST -> 0x00AA50
                        VegetationType.SHRUBLAND -> 0x80AA50
                        else -> 0x57DB57
                    }
                }
                GroundType.WATER -> 0x0078FF
                GroundType.CORNUCOPIA -> 0x7F0000
                else -> 0xAABBFF
            }
        }

    constructor(data: Long) : this((data shr 24).toInt() and 0xFF, GroundType.values()[(data shr 12 and 0xF).toInt()], VegetationType.values()[(data shr 20 and 0xF).toInt()])
}
