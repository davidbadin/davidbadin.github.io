sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/core/routing/History"
], function (Controller, History) {
	"use strict";

	return Controller.extend("Project.Decrypto.controller.wordsDtb", {
		onInit: function () {},

		onNavBack: function () {
			var oHistory = History.getInstance();
			var sPreviousHash = oHistory.getPreviousHash();
			
			if (sPreviousHash !== undefined) {
				window.history.go(-1);
			} else {
				var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
				oRouter.navTo("RouteIntro", true);
			}
		},

		formatDtbTitle: function (sTitle, sNumber) {
			return sTitle + " (" + sNumber + ")";
		},

		onDeleteItem: function (oEvent) {
			var oModel = this.getOwnerComponent().getModel();
			var aData = oModel.getProperty("/WordsCollection");

			var oItem = oEvent.getParameter("listItem");
			var iIndex = oItem.getParent().indexOfItem(oItem);
			var numberOfWords;

			aData.splice(iIndex, 1);
			oModel.setProperty("/WordsCollection", aData);

			// update the number of words in settingsModel
			numberOfWords = oModel.getProperty("/WordsCollection/length");
			this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
		},

		onAddWord: function () {
			this.getOwnerComponent().openAddWordDialog();
			// sap.ui.controller("Project.Decrypto.controller.intro").onAddWord();
		},

		onDeleteAllWords: function () {
			if (!this._oConfirmDialog) {
				this._oConfirmDialog = sap.ui.xmlfragment(this.getView().getId(), "Project.Decrypto.view.fragments.confirmDialog", this);
				this.getView().addDependent(this._oConfirmDialog);
			}
			this._oConfirmDialog.open();
		},
		
		onDeleteAllWordsConfirmed: function () {
			var oModel = this.getOwnerComponent().getModel();
			var oData = {
				"WordsCollection": []
			};
			oModel.setData(oData);
			var numberOfWords = oModel.getProperty("/WordsCollection/length");
			this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
			this._oConfirmDialog.close();
		},
		
		onCloseDialog: function () {
			this._oConfirmDialog.close();
		}

	});

});