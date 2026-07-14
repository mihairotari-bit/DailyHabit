package com.mihai.dailyhabit

fun main() {
    val regex = Regex("(?i)^(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>.+)$")
    val line = "150g pane integrale (vedi alternative del pranzo)"
    println(regex.matches(line))
}
