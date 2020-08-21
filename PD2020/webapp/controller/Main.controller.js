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

			// this.byId("PC0-Header").setVisible(false);
			// this.byId("PC1-Header").setVisible(false);
			// this.byId("PC2-Header").setVisible(false);
			// this.byId("PC3-Header").setVisible(false);

			// this.byId("PC1-Header-Title").setVisible(false);
			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar").setVisible(false);
			// this.byId("PC1-Header-NavToolbar-TodayBtn").setVisible(false);
			// this.byId("PC1-Header-NavToolbar-PickerBtn").setVisible(false);
			
			
			
			// $("div.sapMSinglePCRowHeaders").eq(2).remove();
			// $("div.sapMSinglePCRowHeaders").eq(1).hide();

			

		},

		afterDataLoaded: function () {
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sourceDataModel");
			var aData = [];
			var aDataEvents = [];
			var oStartFestDate = new Date("2020", "7", "27", "12", "00");
			var oToday = new Date();
			// var oEndFestDate = new Date("2020", "7", "31", "00", "00");
			
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
					var oStartDate = this.formatDate( aData[j][0] );
					var oEndDate = this.formatDate( aData[j][1] );
					var sShortDescr = this.formatShortDescr( aData[j][5], oStartDate, oEndDate ); 
					var sDescr = this.formatDescr( sShortDescr, aData[j][4]	);

					switch ( aData[j][5] ) {
						case "Hlavný stage":
							aDataEvents.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"shortDescription": sShortDescr,
								"description": sDescr,
								"type": "Type09"
							});		
							break;
						case "Akustický stage":
							aDataEvents.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"shortDescription": sShortDescr,
								"description": sDescr,
								"type": "Type10"
							});		
							break;
						case "Kinečko":
							aDataEvents.push({
								"band": aData[j][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aData[j][5],
								"shortDescription": sShortDescr,
								"description": sDescr,
								"type": "Type01"
							});		
							break;
						default:
					}
				}
			}
			
			
			if ( oToday.getMonth() === 7 && oToday.getDate() === 30 ) {
				oStartFestDate = new Date("2020", "7", "30", "12", "00");
				this.byId("day04").setType("Emphasized");
			} else {
				if ( oToday.getMonth() === 7 && oToday.getDate() === 29 ) {
					oStartFestDate = new Date("2020", "7", "29", "12", "00");
					this.byId("day03").setType("Emphasized");
				} else {
					if ( oToday.getMonth() === 7 && oToday.getDate() === 28 ) {
						oStartFestDate = new Date("2020", "7", "28", "12", "00");
						this.byId("day02").setType("Emphasized");
					} else {
						oStartFestDate = new Date("2020", "7", "27", "12", "00");
						this.byId("day01").setType("Emphasized");
					}
				}
			}

			// console.log(oToday); // eslint-disable-line no-console

			oModel.setData({
				"startDate": oStartFestDate,
				"events": aDataEvents
			});

			
		
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

		formatShortDescr: function ( sStage, dStartDate, dEndDate ) {
			var sStartTime;
			var sEndTime; 
			
			if (dStartDate.getMinutes() < 10) {
				sStartTime = dStartDate.getHours() + ":" + "0" + dStartDate.getMinutes();
			} else {
				sStartTime = dStartDate.getHours() + ":" + dStartDate.getMinutes();
			}
			
			if (dEndDate.getMinutes() < 10) {
				sEndTime = dEndDate.getHours() + ":" + "0" + dEndDate.getMinutes();
			} else {
				sEndTime = dEndDate.getHours() + ":" + dEndDate.getMinutes();
			}

			return sStartTime + "-" + sEndTime + ", " + sStage;
		},
		
		formatDescr: function (sShortDescr, sDescr) {

		    var tmp = document.createElement("div"); // eslint-disable-line sap-no-element-creation
		    tmp.innerHTML = sDescr;
			
			var sText = tmp.textContent || tmp.innerText;
			
			
			
			return sShortDescr + "\n" + "\n" + sText;
		},
		
		handleAppointmentSelect: function (oEvent) {
			var oView = this.getView();
			var oAppointment = oEvent.getParameter("appointment");
			// console.log(oAppointment); // eslint-disable-line no-console

			if (oAppointment) {
				oAppointment.setSelected(false);
				var sBand = oEvent.getParameter("appointment").getProperty("title");
				var dStartDate = oEvent.getParameter("appointment").getProperty("startDate");
				var oData = oView.getModel().getData().events;
				var sDescr;

				for (var i = 0; i < oData.length; i++ ) {
					if ( oData[i].band === sBand && oData[i].start === dStartDate) {
						sDescr = oData[i].description;
					}
				}

				// console.log( oAppointment  ); // eslint-disable-line no-console
				// console.log( oData  ); // eslint-disable-line no-console

				MessageBox.show(sDescr, {
					title: oAppointment.getTitle()
				});
			}
		},
		
		onDayPress: function (oEvent) {
			var sId = oEvent.getSource().getId().slice(-5);
			var oModel = this.getOwnerComponent().getModel();
			
			// this.byId("day00").setType("Default");
			this.byId("day01").setType("Default");
			this.byId("day02").setType("Default");
			this.byId("day03").setType("Default");
			this.byId("day04").setType("Default");
	
			switch (sId) {
				// case "day00":
				// 	oModel.setProperty("/startDate", new Date());
				// 	break;
				case "day01":
					oModel.setProperty("/startDate", new Date("2020", "7", "27", "12", "00"));
					break;
				case "day02":
					oModel.setProperty("/startDate", new Date("2020", "7", "28", "9", "00"));
					break;
				case "day03":
					oModel.setProperty("/startDate", new Date("2020", "7", "29", "9", "00"));
					break;
				case "day04":
					oModel.setProperty("/startDate", new Date("2020", "7", "30", "9", "00"));
					break;
			}
			
			this.byId(sId).setType("Emphasized");
		}




	});
});