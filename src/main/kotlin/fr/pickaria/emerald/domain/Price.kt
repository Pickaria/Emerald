package fr.pickaria.emerald.domain

import kotlin.math.round

data class Price(val amount: Double, val currency: String) {
    companion object {
        fun roundedToTwoDecimals(amount: Double, currency: String): Price {
            return Price(round(amount * 100) / 100, currency)
        }
    }
}
