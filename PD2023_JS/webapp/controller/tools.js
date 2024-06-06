function formatDate( date ) {
    
    let year, month, day, hour, minute;
            
    year = date.slice(6, 10);
    month = parseInt(date.slice(3, 5)) - 1;
    day = date.slice(0, 2);

    if ( date.slice(12, 13) === ":" ) {
        hour = "0" + date.slice(11, 12);	
        minute = date.slice(13, 15);
    } else {
        hour = date.slice(11, 13);	
        minute = date.slice(14, 16);
    }

    let formatedDate = new Date(year, month, day, hour, minute);

    return formatedDate;

};


function formatShortDescr( stage, startDate, endDate ) {
    
    let startTime;
    let endTime; 
    
    startTime = startDate.getHours() + ":" + ( startDate.getMinutes() < 10 ? "0" : "")  + startDate.getMinutes();
    endTime = endDate.getHours() + ":" + ( endDate.getMinutes() < 10 ? "0" : "") + endDate.getMinutes();

    return startTime + "-" + endTime + ", " + stage;
};

function formatLongDescr( shortDescr, longDescr) {
    return shortDescr + "\n" + "\n" + longDescr;
};

function getCurrentDayStart( ) {

    let currDate = new Date( 
        con.firstDay.getFullYear(), 
        con.firstDay.getMonth(),
        con.firstDay.getDate(),
        con.schedStartHour,
        0,
        0
    );

    currDate.setDate( currDate.getDate() - 1 + cust.currDay );
    return currDate;

};

function getCurrentDayEnd( ) {

    let plusDays;

    let currDate = new Date( 
        con.firstDay.getFullYear(), 
        con.firstDay.getMonth(),
        con.firstDay.getDate(),
        con.schedEndHour,
        0,
        0
    );

    plusDays = cust.currDay;
    if (con.schedStartHour > con.schedEndHour) {
        // end hour is after midnight
        plusDays = plusDays + 1;
    }

    currDate.setDate( currDate.getDate() - 1 + plusDays );
    return currDate;

};

function getLocalData(localDataName) {

    let localData = [];
    
    localData = JSON.parse(localStorage.getItem(localDataName));

    // string to date
    for (let i = 0; i < localData.length; i++) {
        localData[i].start = new Date(localData[i].start);
        localData[i].end = new Date(localData[i].end);
    };

    return localData;
};

function setLocalData(localDataName, data) {
    localStorage.setItem(localDataName, JSON.stringify(data));  
};