sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel"
], function (Controller, JSONModel) {
	"use strict";

	return Controller.extend("Project.RasPiDB.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oModel = new JSONModel();

			$.ajax({                                      
			  url: 'http://192.168.0.101/sql/gettestdb.php',             
		      async:false,        
		      success: function(data)          //on recieve of reply
		      {
				console.log("Success"); // eslint-disable-line no-console
				oModel.setData(JSON.parse(data));
				
		      },
		      error: function(err)
		      {
		    	  console.log(err); // eslint-disable-line no-console
		      }
		    });
			
			console.log(oModel.getData()); // eslint-disable-line no-console
			
			oView.setModel(oModel);

		}
	});
});