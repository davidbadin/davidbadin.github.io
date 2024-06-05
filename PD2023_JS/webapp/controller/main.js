// global data
let aDataEvents = [];

// onInit
function onInit(loadData) {

    if (loadData === true) {
        // get data and create elements
        getAppData();
    } else {
        // create elements only
        createElements();
    };

    console.log("end of init");

};

function getAppData() {

    let urlMainData = con.apiUri + con.sheetId + "/values/" + con.sheetRangeData + "?key=" + con.apiKey;
    this.loadData (urlMainData, this.processData, this, true);

};

function loadData(theUrl, callback, that, isAtStart) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText, that, isAtStart);
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous 
    xmlHttp.send(null);
};

function processData(response, that, isAtStart) {
    
    let obj = JSON.parse( response );
    let sourceData = obj.values;
    let stage;
    let aDataEventsLoaded = [];
    
    for ( var i = 0; i < sourceData.length; i++ ) {
        if (sourceData[i]) { 									// skip if empty row
            
            stage = "";
            stage = con.stage.find(o => o.shortName == sourceData[i][4] );
            
            let band = sourceData[i][2];
            let startDate = that.formatDate( sourceData[i][0] );
            let endDate = that.formatDate( sourceData[i][1] );
            let descrShort = that.formatShortDescr( stage.name, startDate, endDate ); 
            let descrLong = that.formatLongDescr( descrShort, sourceData[i][3] );
            let spotifyUrl = sourceData[i][5];
            let id = sourceData[i][6];
            let favorite = false;

            // get "favorite"
            let fav = aDataEvents.find(o => o.id == id );
            if (fav) { 
                // if ID found
                favorite = fav.favorite;
            };

            aDataEventsLoaded.push({
                "band": band,
                "start": startDate,
                "end": endDate,
                "stage": stage.shortName,
                "shortDescription": descrShort,
                "description": descrLong,
                "spotUrl": spotifyUrl,
                "favorite": favorite,
                "id": id
            });							
        }
    }    

    aDataEventsLoaded.sort(function(a, b) { return a.start - b.start } );

    // assign to global data
    aDataEvents = aDataEventsLoaded;

    console.log(aDataEvents);
    
    // create elements
    createElements();   

};

function setInitProperties() {
    let totalHours;
    
    // get total hours
    totalHours = con.schedEndHour - con.schedStartHour;
    if (totalHours <= 0) {
        totalHours = totalHours + 24;
    }
    cust.mainBlockHeight = (totalHours * con.sizeDefHour * cust.sizeCust + con.labelHourHeight );
    
    document.getElementById("textHeaderTitle").textContent = con.titleLong;
    document.getElementById("divMainBlock").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
    document.getElementById("divHoursLabel").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
};

function createElements() {   

    // get initial properties
    setInitProperties();

    // create HOURS LABEL
    createHoursLabel();

    // create EVENT columns
    createEventColumns();

};

function createHoursLabel() {
    let parrentDiv;
    let newDiv;
    let newSpan;
    let currHour;
    let schedEnd;

    parrentDiv = document.getElementById("divHoursLabel");
    parrentDiv.textContent = "";        // clear DIV (remove all child elements)
    currHour = con.schedStartHour;
    schedEnd = con.schedEndHour > con.schedStartHour ? con.schedEndHour : con.schedEndHour + 24;

    while ( currHour <= schedEnd) {
        newDiv = document.createElement("div");
        newDiv.setAttribute("class", "divHourLabel");
        newSpan = document.createElement("span");
        newSpan.textContent = currHour < 24 ? currHour : currHour - 24;
        
        newDiv.appendChild(newSpan);
        parrentDiv.appendChild(newDiv);
        currHour++;
    }
};

function createEventColumns() {
    let parrentDiv;                 // main block for stages/events
    let newCol = {};                // column for a stage
    let newEvent = {};              // event
    let numberOfStages;             // number of stages                
    let stageData = {};             // stage name, style,...
    let currDateStart;              // begin of timeline
    let currDateEnd;                // end of timeline
    let prevDate;                   // end of the previous event (same day & stage; initial value = start of timeline)

    // initialization
    numberOfStages = con.stage.length;
    parrentDiv = document.getElementById("divEventsBlock");
    parrentDiv.textContent = "";        // clear DIV (remove all child elements)
    eventDummyHeight = (con.labelHourHeight / 2);
    currDateStart = getCurrentDayStart( );
    currDateEnd = getCurrentDayEnd( );

    for (let iStage = 0; iStage < numberOfStages; iStage++) {      

        // create column for a stage
        newCol = document.createElement("div");
        createNewCol(newCol, iStage);
        stageData = con.stage[iStage];

        // get the current date & time - start of the timeline
        prevDate = currDateStart;
        
        //create events for this stage
        for (let iEvent = 0; iEvent < aDataEvents.length; iEvent++) {
                       
            if ( aDataEvents[iEvent].stage !== stageData.shortName ) {
                // skip if it is another stage
                continue;
            };
            
            if ( aDataEvents[iEvent].start < currDateStart || aDataEvents[iEvent].end > currDateEnd ) {
                // skip events that are outside the timeline
                continue;
            };

            // create single event
            newEvent = createNewEvent(aDataEvents[iEvent], prevDate);    

            newCol.appendChild(newEvent);
            prevDate = aDataEvents[iEvent].end;

        }

        parrentDiv.appendChild(newCol);

    }
};

function createNewCol(newCol, stageNumber) {

    let newEventDummy;              // top dummy header for allignment

    // create column for a stage
    newCol.setAttribute("class", "divStageBlock");
    newCol.setAttribute("id", "divStage" + stageNumber);

    // create dummy header
    newEventDummy = document.createElement("div");
    newEventDummy.setAttribute("class", "divEventDummy");
    newEventDummy.setAttribute("style", "height:" + eventDummyHeight + "rem");

    newCol.appendChild(newEventDummy);
};

function getEventDimensions(startDateTime, endDateTime, prevEndDateTime) {

    let minutesEvent;
    let minutesBeforeEvent;
    let eventDimensions = { };

    //get minutes between 2 dates/times
    minutesEvent = ( endDateTime - startDateTime ) / 60000;
    minutesBeforeEvent = ( startDateTime - prevEndDateTime ) / 60000;

    //get height of event and top margin
    eventDimensions.eventHeight = con.sizeDefHour * cust.sizeCust * minutesEvent / 60;
    eventDimensions.eventMarginTop = con.sizeDefHour * cust.sizeCust * minutesBeforeEvent / 60;

    return(eventDimensions);
};

function createNewEvent( eventData, prevDate ) {

    let newEvent;
    let eventDimensions;
    let stage;

    eventDimensions = getEventDimensions(eventData.start, eventData.end, prevDate);
    stage = con.stage.find(o => o.shortName == eventData.stage );

    newEvent = document.createElement("div");
    newEvent.setAttribute("style", "height:" + eventDimensions.eventHeight + "rem; margin-top:" + eventDimensions.eventMarginTop + "rem");
    newEvent.setAttribute("id", "divEvent_" + eventData.id);
    newEvent.classList.add("divEvent");
    newEvent.classList.add(stage.style);

    return newEvent;
};