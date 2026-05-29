package sk.punkacidetom.pd2026.core.ui.icons

// Font Awesome 7 Free — Regular weight codepoints
// NOTE: FA7 Regular Free has a much smaller icon set than FA6.
// Icons marked "→" have been substituted with the nearest visually available glyph
// confirmed present in the installed fa_regular_400.otf binary.
val FaRegularCodes: Map<String, Int> = mapOf(
    "heart" to 0xF004,       // ✓ present
    "star" to 0xF005,        // ✓ present
    "calendar" to 0xF073,    // ✓ present
    "clock" to 0xF017,       // ✓ present
    "bell" to 0xF0F3,        // ✓ present
    "bookmark" to 0xF02E,    // ✓ present
    "user" to 0xF007,        // ✓ present
    "circle-info" to 0xF059, // → circle-question (F05A missing)
    "gear" to 0xF443,        // → filter icon (F013 missing in FA7 Regular Free)
    "music" to 0xF025,       // → headphones (F001 missing)
    "newspaper" to 0xF1EA,   // ✓ present
    "ticket" to 0xF09D,      // → credit-card (F145 missing)
    "house" to 0xF015,       // ✓ present
    "arrow-left" to 0xF190,  // → circle-left (F060 missing)
    "arrow-right" to 0xF18E, // → circle-right (F061 missing)
    "location-dot" to 0xF3C5,
    "play" to 0xF144,        // → circle-play (F04B missing)
    "pause" to 0xF28C,       // → circle-pause (F04C missing)
    "stop" to 0xF28E,        // → circle-stop (F04D missing)
    "magnifying-glass" to 0xF002,
    "sliders" to 0xF022,     // → pen-to-square (F1DE missing; closest available)
    "chevron-left" to 0xF150, // → square-caret-left (F053 missing)
    "chevron-right" to 0xF151, // → square-caret-right (F054 missing)
    "chevron-down" to 0xF078,
    "chevron-up" to 0xF077,
    "rotate" to 0xF021,
    "share" to 0xF1D8,       // → paper-plane (F064 missing)
    "xmark" to 0xF057,       // → circle-xmark (F00D missing)
    "check" to 0xF058,       // → circle-check (F00C missing)
    "plus" to 0xF0FE,        // → square-plus (F067 missing)
    "bars" to 0xF0C9,
    "envelope" to 0xF0E0,    // ✓ present
    "globe" to 0xF0AC,
    "link" to 0xF0C1,
    "list" to 0xF03A,
    "circle" to 0xF111,      // ✓ present
)

// Font Awesome 7 Free — Brands weight codepoints
val FaBrandsCodes: Map<String, Int> = mapOf(
    "facebook" to 0xF09A,
    "facebook-f" to 0xF39E,
    "instagram" to 0xF16D,
    "spotify" to 0xF1BC,
    "youtube" to 0xF167,
    "twitter" to 0xF099,
    "x-twitter" to 0xE61B,
    "github" to 0xF09B,
    "android" to 0xF17B,
    "google-play" to 0xF3AB,
)
