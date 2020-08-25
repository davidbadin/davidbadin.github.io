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
		},

		onCalculate: function () {
			
			var oView = this.getView();
			var oSettings = oView.getModel("settings");
			var iNumberOfPlayers = oSettings.getProperty("/numberOfPlayers");

			for (var i = 1; i <= iNumberOfPlayers; i++) {
				this.colCalc(i);
			}
			if ( iNumberOfPlayers === "1" ) {
				this.colCalc(0);
			}
		},

		colCalc: function (iCol) {
			
			var oView = this.getView();
			var iScore = 0;

			// Completed king's orders
			if ( oView.byId("vp4Box0" + iCol).getSelected() ) {
				iScore = iScore + 4;
			}
			if ( oView.byId("vp6Box0" + iCol).getSelected() ) {
				iScore = iScore + 6;
			}
			if ( oView.byId("vp8Box0" + iCol).getSelected() ) {
				iScore = iScore + 8;
			}

			iScore = iScore 
			// Attribute tracks
				+ parseInt(oView.byId("dpdStrength0" + iCol).getValue())
				+ parseInt(oView.byId("dpdFaith0" + iCol).getValue())
				+ parseInt(oView.byId("dpdInfluence0" + iCol).getValue())
			// VP for actions
				+ parseInt(oView.byId("dpdDevelop0" + iCol).getValue())
				+ parseInt(oView.byId("dpdFortify0" + iCol).getValue())
				+ parseInt(oView.byId("dpdCommission0" + iCol).getValue())
				+ parseInt(oView.byId("dpdGarrison0" + iCol).getValue())
				+ parseInt(oView.byId("dpdAbsolve0" + iCol).getValue());
			
			// VP on walls
			if ( oView.byId("inputFortifyExtra0" + iCol).getValue() ) {
				iScore = iScore + parseInt(oView.byId("inputFortifyExtra0" + iCol).getValue())
			}

			// Converts
			if ( oView.byId("inputConvert0" + iCol).getValue() ) {
				iScore = iScore + parseInt(oView.byId("inputConvert0" + iCol).getValue())
			}

			// Unpaid debts
			if ( oView.byId("inputDebtUnpaid0" + iCol).getValue() ) {
				iScore = iScore + parseInt(oView.byId("inputDebtUnpaid0" + iCol).getValue())
			}

			// Paid debts
			if ( oView.byId("inputDebtPaid0" + iCol).getValue() ) {
				iScore = iScore + parseInt(oView.byId("inputDebtPaid0" + iCol).getValue())
			}

			// Silver and provision
			if ( oView.byId("inputSilver0" + iCol).getValue() ) {
				iScore = iScore + parseInt(oView.byId("inputSilver0" + iCol).getValue())
			}
			
			oView.byId("textScore0" + iCol).setText(iScore);
		},

		onReset: function () {
			
			var oView = this.getView();

			for (var iCol = 0; iCol < 5; iCol++) {
				
				oView.byId("vp4Box0" + iCol).setSelected(false);
				oView.byId("vp6Box0" + iCol).setSelected(false);
				oView.byId("vp8Box0" + iCol).setSelected(false);

				oView.byId("dpdStrength0" + iCol).setSelectedKey("0");
				oView.byId("dpdFaith0" + iCol).setSelectedKey("0");
				oView.byId("dpdInfluence0" + iCol).setSelectedKey("0");

				oView.byId("dpdDevelop0" + iCol).setSelectedKey("0");
				oView.byId("dpdFortify0" + iCol).setSelectedKey("0");
				oView.byId("dpdCommission0" + iCol).setSelectedKey("0");
				oView.byId("dpdGarrison0" + iCol).setSelectedKey("0");
				oView.byId("dpdAbsolve0" + iCol).setSelectedKey("0");
				
				oView.byId("inputFortifyExtra0" + iCol).setValue("");
				oView.byId("inputConvert0" + iCol).setValue("");

				oView.byId("inputDebtUnpaid0" + iCol).setValue("");
				oView.byId("inputDebtPaid0" + iCol).setValue("");

				oView.byId("inputSilver0" + iCol).setValue("");

				oView.byId("textScore0" + iCol).setText("0");
			}
		}

	});
});