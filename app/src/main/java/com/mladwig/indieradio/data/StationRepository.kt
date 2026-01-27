package com.mladwig.indieradio.data

import androidx.compose.material3.RadioButton
import com.mladwig.indieradio.model.RadioStation

object StationRepository {
    fun getStations() : List<RadioStation> = listOf(
        // Local/Midwest stations
        RadioStation(
            id = "radiomke",
            name = "88Nine Radio Milwaukee",
            streamUrl = "https://wyms.streamguys1.com/live",
            description = "Where Milwaukee discovers new music",
            genre = "Adult Album Alternative",
            location = "Milwaukee, WI",
            website = "https://radiomilwaukee.org"
        ),
        RadioStation(
            id = "riverwestradio",
            name = "Riverwest Radio 104.1 FM",
            streamUrl = "http://stream.riverwestradio.com:8000/riverwestradio",
            description = "Where the world experiences Riverwest",
            genre = "Community/Eclectic",
            location = "Riverwest, Milwaukee, WI",
            website = "https://riverwestradio.com"
        ),
        RadioStation(
            id = "thecurrent",
            name = "The Current",
            streamUrl = "https://current.stream.publicradio.org/current.mp3",
            description = "Minnesota's music discovery",
            genre = "Alternative",
            location = "Minneapolis, MN",
            website = "https://thecurrent.org"
        ),

        // Colorado
        RadioStation(
            id = "coloradosound",
            name = "The Colorado Sound",
            streamUrl = "https://ais-sa1.streamon.fm/7891_96k.aac",
            description = "Colorado's independent music station",
            genre = "Adult Album Alternative",
            location = "Denver, CO",
            website = "https://coloradosound.org"
        ),
        // East Coast
        RadioStation(
            id = "wfmu",
            name = "WFMU 91.1 FM",
            streamUrl = "https://stream0.wfmu.org/freeform-128k",
            description = "Longest-running freeform radio in the US",
            genre = "Freeform",
            location = "Jersey City, NJ",
            website = "https://wfmu.org"
        ),
        RadioStation(
            id = "wxpn",
            name = "WXPN 88.5 FM",
            streamUrl = "https://wxpnhi.xpn.org/xpnhi",
            description = "Home of World Cafe",
            genre = "Adult Album Alternative",
            location = "Philadelphia, PA",
            website = "https://xpn.org"
        ),
        //West Coast
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
            id = "kcrw",
            name = "KCRW 89.9 FM",
            streamUrl = "https://streams.kcrw.com/kcrw_mp3?aw_0_1st.playerid=radio-browser.info",
            description = "Eclectic music from LA",
            genre = "Eclectic/NPR",
            location = "Santa Monica, CA",
            website = "https://kcrw.com"
        ),
        //International
        RadioStation(
            id = "nts",
            name = "NTS Radio",
            streamUrl = "https://stream-relay-geo.ntslive.net/stream",
            description = "Cutting-edge underground music",
            genre = "Underground",
            location = "London, UK",
            website = "https://nts.live"
        )
    )
}