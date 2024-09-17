package com.synaptech.weatherapp.util


fun Double.convertKelvinToF(): Double {
    return ((this - 273.15) * 1.8 + 32).round(2);
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}