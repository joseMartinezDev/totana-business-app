package com.josetotana.totanabusiness.categoryItems

import java.io.Serializable

data class CategoryItemModel(val id:String, val name: String, val imageURL: String, val address: String, val phone: String, val whatsapp: String, val maps: String, var isFav: Boolean = false): Serializable