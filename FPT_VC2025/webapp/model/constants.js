
// First day of festival
let festivalStartDate = new Date(2025, 6, 7);

const con = {
    // title 
    titleLong: "Fpt VolleyCup 2025",
    titleShort: "FPT_VC2025",

    // dates & times
    schedStartHour: 8,
    schedEndHour: 20,
    firstDay: festivalStartDate,
    firstDayHourFocus: 10,
    days: [
        {
            name: "sobota",
            number: 1
        }
    ],
    
    // stages
    stage: [
        {
            name: "Ihrisko 1",
            id: "A",
            style: "stageA"
        },
        {
            name: "Ihrisko 2",
            id: "B",
            style: "stageB"
        },
        {
            name: "Program",
            id: "C",
            style: "stageC"
        }
    ],

    // API
    apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",
    sheetId: "1pJiMjSpCzGBtQkuBDAFmA28RxgrPgKpP8sJxJ9f0kIA",
    apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
    sheetRangeData: "'PD2024'!A2:G100",

    // Spotify playlist
    spotifyPlaylist: "https://open.spotify.com/playlist/3hVSERyTLVDufAcI2KHSrv",

    // local data
    localStorageData: "fpt_vc2025_data",

    // format settings
    format: [
        {   
            device: "mobile",
            sizeCust: 4,
            fontSize: "2rem",
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