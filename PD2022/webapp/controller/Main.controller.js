sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/Fragment",
	"sap/ui/unified/library",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"sap/m/Dialog",
	"sap/m/Button",
	"sap/m/Text"
], function (Controller, JSONModel, Fragment, unifiedLibrary, MessageBox, MessageToast, Dialog, Button, Text) {
	"use strict";

	return Controller.extend("Project.PD2022.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oUriModel = oView.getModel("uriModel");
			
			var sUriData;
			var sUriInfo;

			// *** update this part if spreadsheet url/range changed ***
			var sSheetId = "1O9khRTDa9-hkyBLW_ZNEKgHbaaWqK5uZ0GYbkr1G0o8";
			var sSheetRangeData = "'PD2022'!A2:F100";
			var sSheetRangeInfo = "'PD2022_info'!A1:B100";
			var sApiKey = "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI";
			
			
			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar").setVisible(false);
			this.byId("PC2-Header-NavToolbar").setVisible(false);
		
			sUriData = "https://sheets.googleapis.com/v4/spreadsheets/" + sSheetId + "/values/" + sSheetRangeData + "?key=" + sApiKey;
			sUriInfo = "https://sheets.googleapis.com/v4/spreadsheets/" + sSheetId + "/values/" + sSheetRangeInfo + "?key=" + sApiKey;

			oUriModel.setProperty("/uriData", sUriData);
			oUriModel.setProperty("/uriInfo", sUriInfo);

			this.loadData (sUriData, this.prepareData, this, true);	// true = isAtStart (to defer start and refresh)
			this.loadData (sUriInfo, this.prepareInfo, this, true);

		},

		loadData: function (theUrl, callback, that, isAtStart) {
			var xmlHttp = new XMLHttpRequest();
			xmlHttp.onreadystatechange = function() { 
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
					callback(xmlHttp.responseText, that, isAtStart);
			}
			xmlHttp.open("GET", theUrl, true); // true for asynchronous 
			xmlHttp.send(null);
		},

		prepareData: function (response, that, isAtStart) {
			var oView = that.getView();
			var oModel = oView.getModel();

			var obj = JSON.parse( response );
			var aSourceData = obj.values;
			var iSourceLength = aSourceData.length;
			var aOutputData = [];

			var oStartFestDate;
			var oStartFestDate2;
			var oToday = new Date();

			var CalendarDayType = unifiedLibrary.CalendarDayType;
			var aStages = [];

			// stages for the INFO/LEGEND
			aStages = [
				{
					text: "Hlavný stage",
					type: CalendarDayType.Type09
				}
				,
				{
					text: "B scéna",
					type: CalendarDayType.Type10
				}
			]

			for ( var i = 0; i < iSourceLength; i++ ) {
				if (aSourceData[i]) { 									// skip if empty row
					var sStage;
					var sType;

					switch ( aSourceData[i][4] ) {
						case "A":
							sStage = aStages[0].text;
							sType = "Type09";
							break;
						case "B":
							sStage = aStages[1].text;
							sType = "Type10";	
							break;
						default:
					}
					
					var sBand = aSourceData[i][2];
					var oStartDate = that.formatDate( aSourceData[i][0] );
					var oEndDate = that.formatDate( aSourceData[i][1] );
					var sShortDescr = that.formatShortDescr( sStage, oStartDate, oEndDate ); 
					var sDescr = that.formatDescr( sShortDescr, aSourceData[i][3] );
					var sSpotUrl = aSourceData[i][5];

					aOutputData.push({
						"band": sBand,
						"start": oStartDate,
						"end": oEndDate,
						"stage": sStage,
						"shortDescription": sShortDescr,
						"description": sDescr,
						"type": sType,
						"spotUrl": sSpotUrl
					});							
				}
			}
			
			// set higlighted date (if not during festival, the first day will be higlighted)
			that.byId("day01").setType("Default");
			that.byId("day02").setType("Default");
			that.byId("day03").setType("Default");
			that.byId("day04").setType("Default");
			if ( oToday.getMonth() === 7 && oToday.getDate() === 28 ) {
				oStartFestDate = new Date("2022", "7", "27", "12", "00");
				oStartFestDate2 = new Date("2022", "7", "28", "00", "00");
				that.byId("day04").setType("Emphasized");
			} else {
				if ( oToday.getMonth() === 7 && oToday.getDate() === 27 ) {
					oStartFestDate = new Date("2022", "7", "26", "12", "00");
					oStartFestDate2 = new Date("2022", "7", "27", "00", "00");
					that.byId("day03").setType("Emphasized");
				} else {
					if ( oToday.getMonth() === 7 && oToday.getDate() === 26 ) {
						oStartFestDate = new Date("2022", "7", "25", "12", "00");
						oStartFestDate2 = new Date("2022", "7", "26", "00", "00");
						that.byId("day02").setType("Emphasized");
					} else {
						oStartFestDate = new Date("2022", "7", "24", "12", "00");
						oStartFestDate2 = new Date("2022", "7", "25", "00", "00");
						that.byId("day01").setType("Emphasized");
					}
				}
			}

			oModel.setData({
				"startDate": oStartFestDate,
				"startDate2": oStartFestDate2,
				"stages": aStages,
				"events": aOutputData
			});

			if (!isAtStart) {
				MessageToast.show("Program aktualizovaný", {
					duration: 1000,
					width: "15em",
					animationDuration: 500
				});
			}
		},

		prepareInfo: function (response, that, isAtStart) {
			var oView = that.getView();
			var oInfoModel = oView.getModel("infoModel");

			var obj = JSON.parse( response );
			var aSourceData = obj.values;
			var iSourceLength = aSourceData.length;

			var sInfo = "";

			for (var i = 0; i < iSourceLength; i++) {
				if ( aSourceData[i] ) {
					if ( aSourceData[i][0] ) {
						sInfo = sInfo + aSourceData[i][0] + "\n" + "\n" + aSourceData[i][1] + "\n" + "\n" + " ********************* " + "\n" + "\n"
					} else {
						sInfo = sInfo + aSourceData[i][1] + "\n" + "\n" + " ********************* " + "\n" + "\n"
					}
					
				}
			}

			oInfoModel.setProperty("/info", sInfo);
		},

		formatDate: function (sDate) {
			var sYear, sMonth, sDay, sHour, sMinute;
			
			sYear = sDate.slice(6, 10);
			sMonth = parseInt(sDate.slice(3, 5)) - 1;
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

		    // var tmp = document.createElement("div"); // eslint-disable-line sap-no-element-creation
		    // tmp.innerHTML = sDescr;
			// var sText = tmp.textContent || tmp.innerText;
			
			// return sShortDescr + "\n" + "\n" + sText;

			return sShortDescr + "\n" + "\n" + sDescr;
		},
		
		handleAppointmentSelect: function (oEvent) {
			var oView = this.getView();
			var oAppointment = oEvent.getParameter("appointment");

			if (oAppointment) {
				oAppointment.setSelected(false);
				var sBand = oEvent.getParameter("appointment").getProperty("title");
				// var dStartDate = oEvent.getParameter("appointment").getProperty("startDate");
				var oData = oView.getModel().getData().events;
				var sTitle;
				var sDescr;
				var sSpotUrl;

				for (var i = 0; i < oData.length; i++ ) {
					// if ( oData[i].band === sBand && oData[i].start === dStartDate) {		// unknown issue: oData[i].start NOT EQUAL dStartDate after "onRefresh"
					if ( oData[i].band === sBand ) {
						sDescr = oData[i].description;
						sSpotUrl = oData[i].spotUrl;
					}
				}

				sTitle = oAppointment.getTitle();
				this.openPopup (sTitle, sDescr, sSpotUrl);

				// MessageBox.show(sDescr, {
				// 	title: sTitle,
				// });
			}
		},

		openPopup: function (sTitle, sDescr, sSpotUrl) {
			if (!this.oDialog) {
				var aButtons = [];
				if (sSpotUrl) {
					aButtons.push( 
						new Button({
							text: "Spotify",
							icon: "sap-icon://media-play",
							press: function () {
								window.open(sSpotUrl);
							}.bind(this)
						})
					);
				}
				aButtons.push( 
					new Button({
						text: "Ok",
						press: function () {
							this.oDialog.close();
							this.oDialog.destroy();    
							this.oDialog = undefined;
						}.bind(this)
					})
				);

				this.oDialog = new Dialog({
					title: sTitle,
					content: new Text({ text: sDescr }),
					type: sap.m.DialogType.Message,
					buttons: aButtons
				});

				// to get access to the controller's model
				this.getView().addDependent(this.oDialog);
			}

			this.oDialog.open();
		},
		
		onDayPress: function (oEvent) {
			var sId = oEvent.getSource().getId().slice(-5);
			var oModel = this.getOwnerComponent().getModel();
			
			this.byId("day01").setType("Default");
			this.byId("day02").setType("Default");
			this.byId("day03").setType("Default");
			this.byId("day04").setType("Default");
	
			switch (sId) {
				case "day01":
					oModel.setProperty("/startDate", new Date("2022", "7", "24", "12", "00"));
					oModel.setProperty("/startDate2", new Date("2022", "7", "25", "00", "00"));
					break;
				case "day02":
					oModel.setProperty("/startDate", new Date("2022", "7", "25", "12", "00"));
					oModel.setProperty("/startDate2", new Date("2022", "7", "26", "00", "00"));
					break;
				case "day03":
					oModel.setProperty("/startDate", new Date("2022", "7", "26", "12", "00"));
					oModel.setProperty("/startDate2", new Date("2022", "7", "27", "00", "00"));
					break;
				case "day04":
					oModel.setProperty("/startDate", new Date("2022", "7", "27", "12", "00"));
					oModel.setProperty("/startDate2", new Date("2022", "7", "28", "00", "00"));
					break;
			}
			
			this.byId(sId).setType("Emphasized");
		},

		handleOpenLegend: function (oEvent) {
			var oSource = oEvent.getSource();

			if (!this._oLegendPopover) {
				Fragment.load({
					id: "LegendFrag",
					name: "Project.PD2022.view.fragment.Legend",
					controller: this
				}).then(function(oPopoverContent){
					this._oLegendPopover = oPopoverContent;
					this.getView().addDependent(this._oLegendPopover);
					this._oLegendPopover.openBy(oSource);
				}.bind(this));
			} else if (this._oLegendPopover.isOpen()) {
				this._oLegendPopover.close();
			} else {
				this._oLegendPopover.openBy(oSource);
			}
		},

		onInfoPress: function () {
			var oView = this.getView();
			var oInfoModel = oView.getModel("infoModel");
			var sInfo = oInfoModel.getProperty("/info");
					
			MessageBox.show(sInfo, {title: "Informácie pre návštevníkov"});
		},

		onRefresh: function () {
			var oView = this.getView();
			var oUriModel = oView.getModel("uriModel");
			var sUriData = oUriModel.getProperty("/uriData");
			var sUriInfo = oUriModel.getProperty("/uriInfo");

			this.loadData (sUriData, this.prepareData, this, false);
			this.loadData (sUriInfo, this.prepareInfo, this, false);
		},

		onSpotifyPress: function () {
			var myWindow = window.open("https://open.spotify.com/playlist/2AfkiP1vIJoeYqGkceDf39");
		}

	});
});