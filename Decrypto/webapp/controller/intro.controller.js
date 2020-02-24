sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/m/MessageToast"
], function (Controller, MessageToast) {
	"use strict";
	return Controller.extend("Project.Decrypto.controller.intro", {
		onInit: function () {
			var oData = {
				"WordsCollection": []
			};
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

			// var oModel = this.getOwnerComponent().getModel();
			// console.log(oModel.getData()); // eslint-disable-line no-console
			// console.log(oModel.getProperty("/WordsCollection/length") ); // eslint-disable-line no-console
			// MessageToast.show("id: " + oModel.getProperty("/WordsCollection/1/id"));
		},

		onPressDtb: function () {
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("RouteDtb");
		},

		onAddWord: function () {
			this.getOwnerComponent().openAddWordDialog();
			// EASIER WAY TO CREATE DIALOG, works for current view only
			// if (!this._oDialogAddWord) {
			// 	this._oDialogAddWord = sap.ui.xmlfragment(this.getView().getId(), "Project.Decrypto.view.fragments.addWord", this);
			// 	this.getView().addDependent(this._oDialogAddWord);
			// }
			// this._oDialogAddWord.open();
			// this.getView().byId("inputWord").setValue("");
		}
		
		// DIALOG/FRAGMENT FUNCTIONS:
		
		// onCloseDialog: function () {
		// 	this._oDialogAddWord.close();
		// }

		// onWordEnter: function (oEvent) {
		// 	var sQuery = oEvent.getParameter("newValue");
		// 	if (sQuery) {
		// 		this.getView().byId("saveWordButton").setEnabled(true);
		// 	} else {
		// 		this.getView().byId("saveWordButton").setEnabled(false);
		// 	}
		// },

		// onSaveWord: function () {
		// 	var oModel = this.getOwnerComponent().getModel();
		// 	var oData = oModel.getData();
		// 	var oInput = this.getView().byId("inputWord");
		// 	var sQuery = oInput.getValue();
		// 	var numberOfWords;

		// 	// update word database and number of words
		// 	oData.WordsCollection.push({
		// 		"word": sQuery
		// 	});
		// 	oModel.setData(oData);
		// 	numberOfWords = oModel.getProperty("/WordsCollection/length");
		// 	this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);

		// 	// clear input and set focus
		// 	oInput.setValue("");
		// 	this.getView().byId("saveWordButton").setEnabled(false);
		// 	oInput.focus();
		// },



	});
});