sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageToast"
], function (Controller, JSONModel, MessageToast) {
	"use strict";

	return Controller.extend("Project.TapestryAdj.controller.main", {
		onInit: function () {
			var oView = this.getView();
			
			var oSourceModel = oView.getModel("sheetSourceModel");
			var sUri = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetSource.uri;
				
			// load data from spreadsheet
			oSourceModel.attachRequestCompleted(this.afterDataLoaded, this);
			oSourceModel.loadData(sUri);
		},
		
		afterDataLoaded: function () {
			
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sheetSourceModel");
			var oSourceData = oSourceModel.getData().feed.entry;
			var iSourceLength = oSourceModel.getProperty("/feed/entry/length");
			var iNumberOfRows = 0;			// number of data rows (header not included)
			var sTitle;

			var aData = [];
			var aImages = [];

			aImages = this.getImagesArray();

			// convert data from source to array
			for (var i = 0; i < iSourceLength; i++) {
				var iRow = oSourceData[i].gs$cell.row;
				var iCol = oSourceData[i].gs$cell.col;
				var sValue = oSourceData[i].gs$cell.inputValue;


				if (iRow > 1) {
					if (!aData[iRow - 2]) {
						aData[iRow - 2] = [];
						iNumberOfRows++;
					}
					aData[iRow - 2][iCol - 1] = sValue;
				}
			}

			// convert data from array to JSON
			var oDataJson = [];
			for (var i = 0; i < iNumberOfRows; i++) {
				if (aData[i]) {						// skip empty line
					var bIsAdjusted;
					var bIsAutomaComp;

					// convert "1"/"0" into boolean t/f
					if (aData[i][2] == "1") {
						bIsAdjusted = true;
					} else {
						bIsAdjusted = false;
					}

					if (aData[i][4] == "1") {
						bIsAutomaComp = true;
					} else {
						bIsAutomaComp = false;
					}

					oDataJson.push({
						"Civilization": aData[i][0],
						"Adjustment": aData[i][1],
						"IsAdjusted": bIsAdjusted,
						"Version": aData[i][3],
						"IsAutomaComp": bIsAutomaComp,
						"Picture": aImages[i]
					});
					if (!sTitle) {
						sTitle = "Tapestry Adjustments (v" + aData[i][3] + ")";
						oView.byId("page").setTitle(sTitle);
					}
				}
			}

			oModel.setData(oDataJson);
			oSourceModel.detachRequestCompleted(this.afterDataLoaded, this);

		},

		onPressItem: function (oEvent) {
			var oBindingContext = oEvent.getSource().getBindingContext();
			var sCivilization = oBindingContext.getProperty("Civilization");
			var sAdjustment = oBindingContext.getProperty("Adjustment");
			var iIsAdjusted = oBindingContext.getProperty("IsAdjusted");

			if (iIsAdjusted == 1) {

				var oDialog = new sap.m.Dialog({
					title: sCivilization,
					type: 'Message',
					content: new sap.m.Text({
						text: sAdjustment
					}),
					beginButton: new sap.m.Button({	
						text: 'Close',
						press: function () {
							oDialog.close();
						}
					}),
					afterClose: function () {
						oDialog.destroy();
					}
				});

				oDialog.open();

			} else {
				MessageToast.show("no change");
			}
		},

		onCheckboxAdjustedSelect: function (oEvent) {
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");
			var bFilterAdjusted = oSettingsModel.getProperty("/filterAdjusted");
			oSettingsModel.setProperty("/filterAdjusted", !bFilterAdjusted);
		},

		onCheckboxAutomaSelect: function (oEvent) {
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");
			var bFilterAutoma = oSettingsModel.getProperty("/filterAutoma");
			oSettingsModel.setProperty("/filterAutoma", !bFilterAutoma);
		},

		getImagesArray: function () {
			var aImages = [
				"webapp/image/00.png",
				"webapp/image/01.png",
				"webapp/image/02.png",
				"webapp/image/03.png",
				"webapp/image/04.png",
				"webapp/image/05.png",
				"webapp/image/06.png",
				"webapp/image/07.png",
				"webapp/image/08.png",
				"webapp/image/09.png",
				"webapp/image/10.png",
				"webapp/image/11.png",
				"webapp/image/12.png",
				"webapp/image/13.png",
				"webapp/image/14.png",
				"webapp/image/15.png"
			];
			return aImages;			
		}
		
	});
});