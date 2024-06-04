console.log("Hello world");



function onInit() {

    // set main block properties
    setInitProperties();

    console.log("end of init");
};

function setInitProperties() {
    let totalHours;
    let mainBlockHeight;
    
    totalHours = con.schedEndTime - con.schedStartTime;
    if (totalHours <= 0) {
        totalHours = totalHours + 24;
    }
    mainBlockHeight = (totalHours * con.sizeDefHour * cust.sizeCust + con.labelHourHeight );
    
    document.getElementById("textHeaderTitle").textContent = con.titleLong;
    document.getElementById("divMainBlock").setAttribute("style", "height:" + mainBlockHeight + "rem");
    document.getElementById("divHoursLabel").setAttribute("style", "height:" + mainBlockHeight + "rem");
    // document.getElementById("divEventsBlock").setAttribute("style", "height:" + mainBlockHeight + "px");
    
    // Create HOURS LABEL
    createHoursLabel();

    // Create EVENTS columns
    createEventColumn(mainBlockHeight);

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

function createEventColumn(height) {
    let parrentDiv;
    let newDiv;
    let newEventDum;
    let numberOfStages;
    let eventHeight;

    numberOfStages = con.stage.length;
    parrentDiv = document.getElementById("divEventsBlock");
    eventHeight = (con.labelHourHeight / 2);


    for (let i = 0; i < numberOfStages; i++) {
        newDiv = document.createElement("div");
        newDiv.setAttribute("class", "divStageBlock");
        newDiv.setAttribute("id", "divStage" + i);
        newEventDum = document.createElement("div");
        newEventDum.setAttribute("class", "divEventDum");
        newEventDum.setAttribute("style", "height:" + eventHeight + "rem");
        newDiv.appendChild(newEventDum);
        parrentDiv.appendChild(newDiv);
    }
};