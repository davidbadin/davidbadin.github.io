console.log("Hello world");

function onInit() {

    // get data
    loadData();

    // set main block properties
    setInitProperties();
    createElements();

    console.log("end of init");

};

function loadData() {



};

function setInitProperties() {
    let totalHours;
    
    // get total hours
    totalHours = con.schedEndTime - con.schedStartTime;
    if (totalHours <= 0) {
        totalHours = totalHours + 24;
    }
    cust.mainBlockHeight = (totalHours * con.sizeDefHour * cust.sizeCust + con.labelHourHeight );
    
    document.getElementById("textHeaderTitle").textContent = con.titleLong;
    document.getElementById("divMainBlock").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
    document.getElementById("divHoursLabel").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
};

function createElements() {

    // create HOURS LABEL
    createHoursLabel();

    // create EVENTS columns
    createEventColumns();

};

function createHoursLabel() {
    let parrentDiv;
    let newDiv;
    let newSpan;
    let currHour;
    let schedEnd;

    parrentDiv = document.getElementById("divHoursLabel");
    currHour = con.schedStartTime;
    schedEnd = con.schedEndTime > con.schedStartTime ? con.schedEndTime : con.schedEndTime + 24;

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
    let numberOfStages;             // number of stages                
    let eventDimensions = {};       // event dimensions (height, top margin)


    let startDate;
    let endDate;
    let prevDate;
    

    numberOfStages = con.stage.length;
    parrentDiv = document.getElementById("divEventsBlock");
    eventDummyHeight = (con.labelHourHeight / 2);


    for (let i = 0; i < numberOfStages; i++) {

        // create column for a stage
        newCol = document.createElement("div");
        createNewCol(newCol, i);
        
        //create events
        prevDate = new Date(2024, 0, 1, con.schedStartTime, 0, 0);

        //START LOOP OVER EVENTS IN STAGE i

        startDate = new Date(2024, 0, 1, 12, 30, 0);
        endDate = new Date(2024, 0, 1, 13, 0, 0);
        
        eventDimensions = getEventDimensions(startDate, endDate, prevDate);

        let newEvent = document.createElement("div");
        newEvent.setAttribute("class", "divEvent");
        newEvent.setAttribute("style", "height:" + eventDimensions.eventHeight + "rem; margin-top:" + eventDimensions.eventMarginTop + "rem");
        //newEvent.setAttribute("id", "divEvent" + i);

        newCol.appendChild(newEvent);
        prevDate = endDate;

        //ENDLOOP 


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