sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageBox"
], function (Controller, JSONModel, MessageBox) {
	"use strict";

	return Controller.extend("Project.PD2020.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oSourceModel = oView.getModel("sourceDataModel");
			
			var sUri = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetSource.uri;
			
			oSourceModel.loadData(sUri);
			oSourceModel.attachRequestCompleted(this.afterDataLoaded, this);

			this.byId("PC1-Header-Title").setVisible(false);
			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar-TodayBtn").setVisible(false);
			this.byId("PC1-Header-NavToolbar-PickerBtn").setVisible(false);
			
		},
		
		afterDataLoaded: function () {
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sourceDataModel");
			var aData = [];
			var aDataMain = [];
			var aDataAcu = [];
			var aDataCine = [];
			var oStartFestDate = new Date("2020", "7", "27", "12", "00");
			var oEndFestDate = new Date("2020", "7", "31", "00", "00");
			
			// console.log(oSourceModel.getData().feed.entry[84].gs$cell); // eslint-disable-line no-console
			
			var oSourceData = oSourceModel.getData().feed.entry;
			var iSourceLength = oSourceModel.getProperty("/feed/entry/length");
			var iNumberOfRows = 0;							// number of data rows in spreadsheet without header, empty rows (if any) included
			
			for (var i = 0; i < iSourceLength; i++) {
				var iRow = oSourceData[i].gs$cell.row;				
				var iCol = oSourceData[i].gs$cell.col;				
				var sValue = oSourceData[i].gs$cell.$t;

				if (iRow !== "1" && iRow !== "2" && iRow !== "3" && iRow !== "4") { 							
					if (!aData[iRow - 5]) {
						aData[iRow - 5] = [];
					}
					aData[iRow - 5][iCol - 1] = sValue;
				}
				iNumberOfRows = iRow - 4;
			}
			
			for (var j = 0; j < iNumberOfRows; j++) {
				if (aData[j]) { 									// skip if empty row
					var oStartDate;
					var oEndDate;
					var sDescr;
					oStartDate = this.formatDate( aData[j][0] );
					oEndDate = this.formatDate( aData[j][1] );
					sDescr = this.formatDescr( aData[j][4]);
					switch ( aData[j][5] ) {
						case "Hlavný stage":
							aDataMain.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"description": sDescr,
								"type": "Type09"
							});		
							break;
						case "Akustický stage":
							aDataAcu.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"description": sDescr,
								"type": "Type10"
							});		
							break;
						case "Kinečko":
							aDataCine.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"description": sDescr,
								"type": "Type01"
							});		
							break;
						default:
					}
				}
			}
			
			
			if ( ( new Date() > oStartFestDate ) && ( new Date() < oEndFestDate ) ) {
				oStartFestDate = new Date();
			}

			oModel.setData({
				"startDate": oStartFestDate,
				"events": [
					{
						"stage": "Hlavný stage",
						"appointments": aDataMain
					},
					{
						"stage": "Akustický stage",
						"appointments": aDataAcu
					},
					{
						"stage": "Kinečko",
						"appointments": aDataCine
					}
				]
			});
			// console.log(oModel.getData()); // eslint-disable-line no-console
		
			oSourceModel.detachRequestCompleted(this.afterDataLoaded, this);
		},
		
		formatDate: function (sDate) {
			var sYear, sMonth, sDay, sHour, sMinute;
			
			sYear = sDate.slice(6, 10);

			switch (sDate.slice(3, 5)) {
				case "01":
					sMonth = "00";
					break;
				case "02":
					sMonth = "01";
					break;
				case "03":
					sMonth = "02";
					break;
				case "04":
					sMonth = "03";
					break;
				case "05":
					sMonth = "04";
					break;
				case "06":
					sMonth = "05";
					break;
				case "07":
					sMonth = "06";
					break;
				case "08":
					sMonth = "07";
					break;
				case "09":
					sMonth = "08";
					break;
				case "10":
					sMonth = "09";
					break;
				case "11":
					sMonth = "10";
					break;
				case "12":
					sMonth = "11";
			}

			sDay = sDate.slice(0, 2);
			
			if ( sDate.slice(12, 13) === ":" ) {
				sHour = "0" + sDate.slice(11, 12);	
				sMinute = sDate.slice(13, 15);
			} else {
				sHour = sDate.slice(11, 13);	
				sMinute = sDate.slice(14, 16);
			}

			var oDate = new Date(sYear, sMonth, sDay, sHour, sMinute);

			return oDate;
		},
		
		formatDescr: function (sDescr) {

		    var tmp = document.createElement("div"); // eslint-disable-line sap-no-element-creation
		    tmp.innerHTML = sDescr;
		    
		    return tmp.textContent || tmp.innerText;
		},
		
		handleAppointmentSelect: function (oEvent) {
				var oAppointment = oEvent.getParameter("appointment");
				
				oAppointment.setSelected(false);
				MessageBox.show(oAppointment.getText(), {
					title: oAppointment.getTitle()
				});
		},
		
		onDayPress: function (oEvent) {
			var sId = oEvent.getSource().getId().slice(-5);
			var oModel = this.getOwnerComponent().getModel();
			
			this.byId("day00").setType("Default");
			this.byId("day01").setType("Default");
			this.byId("day02").setType("Default");
			this.byId("day03").setType("Default");
	
			switch (sId) {
				case "day00":
					oModel.setProperty("/startDate", new Date());
					break;
				case "day01":
					oModel.setProperty("/startDate", new Date("2020", "7", "27", "12", "00"));
					break;
				case "day02":
					oModel.setProperty("/startDate", new Date("2020", "7", "28", "9", "00"));
					break;
				case "day03":
					oModel.setProperty("/startDate", new Date("2020", "7", "29", "9", "00"));
					break;
			}
			
			this.byId(sId).setType("Emphasized");
		}




	});
});