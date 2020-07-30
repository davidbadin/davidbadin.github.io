sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel"
], function (Controller, JSONModel) {
	"use strict";

	return Controller.extend("Project.RasPiDB.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oModel = oView.getModel();
			var sUrl = "http://192.168.0.101/sql/gettestdb.php";

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
			// "dataSources": {
			// 	"phpSource": {
			// 		"uri": "http://192.168.0.101/sql/gettestdb.php",
			// 		"type": "JSON"
			// 	}
			// }
			//	...
			// "models": {
			// 	"": {
			// 		"type": "sap.ui.model.json.JSONModel",
			// 		"dataSource": "phpSource",
			// 		"preload": true
			// 	}
			// }
			
			console.log(oModel.getData()); // eslint-disable-line no-console
			
			oView.setModel(oModel);

		}
	});
});