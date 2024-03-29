sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/core/Fragment",
	"sap/ui/unified/library",
	"sap/m/MessageToast",
	"sap/m/Dialog",
	"sap/m/Button",
	"sap/m/Text",
	"Project/PD2023/Constants"
], function (Controller, Fragment, unifiedLibrary, MessageToast, Dialog, Button, Text, Constants) {
	"use strict";

	return Controller.extend("Project.PD2023.controller.Main", {
		onInit: function () {

			var oView = this.getView();
			var oUriModel = oView.getModel("uriModel");

			var sUriData;
			var sUriInfo;
			var sSheetId = Constants.sheetId;
			var sSheetRangeData = Constants.sheetRangeData;
			var sSheetRangeInfo = Constants.sheetRangeInfo;
			var sApiKey = Constants.apiKey;
			var sApiUri = Constants.apiUri;
			
			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar").setVisible(false);
			// this.byId("PC2-Header-NavToolbar").setVisible(false);

			this.getView().byId("PC1").setScaleFactor(2);
		
			sUriData = sApiUri + sSheetId + "/values/" + sSheetRangeData + "?key=" + sApiKey;
			sUriInfo = sApiUri + sSheetId + "/values/" + sSheetRangeInfo + "?key=" + sApiKey;

			oUriModel.setProperty("/uriData", sUriData);
			oUriModel.setProperty("/uriInfo", sUriInfo);

			this.prepareOutput();

			this.loadData (sUriData, this.prepareData, this, true);	// true = isAtStart (to defer start and refresh)
			this.loadData (sUriInfo, this.prepareInfo, this, true);
		},

		prepareOutput: function() {
			var oView = this.getView();
			var oModel = oView.getModel();

			var oStartFestDate;
			var oStartFestDate2;
			var oToday = new Date();

			var CalendarDayType = unifiedLibrary.CalendarDayType;
			var aStages = [];

			var aLocalData = [];
			var aOutputData = [];

			var sDay;

			// stages for the INFO/LEGEND
			aStages = [
				{
					text: Constants.stage.stageA,
					type: "Type09",
					legendType: CalendarDayType.Type09
				},
				{
					text: Constants.stage.stageB,
					type: "Type10",
					legendType: CalendarDayType.Type10
				}
			]

			// set higlighted date (if not during festival, the first day will be higlighted)
			if ( oToday.getMonth() === Constants.date.day4.month && oToday.getDate() === Constants.date.day4.day ) {
				sDay = 4;
			} else {
				if ( oToday.getMonth() === Constants.date.day3.month && oToday.getDate() === Constants.date.day3.day ) {
					sDay = 3;
				} else {
					if ( oToday.getMonth() === Constants.date.day2.month && oToday.getDate() === Constants.date.day2.day ) {
						sDay = 2;
					} else {
						sDay = 1;
					}
				}
			}

			oStartFestDate = Constants.date["day" + sDay].part1;
			oStartFestDate2 = Constants.date["day" + sDay].part2;
			this.setDefaultButtons(this);
			this.byId("day0" + sDay).setType("Emphasized");


			// set bottom text
			this.byId("bottomText").setProperty("text", Constants.bottomText[sDay]);

			// get local storage data if exists
			aLocalData = JSON.parse(localStorage.getItem("pd2023_data"));
			if (aLocalData) {
				for ( var i = 0; i < aLocalData.length; i++ ) {
					var dStartDate = new Date(aLocalData[i].start);
					var dEndDate = new Date(aLocalData[i].end);
					aOutputData.push({
						"band": aLocalData[i].band,
						"start": dStartDate,
						"end": dEndDate,
						"stage": aLocalData[i].stage,
						"shortDescription": aLocalData[i].shortDescription,
						"description": aLocalData[i].description,
						"type": aLocalData[i].type,
						"spotUrl": aLocalData[i].spotUrl,
						"favorite": aLocalData[i].favorite
					});
				}
			}

			oModel.setData({
				"startDate": oStartFestDate,
				"startDate2": oStartFestDate2,
				"stages": aStages,
				"events": aOutputData
			});
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
			var aLocalData = [];
			var aStages = [];
			
			aStages = oModel.getProperty("/stages");

			for ( var i = 0; i < iSourceLength; i++ ) {
				if (aSourceData[i]) { 									// skip if empty row
					var sStage;
					var sType;

					switch ( aSourceData[i][4] ) {
						case "A":
							sStage = aStages[0].text;
							sType = aStages[0].type;
							break;
						case "B":
							sStage = aStages[1].text;
							sType = aStages[1].type;
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
						"spotUrl": sSpotUrl,
						"favorite": ""
					});							
				}
			}



			// get favorites
			aLocalData = JSON.parse(localStorage.getItem("pd2023_data"));
			if (aLocalData) {
				for (var i = 0; i < aOutputData.length; i++) {
					if (aLocalData[i]) {
						aOutputData[i].favorite = aLocalData[i].favorite;
					}
				}
			} 

			// save local storage data
			localStorage.setItem("pd2023_data", JSON.stringify(aOutputData));
			oModel.setProperty("/events", aOutputData);

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
						sInfo = sInfo + "\n" + "\n" + " ********************* " + "\n" + "\n" + aSourceData[i][0] + "\n" + "\n" + aSourceData[i][1]
					} else {
						sInfo = sInfo + "\n" + "\n" + " ********************* " + "\n" + "\n" + aSourceData[i][1]
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
				var sFavorite;

				for (var i = 0; i < oData.length; i++ ) {
					// if ( oData[i].band === sBand && oData[i].start === dStartDate) {		// unknown issue: oData[i].start NOT EQUAL dStartDate after "onRefresh"
					if ( oData[i].band === sBand ) {
						sDescr = oData[i].description;
						sSpotUrl = oData[i].spotUrl;
						sFavorite = oData[i].favorite;
					}
				}

				sTitle = oAppointment.getTitle();
				this.openPopup (sTitle, sDescr, sSpotUrl, sFavorite);
			}
		},

		openPopup: function (sTitle, sDescr, sSpotUrl, sFavorite) {
			if (!this.oDialog) {
				var favorIcon;
				var aButtons = [];
				if (sFavorite) {
					favorIcon = Constants.iconFavorOn;
				} else {
					favorIcon = Constants.iconFavorOff;
				}
				aButtons.push( 
					new Button({
						id: "favorIcon",
						icon: favorIcon,
						press: function () {
							this.handleFavoritePress(sTitle);
						}.bind(this)
					})
				);
				if (sSpotUrl) {
					aButtons.push( 
						new Button({
							icon: Constants.iconSpotify,
							press: function () {
								window.open(sSpotUrl);
							}.bind(this)
						})
					);
				}
				aButtons.push( 
					new Button({
						// text: "Ok",
						icon: Constants.iconClose,
						press: function () {
							this.oDialog.close();
							this.oDialog.destroy();    
							this.oDialog = undefined;
						}.bind(this)
					})
				);

				this.oDialog = new Dialog({
					title: sTitle,
					content: new sap.m.VBox({
						items: [new Text({ text: sDescr })]
					}), 
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
			var sDay;
			
			this.setDefaultButtons(this);

			sDay = sId.charAt(sId.length - 1);

			oModel.setProperty("/startDate", Constants.date["day" + sDay].part1);
			oModel.setProperty("/startDate2", Constants.date["day" + sDay].part2);

			// highlight the DAY button
			this.byId(sId).setType("Emphasized");

			// set bottom text
			this.byId("bottomText").setProperty("text", Constants.bottomText[sDay]);
		},

		handleOpenLegend: function (oEvent) {
			var oSource = oEvent.getSource();

			if (!this._oLegendPopover) {
				Fragment.load({
					id: "LegendFrag",
					name: "Project.PD2023.view.fragment.Legend",
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
			var sTitle = "Informácie pre návštevníkov";
			var sLink = new sap.m.Link({
				text: "Predpredaj lístkov a eshop tu",	
				press: [this.handleTicketsLinkPress, this]
			});

			this.oDialog = new Dialog({
				title: sTitle,
				content: new sap.m.VBox({
					items: [new Text(), sLink, new Text({ text: sInfo })]
				}), 
				
				type: sap.m.DialogType.Message,
				buttons: new Button({
					icon: Constants.iconClose,
					press: function () {
						this.oDialog.close();
						this.oDialog.destroy();    
						this.oDialog = undefined;
					}.bind(this)
				})
			});
			this.oDialog.open();
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
			var sLink = Constants.spotifyLink;
			var myWindow = window.open(sLink);
		},

		handleTicketsLinkPress: function () {
			var sLink = Constants.ticketsLink;
			window.open(sLink);
		},

		handleFavoritePress: function (band) {
			var oView = this.getView();
			var oModel = oView.getModel();
			var aData = [];
			var oButton = sap.ui.getCore().byId("favorIcon");

			aData = oModel.getProperty("/events");

			for (var i = 0; i < aData.length; i++) {
				if (aData[i].band === band) {
					if (aData[i].favorite) {
						aData[i].favorite = "";
						oButton.setIcon(Constants.iconFavorOff);
					} else {
						aData[i].favorite = Constants.iconFavorOn;
						oButton.setIcon(Constants.iconFavorOn);
					}
				}
			}

			oModel.setProperty("data", aData);
			oModel.refresh();
			localStorage.setItem("pd2023_data", JSON.stringify(aData));

		},

		setDefaultButtons: function (that) {
			that.byId("day01").setType("Default");
			that.byId("day02").setType("Default");
			that.byId("day03").setType("Default");
			that.byId("day04").setType("Default");
		}

	});
});