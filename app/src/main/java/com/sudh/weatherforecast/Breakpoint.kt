package com.sudh.weatherforecast

data class Breakpoint(val cLow: Double, val cHigh: Double, val iLow: Int, val iHigh: Int)

val pm25Breakpoints = listOf(
    Breakpoint(0.0, 30.0, 0, 50),
    Breakpoint(31.0, 60.0, 51, 100),
    Breakpoint(61.0, 90.0, 101, 200),
    Breakpoint(91.0, 120.0, 201, 300),
    Breakpoint(121.0, 250.0, 301, 400),
    Breakpoint(251.0, 500.0, 401, 500)
)

val pm10Breakpoints = listOf(
    Breakpoint(0.0, 50.0, 0, 50),
    Breakpoint(51.0, 100.0, 51, 100),
    Breakpoint(101.0, 250.0, 101, 200),
    Breakpoint(251.0, 350.0, 201, 300),
    Breakpoint(351.0, 430.0, 301, 400),
    Breakpoint(431.0, 600.0, 401, 500)
)

fun calculateSubIndex(value: Double, breakpoints: List<Breakpoint>): Int {
    for (bp in breakpoints) {
        if (value in bp.cLow..bp.cHigh) {
            return ((bp.iHigh - bp.iLow) * (value - bp.cLow) / (bp.cHigh - bp.cLow) + bp.iLow).toInt()
        }
    }
    return -1
}

fun calculateNAQI(pm25: Double?, pm10: Double?): Int {
    val pm25Index = pm25?.let { calculateSubIndex(it, pm25Breakpoints) } ?: -1
    val pm10Index = pm10?.let { calculateSubIndex(it, pm10Breakpoints) } ?: -1
    return listOf(pm25Index, pm10Index).maxOrNull() ?: -1
}
