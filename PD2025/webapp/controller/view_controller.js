
function createTopButtons() {

    let topButtonsDiv;
    let newButton;

    // top days buttons
    topButtonsDiv = document.getElementById("divTopDaysButtons");

    for (let i = 0; i < con.days.length; i++) {
        newButton = document.createElement("button");
        newButton.setAttribute("id", "buttonDay" + con.days[i].number );
        newButton.setAttribute("class", "buttonDay");

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
            document.getElementById("spanPopupFav").textContent = "favorite";
        } else {
            document.getElementById("spanPopupFav").textContent = "favorite_border";
        }
        // Spotify button
        if (eventData.spotUrl) {
            // document.getElementById("buttonPopupPlay").style.display = "flex";
            displayPlayer(eventData.spotUrl);
        // } else {
            // document.getElementById("buttonPopupPlay").style.display = "none";
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
                document.getElementById("spanPopupFav").textContent = "favorite_border";
                document.getElementById("divEventFav_" + eventId).textContent = "";
                aDataEvents[eventIndex].favorite = false;
            } else {
                // set favorite
                document.getElementById("spanPopupFav").textContent = "favorite";
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
    let url = "https://open.spotify.com/artist/" + aDataEvents[eventIndex].spotUrl;
    window.open(url);
};

function onClickClose() {
    destroyPlayer();
    const popDialog = document.getElementById("divPopupEvent");    
    popDialog.setAttribute("data-id", "0");
    popDialog.style.visibility = "hidden";
};

function onClickSpotifyPlaylist() {
    window.open(con.spotifyPlaylist);
};

function displayPlayer(url) {

    let iframePlayer = {};
    let urlSpotify;

    // destroy existing player (if exists)
    destroyPlayer();

    // create Spotify player
    iframePlayer = document.createElement("iframe");
    urlSpotify = "https://open.spotify.com/embed/artist/" + url + "?utm_source=generator&theme=0";
    iframePlayer.setAttribute("id", "iframePlayer");
    iframePlayer.setAttribute("src", urlSpotify);
    iframePlayer.setAttribute("frameBorder", "0");
	iframePlayer.setAttribute("allowfullscreen", ""); 
	iframePlayer.setAttribute("allow", "autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture");
	iframePlayer.setAttribute("loading", "lazy");
    iframePlayer.setAttribute("class", "iframePlayer");

    const divPlayer = document.getElementById("divPopupSpotifyPlayer");
    divPlayer.appendChild(iframePlayer);

};

function destroyPlayer() {
    const player = document.getElementById("iframePlayer");
    if(player) {
        player.remove();
    }
};