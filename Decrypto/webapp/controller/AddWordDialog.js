sap.ui.define([
	"sap/ui/base/ManagedObject",
	"sap/ui/core/Fragment",
	"sap/m/MessageToast"
], function (ManagedObject, Fragment, MessageToast) {
	"use strict";

	return ManagedObject.extend("Project.Decrypto.controller.AddWordDialog", {

		constructor: function (oView) {
			this._oView = oView;
		},

		exit: function () {
			delete this._oView;
		},

		open: function () {
			var oView = this._oView;

			// create dialog lazily
			if (!oView.byId("addWordDialog")) {
				var oFragmentController = {
					
					onWordEnter: function (oEvent) {
						var sQuery = oEvent.getParameter("newValue");
						if (sQuery) {
							oView.byId("saveWordButton").setEnabled(true);
						} else {
							oView.byId("saveWordButton").setEnabled(false);
						}
					},

					onSaveWord: function () {
						var oModel = oView.getModel();
						var oI18N = oView.getModel("i18n").getResourceBundle();
						var oData = oModel.getData();
						var oInput = oView.byId("inputWord");
						var sQuery = oInput.getValue();
						var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
						var numberOfWords;

						// update word database and number of words
						oData.WordsCollection.push({
							"word": sQuery
						});
						oModel.setData(oData);
						numberOfWords = oModel.getProperty("/WordsCollection/length");
						oView.getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
						oStorage.put("wordsDtb", oData);

						// clear input and set focus
						oInput.setValue("");
						oView.byId("saveWordButton").setEnabled(false);
						MessageToast.show(oI18N.getText("wordAdded", sQuery));
						jQuery.sap.delayedCall(500, this, function () {
							oView.byId("inputWord").focus();
						});

					},
					
					onCloseDialog: function () {
						oView.byId("addWordDialog").close();
					}
				};
				// load asynchronous XML fragment
				Fragment.load({
					id: oView.getId(),
					name: "Project.Decrypto.view.fragments.addWord",
					controller: oFragmentController
				}).then(function (oDialog) {
					// connect dialog to the root view of this component (models, lifecycle)
					oView.addDependent(oDialog);
					oDialog.open();
					jQuery.sap.delayedCall(500, this, function () {
						oView.byId("inputWord").focus();
					});
				});
			} else {
				oView.byId("addWordDialog").open();
				jQuery.sap.delayedCall(500, this, function () {
					oView.byId("inputWord").focus();
				});
			}

			oView.byId("inputWord").setValue("");

		}

	});

});