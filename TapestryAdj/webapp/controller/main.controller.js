sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel"
], function (Controller, JSONModel) {
	"use strict";

	return Controller.extend("Project.TapestryAdj.controller.main", {
		onInit: function () {
			var oView = this.getView();
			
			var oModel = new JSONModel();
			this.getOwnerComponent().setModel(oModel);
			
			var oSourceModel = oView.getModel("sheetSourceModel");
			var sUri = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.sheetSource.uri;
			
			// load data from spreadsheet
			oSourceModel.loadData(sUri);
			oSourceModel.attachRequestCompleted(this.afterDataLoaded, this);
		},
		
		afterDataLoaded: function () {
			
			var oView = this.getView();
			var oModel = oView.getModel();
			var oSourceModel = oView.getModel("sourceJsonModel");
			var oSourceData = oSourceModel.getData().feed.entry;
			var iSourceLength = oSourceModel.getProperty("/feed/entry/length");
			
			console.log(oSourceData); // eslint-disable-line no-console
			console.log(oModel); // eslint-disable-line no-console
		}
		
		
	});
});