sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/m/MessageToast"
], function (Controller, MessageToast) {
	"use strict";
	return Controller.extend("Project.Decrypto.controller.intro", {
		onInit: function () {

			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 

			// check if there are any locally stored words, if not, create an empty wordDtb
			if ( !oStorage.get("wordsDtb") ) {
				var oData = {
					"WordsCollection": []
				};
				oStorage.put("wordsDtb", oData);

			} else {
				var oData = oStorage.get("wordsDtb");
			}

			this.getOwnerComponent().getModel().setData(oData);
			this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", this.getOwnerComponent().getModel().getProperty("/WordsCollection/length"));
			
		},

		onPressPlay: function (oEvent) {
			var oModel = this.getOwnerComponent().getModel();
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			
			var numberOfWords = oModel.getProperty("/WordsCollection/length");
			
			if (numberOfWords < 4) {
				MessageToast.show("You need at least 4 words");
			} else {
				oRouter.navTo("RoutePlay");
			}
		},

		onPressDtb: function () {
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("RouteDtb");
		},

		onAddWord: function () {
			this.getOwnerComponent().openAddWordDialog();
		}
		
	});
});