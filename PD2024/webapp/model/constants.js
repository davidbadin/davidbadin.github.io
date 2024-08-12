
// First day of festival
let festivalStartDate = new Date(2024, 7, 29);

const con = {
    // title 
    titleLong: "Punkáči deťom 2024",
    titleShort: "PD2024",

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
            name: "Save the Festival Stage",
            id: "B",
            style: "stageB"
        }
    ],

    // API
    apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",
    sheetId: "1pJiMjSpCzGBtQkuBDAFmA28RxgrPgKpP8sJxJ9f0kIA",
    apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
    sheetRangeData: "'PD2024'!A2:G100",

    // local data
    localStorageData: "pd2024_data",

    // format settings
    format: [
        {   
            device: "mobile",
            sizeCust: 4,
            fontSize: "2rem",
            fontSizeTitle: "3rem",
            fontSizePopup: "3rem",
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