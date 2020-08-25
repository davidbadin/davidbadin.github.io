sap.ui.define([
	"sap/ui/core/mvc/Controller"
], function (Controller) {
	"use strict";

	return Controller.extend("Project.PaladinsScoresheet.controller.Main", {
		onInit: function () {

		},
		
		onSelectedPlayersNumber: function () {
			var oView = this.getView();
			var oSettings = oView.getModel("settings");
			var iNumberOfPlayers = oSettings.getProperty("/numberOfPlayers");
			
			console.log(iNumberOfPlayers); //eslint-disable-line no-console
			
			switch (iNumberOfPlayers) {
				case "1":
					oSettings.setProperty("/player2", false);
					oSettings.setProperty("/player3", false);
					oSettings.setProperty("/player4", false);
					oSettings.setProperty("/player0", true);
					break;
				case "2":
					oSettings.setProperty("/player2", true);
					oSettings.setProperty("/player3", false);
					oSettings.setProperty("/player4", false);
					oSettings.setProperty("/player0", false);
					break;
				case "3":
					oSettings.setProperty("/player2", true);
					oSettings.setProperty("/player3", true);
					oSettings.setProperty("/player4", false);
					oSettings.setProperty("/player0", false);
					break;
				case "4":
					oSettings.setProperty("/player2", true);
					oSettings.setProperty("/player3", true);
					oSettings.setProperty("/player4", true);
					oSettings.setProperty("/player0", false);
					break;
			}
			
		}
	});
});