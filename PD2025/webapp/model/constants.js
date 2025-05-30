
// First day of festival
let festivalStartDate = new Date(2024, 7, 29);

const con = {
    // title 
    titleLong: "Punkáči deťom 2025",
    titleShort: "PD2025",

    // dates & times
    schedStartHour: 11,
    schedEndHour: 2,
    firstDay: festivalStartDate,
    firstDayHourFocus: 16,
    days: [
        {
            name: "štvrtok",
            number: 1
        },
        {
            name: "piatok",
            number: 2
        },
        {
            name: "sobota",
            number: 3
        }
    ],
    
    // stages
    stage: [
        {
            name: "Main Open Air Stage",
            id: "A",
            style: "stageA"
        },
        {
            name: "United Stage",
            id: "B",
            style: "stageB"
        }
    ],

    // API
    apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",
    sheetId: "1VvLnbTEg61lK6h6aib0O8wTEuWpGaYieLCCQ4qfUg5U",
    apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
    sheetRangeData: "'PD2025'!A2:G100",

    // Spotify playlist
    spotifyPlaylist: "https://open.spotify.com/playlist/6xLDXTKkGWlNS2Ky2enHuI",

    // local data
    localStorageData: "pd2025_data",

    // format settings
    format: [
        {   
            device: "mobile",
            sizeCust: 4,
            fontSize: "1.5rem",
            fontSizeTitle: "3rem",
            fontSizePopup: "3rem",
            fontSizePopupDescr: "2rem",
            fontSizeSpotifyLogo: "2rem",
            titleHeight: "6rem",
            hoursLabelWidth: "4rem",
            popupButtonFontSize: "5rem",
            hourLabelPaddingRight: "1rem"
        },
        {
            device: "desktop",
            sizeCust: 2,
            fontSize: "1rem",
            fontSizeTitle: "1.5rem",
            fontSizePopup: "1.25rem",
            fontSizePopupDescr: "1rem",
            fontSizeSpotifyLogo: "1.5rem",
            titleHeight: "3rem",
            hoursLabelWidth: "2rem",
            popupButtonFontSize: "2.5rem",
            hourLabelPaddingRight: "0.75rem"
        },
    ],

    // technical
    sizeDefHour: 6,
    labelHourHeight: 1.25
};

let cust = {
    // technical
    sizeCust: 1.5,
    mainBlockHeight: 0,
    currDay: 1
}