sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel"
], function (Controller, JSONModel) {
	"use strict";

	return Controller.extend("Project.RasPiDB.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oModel = oView.getModel();
			var sUrl = this.getOwnerComponent().getMetadata().getManifestEntry("sap.app").dataSources.phpSource.uri;

			$.ajax({                                      
			  url: sUrl,
		      async:false,        
		      success: function(data)          //on recieve of reply
		      {
				oModel.setData(JSON.parse(data));
				
		      },
		      error: function(err)
		      {
		    	  console.log(err); // eslint-disable-line no-console
		      }
			});
			
			// alternative solution: in manifest.json:
			//
			// "models": {
			// 	"": {
			// 		"type": "sap.ui.model.json.JSONModel",
			// 		"dataSource": "phpSource",
			// 		"preload": true
			// 	}
			// }
			
			console.log(oModel.getData().result); // eslint-disable-line no-console
			
			oView.setModel(oModel);

		}
	});
});