
function createTopButtons() {

    let topButtonsDiv;
    let newButton;

    topButtonsDiv = document.getElementById("divTopButtons");

    for (let i = 0; i < con.days.length; i++) {
        newButton = document.createElement("button");
        newButton.setAttribute("id", "buttonDay" + con.days[i].number );

        textButton = document.createElement("span");
        textButton.classList.add("spanDayButton");
        textButton.textContent = con.days[i].name;
        newButton.appendChild(textButton);

        // set active button
        if (cust.currDay == con.days[i].number) {
            newButton.classList.add("buttonActive");
        }

        newButton.addEventListener("click", function onClickDay () {    
            onDayPress( con.days[i] );
        } );
        
        topButtonsDiv.appendChild(newButton);

    };
};

function onDayPress(day) {

    let button;

    // close popup first
    onClickClose();

    cust.currDay = day.number;

    // set buttons active / non-active
    for (let i = 0; i < con.days.length; i++) {
        
        button = {};
        button = document.getElementById("buttonDay" + con.days[i].number);
        if (button) {
            if (cust.currDay == con.days[i].number) {
                // active
                button.classList.add("buttonActive");
            } else {
                // non-active
                button.classList.remove("buttonActive");
            };
        };

    };

    // create elements
    onInit(false);

};

// POPUP controller:
function onClickEvent(eventId) { 
    
    console.log("onClick " + eventId);

    // get event data
    let eventData = aDataEvents.find(o => o.id == eventId );
    if (eventData) {
    
        const popDialog = document.getElementById("divPopupEvent");
        popDialog.style.visibility = "visible";
        
        // set custom attribute "ID"
        popDialog.setAttribute("data-id", eventId);
        
        // set popup texts & buttons
        document.getElementById("spanPopupEventTitle").textContent = eventData.band;
        document.getElementById("spanPopupEventSubtitle").textContent = eventData.shortDescription;
        document.getElementById("spanPopupEventBody").textContent = eventData.description;
        // favorite button
        if (eventData.favorite) {
            document.getElementById("buttonPopupFav").textContent = "favorite";
        } else {
            document.getElementById("buttonPopupFav").textContent = "favorite_border";
        }
        // Spotify button
        if (eventData.spotUrl) {
            document.getElementById("buttonPopupPlay").style.display = "flex";
        } else {
            document.getElementById("buttonPopupPlay").style.display = "none";
        }
    
    }

};

function onClickFav() {

    let eventId = document.getElementById("divPopupEvent").getAttribute("data-id");

    if (eventId) {

        let eventIndex = aDataEvents.findIndex( o => o.id == eventId );
        
        if (eventIndex >= 0 ) {
            if (aDataEvents[eventIndex].favorite) {
                // unset favorite
                document.getElementById("buttonPopupFav").textContent = "favorite_border";
                document.getElementById("divEventFav_" + eventId).textContent = "";
                aDataEvents[eventIndex].favorite = false;
            } else {
                // set favorite
                document.getElementById("buttonPopupFav").textContent = "favorite";
                document.getElementById("divEventFav_" + eventId).textContent = "favorite";
                aDataEvents[eventIndex].favorite = true;
            }

            // save data to local
            setLocalData(con.localStorageData, aDataEvents);
        }
    }
};

function onClickPlay() {
    let eventId = document.getElementById("divPopupEvent").getAttribute("data-id");
    let eventIndex = aDataEvents.findIndex( o => o.id == eventId );
    window.open(aDataEvents[eventIndex].spotUrl);
};

function onClickClose() {
    const popDialog = document.getElementById("divPopupEvent");    
    popDialog.setAttribute("data-id", "0");
    popDialog.style.visibility = "hidden";
};