// global data
let aDataEvents = [];


// onInit: loadData = initial run of the app
function onInit(loadData) {

    // get device type
    setDeviceProperties();

    // get local data
    if (aDataEvents.length < 1) {
        aDataEvents = getLocalData(con.localStorageData);
    };

    if (loadData === true) {

        if(aDataEvents) {
            // display saved data first
            createElements(true);
            
            // then load data and update
            getAppData(false);

        } else {
            // get data and create elements
            getAppData(true);
        };
        
    } else {
        // create elements only
        createElements(false);
    };

};

function getAppData(initialRun) {

    let urlMainData = con.apiUri + con.sheetId + "/values/" + con.sheetRangeData + "?key=" + con.apiKey;
    this.loadData (urlMainData, this.processData, initialRun);

};

function loadData(theUrl, callback, initialRun) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText, initialRun);
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous 
    xmlHttp.send(null);
};

function processData(response, initialRun) {
    
    let obj = JSON.parse( response );
    let sourceData = obj.values;
    let stage;
    let aDataEventsLoaded = [];
    
    for ( var i = 0; i < sourceData.length; i++ ) {
        if (sourceData[i]) { 									// skip if empty row
            
            stage = "";
            stage = con.stage.find(o => o.id == sourceData[i][4] );
            
            let band = sourceData[i][2];
            let startDate = formatDate( sourceData[i][0] );
            let endDate = formatDate( sourceData[i][1] );
            let descrShort = formatShortDescr( stage.name, startDate, endDate ); 
            let descrLong = sourceData[i][3];
            let spotifyUrl = sourceData[i][5];
            let id = sourceData[i][6];
            let genre = sourceData[i][7];
            let favorite = false;

            // get "favorite"
            if(aDataEvents){
                let fav = aDataEvents.find(o => o.id == id );
                if (fav) { 
                    // if ID found
                    favorite = fav.favorite;
                };
            };       

            if ( 
                isValidDate(startDate)
                && isValidDate(endDate)
                && id != null
                && stage.id != null 
            ) {
                aDataEventsLoaded.push({
                    "band": band,
                    "start": startDate,
                    "end": endDate,
                    "stage": stage.id,
                    "shortDescription": descrShort,
                    "description": descrLong,
                    "spotUrl": spotifyUrl,
                    "favorite": favorite,
                    "id": id,
                    "genre": genre
                });		
            }					
        }
    }    

    aDataEventsLoaded.sort(function(a, b) { return a.start - b.start } );

    // compare with existing global data - if same, do nothing
    if(JSON.stringify(aDataEventsLoaded) !== JSON.stringify(aDataEvents)) {
        // assign to global data
        aDataEvents = aDataEventsLoaded;

        // save data to local
        setLocalData(con.localStorageData, aDataEvents);

        // create elements
        createElements(initialRun);     
    }      

};

function setInitProperties() {
    let totalHours;
    
    // get total hours
    totalHours = con.schedEndHour - con.schedStartHour;
    if (totalHours <= 0) {
        totalHours = totalHours + 24;
    }
    cust.mainBlockHeight = (totalHours * con.sizeDefHour * cust.sizeCust + con.labelHourHeight );
    
    // document.getElementById("textHeaderTitle").textContent = con.titleLong;
    document.getElementById("divMainBlock").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
    document.getElementById("divHoursLabel").setAttribute("style", "height:" + cust.mainBlockHeight + "rem");
};

function createElements(initialRun) {   

    // get initial properties
    setInitProperties();

    // create HOURS LABEL
    createHoursLabel();

    // create EVENT LABELS / STAGE LABELS
    createEventLabel();

    // create EVENT columns
    createEventColumns();

    // scroll to the current hour: 
        // initialRun == false => scroll to the current event if the current day
        // initialRun == true => as above; if not: scroll to first event of the first day
    scrollToHour(initialRun);

};

