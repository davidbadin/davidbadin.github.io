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

			var aData = [];

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
						"IsAutomaComp": bIsAutomaComp
					});
				}
			}

			oModel.setData(oDataJson);
			oSourceModel.detachRequestCompleted(this.afterDataLoaded, this);

			console.log(oModel); // eslint-disable-line no-console

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
		}


		
	});
});