
// First day of festival
let festivalStartDate = new Date(2023, 7, 23);

const con = {
    // title 
    titleLong: "Punkáči deťom 2023",
    titleShort: "PD2023",

    // dates & times
    schedStartHour: 11,
    schedEndHour: 3,
    firstDay: festivalStartDate,
    firstDayHourFocus: 16,
    days: [
        {
            name: "streda",
            number: 1
        },
        {
            name: "štvrtok",
            number: 2
        },
        {
            name: "piatok",
            number: 3
        },
        {
            name: "sobota",
            number: 4
        }
    ],
    
    // stages
    stage: [
        {
            name: "A stage",
            id: "A",
            style: "stageA"
        },
        {
            name: "B stage",
            id: "B",
            style: "stageB"
        }
    ],

    // API
    apiUri: "https://sheets.googleapis.com/v4/spreadsheets/",
    sheetId: "1YuHNmbkh-oGDLgRFZjmwuJaMhWU-iAqsi-MzGUpNS-Q",
    apiKey: "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI",
    sheetRangeData: "'PD2023'!A2:G100",

    // local data
    localStorageData: "pd2023_data",

    // format settings
    format: [
        {   
            device: "mobile",
            sizeCust: 4,
            fontSize: "2rem",
            fontSizeTitle: "3rem",
            titleHeight: "6rem",
            hoursLabelWidth: "4rem",
            popupButtonFontSize: "3rem",
            hourLabelPaddingRight: "1rem"
        },
        {
            device: "desktop",
            sizeCust: 2,
            fontSize: "1rem",
            fontSizeTitle: "1.5rem",
            titleHeight: "3rem",
            hoursLabelWidth: "2rem",
            popupButtonFontSize: "1.5rem",
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