package com.mladwig.indieradio.model

import android.R

data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val description: String,
    val genre: String,
    val location: String,
    val logoUrl: String? = null,
    val website: String? = null
)