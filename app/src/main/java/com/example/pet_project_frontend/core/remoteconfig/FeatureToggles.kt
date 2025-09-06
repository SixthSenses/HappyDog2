package com.example.pet_project_frontend.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

enum class CardType { water, activity, meal, weight, bcs }

interface FeatureToggles {
    fun visibleCards(): List<CardType>
}

@Singleton
class RemoteConfigToggles @Inject constructor(
    private val rc: FirebaseRemoteConfig
) : FeatureToggles {
    override fun visibleCards(): List<CardType> {
        val orderJson = rc.getString("petcare_cards_order").ifBlank { "[\"water\",\"activity\",\"meal\",\"weight\",\"bcs\"]" }
        val hiddenJson = rc.getString("petcare_cards_hidden").ifBlank { "[]" }
        val order = parseOrder(orderJson)
        val hidden = parseHidden(hiddenJson).toSet()
        return order.filter { it !in hidden }
    }

    private fun parseOrder(json: String): List<CardType> = try {
        val arr = com.google.gson.JsonParser.parseString(json).asJsonArray
        arr.mapNotNull { runCatching { CardType.valueOf(it.asString) }.getOrNull() }
    } catch (_: Throwable) {
        listOf(CardType.water, CardType.activity, CardType.meal, CardType.weight, CardType.bcs)
    }

    private fun parseHidden(json: String): List<CardType> = try {
        val arr = com.google.gson.JsonParser.parseString(json).asJsonArray
        arr.mapNotNull { runCatching { CardType.valueOf(it.asString) }.getOrNull() }
    } catch (_: Throwable) {
        emptyList()
    }
}
