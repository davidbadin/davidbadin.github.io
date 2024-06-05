
function createTopButtons() {

    let topButtonsDiv;
    let newButton;

    topButtonsDiv = document.getElementById("divTopButtons");

    for (let i = 0; i < con.days.length; i++) {
        newButton = document.createElement("button");
        newButton.setAttribute("id", "buttonDay" + con.days[i].number );
        newButton.textContent = con.days[i].name;

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