function createHoursLabel() {
    let parrentDiv;
    let newDiv;
    let newSpan;
    let currHour;
    let schedEnd;
    let hour;

    parrentDiv = document.getElementById("divHoursLabel");
    parrentDiv.textContent = "";        // clear DIV (remove all child elements)
    currHour = con.schedStartHour;
    schedEnd = con.schedEndHour > con.schedStartHour ? con.schedEndHour : con.schedEndHour + 24;

    while ( currHour <= schedEnd) {
        newDiv = document.createElement("div");
        newDiv.setAttribute("class", "divHourLabel");
        newDiv.setAttribute("tabindex", "-1");
        newSpan = document.createElement("span");
        hour =  currHour < 24 ? currHour : currHour - 24;
        newSpan.textContent = hour;
        
        newDiv.appendChild(newSpan);
        newDiv.setAttribute("id", "hourLabel_" + hour);
        parrentDiv.appendChild(newDiv);
        currHour++;
    }
};

function createEventLabel() {
    
    let parrentDiv;                 // main block for stages/event labels
    let newCol = {};                // column for a stage
    let newSpan;                    // stage name
    let numberOfStages;             // number of stages                

    numberOfStages = con.stage.length;
    parrentDiv = document.getElementById("divEventsLabels");
    parrentDiv.textContent = "";        // clear DIV (remove all child elements)

    for (let iStage = 0; iStage < numberOfStages; iStage++) {      
        newCol = document.createElement("div");
        newCol.setAttribute("class", "divEventsLabel " + con.stage[iStage].style );
        newSpan = document.createElement("span");
        newSpan.textContent = con.stage[iStage].name;
        newSpan.setAttribute("class", "spanEventsLabel");
        newCol.appendChild(newSpan);
        parrentDiv.appendChild(newCol);
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
    let stageCreated;               // flag - stage was created already

    // initialization
    numberOfStages = con.stage.length;
    parrentDiv = document.getElementById("divEventsBlock");
    parrentDiv.textContent = "";        // clear DIV (remove all child elements)
    eventDummyHeight = (con.labelHourHeight / 2);
    currDateStart = getCurrentDayStart( );
    currDateEnd = getCurrentDayEnd( );

    for (let iStage = 0; iStage < numberOfStages; iStage++) {      

        // get the current date & time - start of the timeline
        prevDate = currDateStart;
        stageCreated = false;
        newCol = null;
        stageData = con.stage[iStage];
        
        //create events for this stage
        for (let iEvent = 0; iEvent < aDataEvents.length; iEvent++) {
                       
            if ( aDataEvents[iEvent].stage !== stageData.id ) {
                // skip if it is another stage
                continue;
            };
            
            if ( aDataEvents[iEvent].start < currDateStart || aDataEvents[iEvent].end > currDateEnd ) {
                // skip events that are outside the timeline
                continue;
            };

            // create column for a stage
            if ( stageCreated == false ) {
                newCol = document.createElement("div");
                createNewCol(newCol, iStage);
                stageCreated = true;
            };

            // create single event
            newEvent = createNewEvent(aDataEvents[iEvent], prevDate);  
            newEvent.setAttribute("onclick", "onClickEvent(" + aDataEvents[iEvent].id + ")");

            newCol.appendChild(newEvent);
            prevDate = aDataEvents[iEvent].end;

        }

        if ( newCol !== null ) {
            parrentDiv.appendChild(newCol);
        };
    }

};

function createNewCol(newCol, stageNumber) {

    let newEventDummy;              // top dummy header for allignment issues

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

    let eventBlock;
    let textBlock;
    let topLineBlock;
    let favBlock;
    let favText;
    let titleBlock;
    let titleText;
    let descrBlock;
    let descrText;
    let eventSubBlock;
    let headerBlock;
    let footerBlock;
    
    let eventDimensions;
    let stage;

    let nowDate = new Date();

    eventDimensions = getEventDimensions(eventData.start, eventData.end, prevDate);
    stage = con.stage.find(o => o.id == eventData.stage );

    // event block
    eventBlock = document.createElement("div");
    eventBlock.setAttribute("style", "height:" + eventDimensions.eventHeight + "rem; margin-top:" + eventDimensions.eventMarginTop + "rem");
    eventBlock.setAttribute("id", "divEvent_" + eventData.id);
    eventBlock.classList.add("divEvent");
    eventBlock.classList.add(stage.style);
    if (detectDeviceType() === 'Mobile') {
        eventBlock.classList.add("mobile");
    } else {
        eventBlock.classList.add("desktop");
    };
    // highlight the event that is running right now
    if ( ( nowDate >= eventData.start ) && ( nowDate <= eventData.end ) ) {
        eventBlock.classList.add("eventHighlighted");
    }
    
    // event sub-block
    eventSubBlock = document.createElement("div");
    eventSubBlock.classList.add("divEventSubBlock");
    eventSubBlock.classList.add(stage.style);
    if (detectDeviceType() === 'Mobile') {
        eventSubBlock.classList.add("mobile");
    } else {
        eventSubBlock.classList.add("desktop");
    };

    // footer
    headerBlock = document.createElement("div");
    headerBlock.classList.add("divEventHeader");
    headerBlock.classList.add(stage.style);

    // footer
    footerBlock = document.createElement("div");
    footerBlock.classList.add("divEventFooter");
    footerBlock.classList.add(stage.style);

    // text block
    textBlock = document.createElement("div");
    textBlock.classList.add("divEventContent");

    // top line (fav icon + title)
    topLineBlock = document.createElement("div");
    topLineBlock.classList.add("divEventTopLine");

    // favorite icon
    favBlock = document.createElement("div");
    favBlock.classList.add("divEventFav");
    favText = document.createElement("span");
    favText.classList.add("spanEventFav");
    favText.setAttribute("id", "divEventFav_" + eventData.id);
    favText.classList.add("material-icons");
    if (eventData.favorite == true) {
        favText.textContent = "favorite";
    } else {
        favText.textContent = "";
    }
    favBlock.appendChild(favText);

    // title
    titleBlock = document.createElement("div");
    titleBlock.classList.add("divEventTitle");
    titleText = document.createElement("span");
    titleText.classList.add("spanEventTitle");
    titleText.textContent = eventData.band;
    titleBlock.appendChild(titleText);

    // description
    descrBlock = document.createElement("div");
    descrBlock.classList.add("divEventDescr");
    descrText = document.createElement("span");
    descrText.classList.add("spanEventDescr");
    if(eventData.genre) {
        descrText.textContent = eventData.shortDescription + "\n" + eventData.genre;
    } else {
        descrText.textContent = eventData.shortDescription;
    }
    descrBlock.appendChild(descrText);

    topLineBlock.appendChild(favBlock);
    topLineBlock.appendChild(titleBlock);
    textBlock.appendChild(topLineBlock);
    textBlock.appendChild(descrBlock);
    
    eventSubBlock.appendChild(textBlock);
    eventBlock.appendChild(headerBlock);
    eventBlock.appendChild(eventSubBlock);
    eventBlock.appendChild(footerBlock);

    return eventBlock;
};

function setDeviceProperties() {

    let deviceProp;

    if (detectDeviceType() === 'Mobile') {
        deviceProp = con.format.find(o => o.device === "mobile" );
    } else {
        deviceProp = con.format.find(o => o.device === "desktop" );
    };

    cust.sizeCust = deviceProp.sizeCust;
    setCssProp("--font-size", deviceProp.fontSize);
    setCssProp("--font-size-title", deviceProp.fontSizeTitle); 
    setCssProp("--font-size-popup", deviceProp.fontSizePopup);
    setCssProp("--font-size-popup-descr", deviceProp.fontSizePopupDescr);
    setCssProp("--font-size-spotify-logo", deviceProp.fontSizeSpotifyLogo);
    setCssProp("--title-height", deviceProp.titleHeight); 
    setCssProp("--hoursLabelWidth", deviceProp.hoursLabelWidth);
    setCssProp("--popup-button-font-size", deviceProp.popupButtonFontSize);
    setCssProp("--hour-label-padding-right", deviceProp.hourLabelPaddingRight);

};

function setCssProp(name, value) {
    window.addEventListener('DOMContentLoaded', function() { document.documentElement.style.setProperty(name, value); })
};

function scrollToHour(initialRun) {

    let nowDate = new Date();
    let currDateStart;
    let currDateEnd;

    currDateStart = getCurrentDayStart( );
    currDateEnd = getCurrentDayEnd( );

    if ( ( nowDate >= currDateStart ) && ( nowDate <= currDateEnd ) ) {
        document.getElementById("hourLabel_" + nowDate.getHours()).scrollIntoView({ behavior: "smooth", block: "center" });
    } else { 
        if (initialRun) {
            // if initial run of the app: scroll to the first event of the first day 
            document.getElementById("hourLabel_" + con.firstDayHourFocus).scrollIntoView({ behavior: "smooth", block: "center" });
        }
    };
};