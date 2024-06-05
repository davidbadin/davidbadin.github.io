
function createTopButtons() {

    let topButtonsDiv;
    let newButton;

    topButtonsDiv = document.getElementById("divTopButtons");

    for (let i = 0; i < con.days.length; i++) {
        newButton = document.createElement("button");
        newButton.setAttribute("class", "butDayButtons");
        newButton.setAttribute("id", "buttonDay" + (i + 1) );
        newButton.textContent = con.days[i].name;
        newButton.addEventListener("click", function onClickDay () {    
            onDayPress( con.days[i] );
        } );
        
        topButtonsDiv.appendChild(newButton);

    };
};

function onDayPress(day) {

    console.log("click " + day.number);  
    cust.currDay = day.number;

    onInit();

};