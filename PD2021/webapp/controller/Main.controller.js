sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/Fragment",
	"sap/ui/unified/library",
	"sap/m/MessageBox",
	"sap/m/MessageToast"
], function (Controller, JSONModel, Fragment, unifiedLibrary, MessageBox, MessageToast) {
	"use strict";

	return Controller.extend("Project.PD2021.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			
			var sUriData;
			var sUriInfo;

			var sSheetIdData = "1Sdueu-N5Hlj01NdYsoHB7dI1T5smjUH6WsPseBHaSTs";
			var sSheetRangeData = "A2:F100";
			var sSheetIdInfo = "";
			var sSheetRangeInfo = "A1:B100";
			var sApiKey = "AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI";
			
			sUriData = "https://sheets.googleapis.com/v4/spreadsheets/" + sSheetIdData + "/values/" + sSheetRangeData + "?key=" + sApiKey;
			sUriInfo = "https://sheets.googleapis.com/v4/spreadsheets/" + sSheetIdInfo + "/values/" + sSheetRangeInfo + "?key=" + sApiKey;

			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar").setVisible(false);
			this.byId("PC2-Header-NavToolbar").setVisible(false);
		
			// oSourceDataModel.attachRequestCompleted(this.afterDataLoaded, this);	
			// oSourceDataModel.loadData(sUriData);

			// oSourceInfoModel.attachRequestCompleted(this.afterInfoLoaded, this);	
			// oSourceInfoModel.loadData(sUriInfo);		

			// var sUriTest = "https://sheets.googleapis.com/v4/spreadsheets/1Sdueu-N5Hlj01NdYsoHB7dI1T5smjUH6WsPseBHaSTs/values/A5:F7?key=AIzaSyBIHleeVgn137sWxmlCGvFQjewrv-ueXMI";


			this.loadData (sUriData, this.prepareData, this);


		},

		loadData: function (theUrl, callback, that) {
			var xmlHttp = new XMLHttpRequest();
			xmlHttp.onreadystatechange = function() { 
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
					callback(xmlHttp.responseText, that);
			}
			xmlHttp.open("GET", theUrl, true); // true for asynchronous 
			xmlHttp.send(null);
		},

		prepareData: function (response, that) {
			console.log( response );

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
			
			console.log( aSourceData.length );

			for ( var i = 0; i < iSourceLength; i++ ) {
				if (aSourceData[i]) { 									// skip if empty row
					var oStartDate = that.formatDate( aSourceData[i][0] );
					var oEndDate = that.formatDate( aSourceData[i][1] );
					var sShortDescr = that.formatShortDescr( aSourceData[i][4], oStartDate, oEndDate ); 
					var sDescr = that.formatDescr( sShortDescr, aSourceData[i][3]	);

					switch ( aSourceData[i][4] ) {
						case "Hlavný stage":
							aOutputData.push({
								"band": aSourceData[i][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aSourceData[i][4],
								"shortDescription": sShortDescr,
								"description": sDescr,
								"type": "Type09"
							});		
							break;
						case "Curious Trenčín 2026 stage":
							aOutputData.push({
								"band": aSourceData[i][2],
								"start": oStartDate,
								"end": oEndDate,
								"stage": aSourceData[i][4],
								"shortDescription": sShortDescr,
								"description": sDescr,
								"type": "Type10"
							});		
							break;
						default:
					}
				}
			}
			
			if ( oToday.getMonth() === 7 && oToday.getDate() === 28 ) {
				oStartFestDate = new Date("2021", "7", "28", "9", "00");
				oStartFestDate2 = new Date("2021", "7", "29", "00", "00");
				that.byId("day04").setType("Emphasized");
			} else {
				if ( oToday.getMonth() === 7 && oToday.getDate() === 27 ) {
					oStartFestDate = new Date("2021", "7", "27", "9", "00");
					oStartFestDate2 = new Date("2021", "7", "28", "00", "00");
					that.byId("day03").setType("Emphasized");
				} else {
					if ( oToday.getMonth() === 7 && oToday.getDate() === 26 ) {
						oStartFestDate = new Date("2021", "7", "26", "9", "00");
						oStartFestDate2 = new Date("2021", "7", "27", "00", "00");
						that.byId("day02").setType("Emphasized");
					} else {
						oStartFestDate = new Date("2021", "7", "25", "12", "00");
						oStartFestDate2 = new Date("2021", "7", "26", "00", "00");
						that.byId("day01").setType("Emphasized");
					}
				}
			}

			aStages = [
				{
					text: "Hlavný stage",
					type: CalendarDayType.Type09
				},
				{
					text: "Curious Trenčín 2026 stage",
					type: CalendarDayType.Type10
				}
			]

			oModel.setData({
				"startDate": oStartFestDate,
				"startDate2": oStartFestDate2,
				"stages": aStages,
				"events": aOutputData
			});


		},

		afterDataLoaded: function () {
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sourceDataModel");
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			
			var CalendarDayType = unifiedLibrary.CalendarDayType;
			var aStages = [];

			var oStartFestDate;
			var oStartFestDate2;
			var oToday = new Date();

			var aData = [];
			var aDataEvents = [];

			var oSourceData;
			var iSourceLength;
			var iNumberOfRows = 0;							// number of data rows in spreadsheet without header, empty rows (if any) included


			console.log( oSourceModel.getData() );
			if ( oSourceModel.getProperty("/version") ) {
				console.log( "Data Loaded");

				oSourceData = oSourceModel.getData().feed.entry;
				iSourceLength = oSourceModel.getProperty("/feed/entry/length");

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
							case "Curious Trenčín 2026 stage":
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
							default:
						}
					}
				}

				MessageToast.show("Data updated", {duration: 1000});

			} else {
				console.log( "Data NOT Loaded");
			}
									

			var oToday = new Date();

			if ( oToday.getMonth() === 7 && oToday.getDate() === 28 ) {
				oStartFestDate = new Date("2021", "7", "28", "9", "00");
				oStartFestDate2 = new Date("2021", "7", "29", "00", "00");
				this.byId("day04").setType("Emphasized");
			} else {
				if ( oToday.getMonth() === 7 && oToday.getDate() === 27 ) {
					oStartFestDate = new Date("2021", "7", "27", "9", "00");
					oStartFestDate2 = new Date("2021", "7", "28", "00", "00");
					this.byId("day03").setType("Emphasized");
				} else {
					if ( oToday.getMonth() === 7 && oToday.getDate() === 26 ) {
						oStartFestDate = new Date("2021", "7", "26", "9", "00");
						oStartFestDate2 = new Date("2021", "7", "27", "00", "00");
						this.byId("day02").setType("Emphasized");
					} else {
						oStartFestDate = new Date("2021", "7", "25", "12", "00");
						oStartFestDate2 = new Date("2021", "7", "26", "00", "00");
						this.byId("day01").setType("Emphasized");
					}
				}
			}

			aStages = [
				{
					text: "Hlavný stage",
					type: CalendarDayType.Type09
				},
				{
					text: "Curious Trenčín 2026 stage",
					type: CalendarDayType.Type10
				}
			]

			oModel.setData({
				"startDate": oStartFestDate,
				"startDate2": oStartFestDate2,
				"stages": aStages,
				"events": aDataEvents
			});

			oSourceModel.detachRequestCompleted(this.afterDataLoaded, this);
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

		    var tmp = document.createElement("div"); // eslint-disable-line sap-no-element-creation
		    tmp.innerHTML = sDescr;
			var sText = tmp.textContent || tmp.innerText;
			
			return sShortDescr + "\n" + "\n" + sText;
		},
		
		handleAppointmentSelect: function (oEvent) {
			var oView = this.getView();
			var oAppointment = oEvent.getParameter("appointment");

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

				MessageBox.show(sDescr, {
					title: oAppointment.getTitle()
				});
			}
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
					oModel.setProperty("/startDate", new Date("2021", "7", "25", "12", "00"));
					oModel.setProperty("/startDate2", new Date("2021", "7", "26", "00", "00"));
					break;
				case "day02":
					oModel.setProperty("/startDate", new Date("2021", "7", "26", "9", "00"));
					oModel.setProperty("/startDate2", new Date("2021", "7", "27", "00", "00"));
					break;
				case "day03":
					oModel.setProperty("/startDate", new Date("2021", "7", "27", "9", "00"));
					oModel.setProperty("/startDate2", new Date("2021", "7", "28", "00", "00"));
					break;
				case "day04":
					oModel.setProperty("/startDate", new Date("2021", "7", "28", "9", "00"));
					oModel.setProperty("/startDate2", new Date("2021", "7", "29", "00", "00"));
					break;
			}
			
			this.byId(sId).setType("Emphasized");
		},

		handleOpenLegend: function (oEvent) {
			var oSource = oEvent.getSource();

			if (!this._oLegendPopover) {
				Fragment.load({
					id: "LegendFrag",
					name: "Project.PD2021.view.fragment.Legend",
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

		afterInfoLoaded: function () {
			var oView = this.getView();
			var oSourceInfoModel = oView.getModel("sourceInfoModel");
			var oInfoModel = oView.getModel("infoModel");
			
			var aData = [];
			var sInfo = "";
			
			var oInfoData;
			var iLength;
			var iNumberOfRows = 0;	

			console.log( oSourceInfoModel.getData() );
			if ( oSourceInfoModel.getProperty("/version") ) {
				console.log( "Info Loaded");

				oInfoData = oSourceInfoModel.getData().feed.entry;
				iLength = oSourceInfoModel.getProperty("/feed/entry/length");		
				
				for (var i = 0; i < iLength; i++) {
					var iRow = oInfoData[i].gs$cell.row;				
					var iCol = oInfoData[i].gs$cell.col;				
					var sValue = oInfoData[i].gs$cell.$t;

					if (!aData[iRow - 1]) {
						aData[iRow - 1] = [];
					}
					aData[iRow - 1][iCol - 1] = sValue;
					iNumberOfRows = iRow;
				}

				for (var j = 0; j < iNumberOfRows; j++) {
					if ( aData[j] ) {
						sInfo = sInfo + aData[j][0] + "\n" + "\n" + aData[j][1] + "\n" + "\n" + " ***** " + "\n" + "\n"
					}
				}

				oInfoModel.setProperty("/info", sInfo);

			} else {
				console.log( "Info NOT Loaded");
			}		

			oSourceInfoModel.detachRequestCompleted(this.afterInfoLoaded, this);

		},

		onInfoPress: function () {
			var oView = this.getView();
			var oInfoModel = oView.getModel("infoModel");
			var sInfo = oInfoModel.getProperty("/info");
					
			MessageBox.show(sInfo, {title: "Informácie pre návštevníkov"});
		},

		onRefresh: function () {
			var oView = this.getView();
			
			var oSourceModel = oView.getModel("sourceDataModel");
			var sUriData = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetSource.uri;

			var oSourceInfoModel = oView.getModel("sourceInfoModel");
			var sUriInfo = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetInfoSource.uri;
			

			oSourceModel.attachRequestCompleted(this.afterDataLoaded, this);	
			oSourceModel.loadData(sUriData);

			oSourceInfoModel.attachRequestCompleted(this.afterInfoLoaded, this);	
			oSourceInfoModel.loadData(sUriInfo);
		}

	});
});