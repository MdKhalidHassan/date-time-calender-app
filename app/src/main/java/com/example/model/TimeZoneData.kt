package com.example.model

import java.util.Locale
import java.util.TimeZone

data class TimeZoneItem(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val gmtOffset: String,
    val flag: String = "🌐"
)

object TimeZoneData {

    val popularPresets = listOf(
        TimeZoneItem("Asia/Dhaka", "Dhaka (Bangladesh)", "ঢাকা (বাংলাদেশ সময়)", "GMT+06:00", "🇧🇩"),
        TimeZoneItem("Asia/Kolkata", "Kolkata (India)", "কলকাতা (ভারত সময়)", "GMT+05:30", "🇮🇳"),
        TimeZoneItem("Asia/Riyadh", "Riyadh (Saudi Arabia)", "রিয়াদ (সৌদি আরব)", "GMT+03:00", "🇸🇦"),
        TimeZoneItem("Asia/Dubai", "Dubai (UAE)", "দুবাই (সংযুক্ত আরব আমিরাত)", "GMT+04:00", "🇦🇪"),
        TimeZoneItem("Europe/London", "London (UK / GMT)", "লন্ডন (যুক্তরাজ্য)", "GMT+00:00", "🇬🇧"),
        TimeZoneItem("UTC", "UTC (Universal Coordinated)", "ইউটিসি (আন্তর্জাতিক সময়)", "GMT+00:00", "🌐"),
        TimeZoneItem("America/New_York", "New York (USA Eastern)", "নিউ ইয়র্ক (যুক্তরাষ্ট্র)", "GMT-05:00", "🇺🇸"),
        TimeZoneItem("America/Los_Angeles", "Los Angeles (USA Pacific)", "লস অ্যাঞ্জেলেস (যুক্তরাষ্ট্র)", "GMT-08:00", "🇺🇸"),
        TimeZoneItem("America/Chicago", "Chicago (USA Central)", "শিকাগো (যুক্তরাষ্ট্র)", "GMT-06:00", "🇺🇸"),
        TimeZoneItem("America/Denver", "Denver (USA Mountain)", "ডেনভার (যুক্তরাষ্ট্র)", "GMT-07:00", "🇺🇸"),
        TimeZoneItem("Asia/Singapore", "Singapore", "সিঙ্গাপুর", "GMT+08:00", "🇸🇬"),
        TimeZoneItem("Asia/Kuala_Lumpur", "Kuala Lumpur (Malaysia)", "কুয়ালালামপুর (মালয়েশিয়া)", "GMT+08:00", "🇲🇾"),
        TimeZoneItem("Asia/Tokyo", "Tokyo (Japan)", "টোকিও (জাপান সময়)", "GMT+09:00", "🇯🇵"),
        TimeZoneItem("Australia/Sydney", "Sydney (Australia)", "সিডনি (অস্ট্রেলিয়া)", "GMT+10:00", "🇦🇺"),
        TimeZoneItem("America/Toronto", "Toronto (Canada)", "টরন্টো (কানাডা)", "GMT-05:00", "🇨🇦"),
        TimeZoneItem("Europe/Berlin", "Berlin (Germany)", "বার্লিন (জার্মানি)", "GMT+01:00", "🇩🇪"),
        TimeZoneItem("Europe/Paris", "Paris (France)", "প্যারিস (ফ্রান্স)", "GMT+01:00", "🇫🇷"),
        TimeZoneItem("Asia/Bangkok", "Bangkok (Thailand)", "ব্যাংকক (থাইল্যান্ড)", "GMT+07:00", "🇹🇭"),
        TimeZoneItem("Asia/Karachi", "Karachi (Pakistan)", "করাচি (পাকিস্তান)", "GMT+05:00", "🇵🇰"),
        TimeZoneItem("Asia/Qatar", "Doha (Qatar)", "দোহা (কাতার)", "GMT+03:00", "🇶🇦"),
        TimeZoneItem("Asia/Kuwait", "Kuwait City", "কুয়েত সিটি", "GMT+03:00", "🇰🇼"),
        TimeZoneItem("Asia/Muscat", "Muscat (Oman)", "মাস্কাট (ওমান)", "GMT+04:00", "🇴🇲"),
        TimeZoneItem("Africa/Cairo", "Cairo (Egypt)", "কায়রো (মিশর)", "GMT+02:00", "🇪🇬"),
        TimeZoneItem("Asia/Seoul", "Seoul (South Korea)", "সিউল (দক্ষিণ কোরিয়া)", "GMT+09:00", "🇰🇷")
    )

    val standardGmtOffsets = listOf(
        "GMT-12:00", "GMT-11:00", "GMT-10:00", "GMT-09:00", "GMT-08:00", "GMT-07:00",
        "GMT-06:00", "GMT-05:00", "GMT-04:00", "GMT-03:30", "GMT-03:00", "GMT-02:00",
        "GMT-01:00", "GMT+00:00", "GMT+01:00", "GMT+02:00", "GMT+03:00", "GMT+03:30",
        "GMT+04:00", "GMT+04:30", "GMT+05:00", "GMT+05:30", "GMT+05:45", "GMT+06:00",
        "GMT+06:30", "GMT+07:00", "GMT+08:00", "GMT+08:45", "GMT+09:00", "GMT+09:30",
        "GMT+10:00", "GMT+10:30", "GMT+11:00", "GMT+12:00", "GMT+13:00", "GMT+14:00"
    ).map { offset ->
        TimeZoneItem(
            id = offset,
            nameEn = "Standard $offset",
            nameBn = "স্ট্যান্ডার্ড $offset",
            gmtOffset = offset,
            flag = "⏱️"
        )
    }

    fun getDisplayName(id: String, isBengali: Boolean): String {
        popularPresets.find { it.id.equals(id, ignoreCase = true) }?.let {
            return if (isBengali) "${it.flag} ${it.nameBn} [${it.gmtOffset}]" else "${it.flag} ${it.nameEn} [${it.gmtOffset}]"
        }
        standardGmtOffsets.find { it.id.equals(id, ignoreCase = true) }?.let {
            return if (isBengali) "${it.flag} ${it.nameBn}" else "${it.flag} ${it.nameEn}"
        }

        val tz = TimeZone.getTimeZone(id)
        val offsetMillis = tz.getOffset(System.currentTimeMillis())
        val offsetHours = offsetMillis / (1000 * 60 * 60)
        val offsetMins = Math.abs(offsetMillis / (1000 * 60)) % 60
        val sign = if (offsetMillis >= 0) "+" else "-"
        val offsetStr = String.format(Locale.US, "GMT%s%02d:%02d", sign, Math.abs(offsetHours), offsetMins)
        val cleanName = id.substringAfterLast('/').replace('_', ' ')

        return "$offsetStr ($cleanName)"
    }
}
