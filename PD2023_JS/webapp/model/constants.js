
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

    // technical
    sizeDefHour: 6,
    labelHourWidth: 1.5,
    labelHourHeight: 1.25
};

let cust = {
    // technical
    sizeCust: 1.5,
    mainBlockHeight: 0,
    currDay: 1
}