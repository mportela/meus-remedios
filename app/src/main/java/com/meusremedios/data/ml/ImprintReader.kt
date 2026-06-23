package com.meusremedios.data.ml

interface ImprintReader {
    suspend fun read(imagePath: String): String?
}
