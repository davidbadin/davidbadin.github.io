sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/core/routing/History"
], function (Controller, History) {
	"use strict";

	return Controller.extend("Project.Decrypto.controller.wordsDtb", {
		onInit: function () {
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			if (oStorage.get("generatedWords")) {
				oStorage.remove("generatedWords");
			}	

			if (oStorage.get("generatedIndex")) {
				oStorage.remove("generatedIndex");
			}	

			if (oStorage.get("generatedCode")) {
				oStorage.remove("generatedCode");
			}	
		},

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
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 

			var oItem = oEvent.getParameter("listItem");
			var iIndex = oItem.getParent().indexOfItem(oItem);
			var numberOfWords;

			aData.splice(iIndex, 1);
			oModel.setProperty("/WordsCollection", aData);
			
			var oData = oModel.getData();
			oStorage.put("wordsDtb", oData);
			
			// update the number of words in settingsModel
			numberOfWords = oModel.getProperty("/WordsCollection/length");
			this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
		},

		onAddWord: function () {
			this.getOwnerComponent().openAddWordDialog();
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
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			var oData = {
				"WordsCollection": []
			};

			oModel.setData(oData);
			oStorage.put("wordsDtb", oData);

			var numberOfWords = oModel.getProperty("/WordsCollection/length");
			this.getOwnerComponent().getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
			this._oConfirmDialog.close();
		},
		
		onCloseDialog: function () {
			this._oConfirmDialog.close();
		}

	});

});