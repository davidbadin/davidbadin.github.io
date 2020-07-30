sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/model/json/JSONModel"
], function (Controller, JSONModel) {
	"use strict";

	return Controller.extend("Project.RasPiDB.controller.Main", {
		onInit: function () {
			
			var oView = this.getView();
			var oModel = oView.getModel();

			$.ajax({                                      
			  url: "http://192.168.0.101/sql/gettestdb.php",
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
			
			console.log(oModel.getData()); // eslint-disable-line no-console
			
			oView.setModel(oModel);

		}
	});
});