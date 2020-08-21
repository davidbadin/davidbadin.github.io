sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/Fragment",
	"sap/ui/unified/library",
	"sap/m/MessageBox"
], function (Controller, JSONModel, Fragment, unifiedLibrary, MessageBox) {
	"use strict";

	return Controller.extend("Project.PD2020.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oSourceModel = oView.getModel("sourceDataModel");
			
			var sUri = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetSource.uri;
			
			// this.byId("PC1-Header").setVisible(false);
			
			this.byId("PC1-Header-Spacer").setVisible(false);
			this.byId("PC1-Header-NavToolbar").setVisible(false);
						
			// $("div.sapMSinglePCRowHeaders").eq(2).remove();
			// $("div.sapMSinglePCRowHeaders").eq(1).hide();

			oSourceModel.loadData(sUri);
			oSourceModel.attachRequestCompleted(this.afterDataLoaded, this);

		},

		afterDataLoaded: function () {
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sourceDataModel");
			var CalendarDayType = unifiedLibrary.CalendarDayType;

			var aData = [];
			var aDataEvents = [];
			var aStages = [];

			var oStartFestDate;
			var oToday = new Date();
			
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

			

			aStages = [
				{
					text: "Hlavný stage",
					type: CalendarDayType.Type09
				},
				{
					text: "Akustický stage",
					type: CalendarDayType.Type10
				},
				{
					text: "Kinečko",
					type: CalendarDayType.Type01
				}
			]

			// console.log(oToday); // eslint-disable-line no-console

			oModel.setData({
				"startDate": oStartFestDate,
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
		},

		handleOpenLegend: function (oEvent) {
			var oSource = oEvent.getSource();

			if (!this._oLegendPopover) {
				Fragment.load({
					id: "LegendFrag",
					name: "Project.PD2020.view.fragment.Legend",
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
			var oInfoModel = oView.getModel("sourceInfoModel");
			var sUri = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetInfoSource.uri;
			
			var aData = [];
			var sInfo = "";

			oInfoModel.loadData(sUri).then(function () {
				var oInfoData = oInfoModel.getData().feed.entry;
				var iLength = oInfoModel.getProperty("/feed/entry/length");
				var iNumberOfRows = 0;	
				
				
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

				MessageBox.show(sInfo, {
					title: "Informácie pre návštevníkov"
				});
			});
			

			
		}
		




	});
});