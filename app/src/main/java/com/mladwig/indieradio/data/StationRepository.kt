package com.mladwig.indieradio.data

import com.mladwig.indieradio.model.RadioStation

object StationRepository {
    fun getStations() : List<RadioStation> = listOf(
        RadioStation(
            id = "kexp",
            name = "KEXP 90.3 FM",
            streamUrl = "https://kexp-mp3-128.streamguys1.com/kexp128.mp3",
            description = "Where the music matters",
            genre = "Alternative/Indie",
            location = "Seattle, WA",
            website = "https://kexp.org"
        ),
        RadioStation(
            id = "radiomke",
            name = "88Nine Radio Milwaukee",
            streamUrl = "https://wyms.streamguys1.com/live",
            description = "Where Milwaukee discovers new music",
            genre = "Adult Alternative",
            location = "Milwaukee, WI",
            website = "https://radiomilwaukee.org"
        )
    )
}