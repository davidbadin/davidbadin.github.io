sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/m/MessageToast",
	"sap/m/MessageBox",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/routing/History"
], function (Controller, MessageToast, MessageBox, JSONModel, History) {
	"use strict";

	return Controller.extend("Project.Decrypto.controller.play", {
		onInit: function () {
			var oView = this.getView();
			var oCodeJsonModel = new JSONModel();
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			
			if (!oStorage.get("generatedWords")) {

				oCodeJsonModel.setData({
					"cCode1": "X",
					"cCode2": "X",
					"cCode3": "X"
				});	

			} else {	// in case the game didn't finnish properly / was resumed / the page was reloaded
				var aGeneratedWords = oStorage.get("generatedWords");
				var aGeneratedIndex = oStorage.get("generatedIndex");
				var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");
				var oButtonGenerateWords = oView.byId("generateWordsButton");
				
				// restore words and indexes
				oView.byId("codeWord01").setText("1: " + aGeneratedWords[0]);
				oView.byId("codeWord02").setText("2: " + aGeneratedWords[1]);
				oView.byId("codeWord03").setText("3: " + aGeneratedWords[2]);
				oView.byId("codeWord04").setText("4: " + aGeneratedWords[3]);
				oSettingsModel.setProperty("/savedIndex", aGeneratedIndex);

				oButtonGenerateWords.setText(this.getOwnerComponent().getModel("i18n").getProperty("regenerateWords"));
				oSettingsModel.setProperty("/wordsGenerated", true);

				if (!oStorage.get("generatedCode")) {
					oCodeJsonModel.setData({
						"cCode1": "X",
						"cCode2": "X",
						"cCode3": "X"
					});	
				} else {
					var aGeneratedCode = oStorage.get("generatedCode");

					// restore code
					oCodeJsonModel.setData({
						"cCode1": aGeneratedCode[0],
						"cCode2": aGeneratedCode[1],
						"cCode3": aGeneratedCode[2]
					});	

					oSettingsModel.setProperty("/gameStarted", true);
					oSettingsModel.setProperty("/codeExpanded", true);
				}
			}

			this.getView().setModel(oCodeJsonModel, "codeModel");

		},

		onGenerateWords: function () {
			var aCode = [];
			var aIndex = [];
			var oView = this.getView();
			var oModel = this.getOwnerComponent().getModel();
			var oData = oModel.getProperty("/WordsCollection");
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			var aGeneratedWords = [];
			var oButtonGenerateWords = oView.byId("generateWordsButton");

			// Generate codewords
			aCode = this.generateCode(oModel.getProperty("/WordsCollection/length"));
			oView.byId("codeWord01").setText("1: " + oData[aCode[0] - 1].word);
			oView.byId("codeWord02").setText("2: " + oData[aCode[1] - 1].word);
			oView.byId("codeWord03").setText("3: " + oData[aCode[2] - 1].word);
			oView.byId("codeWord04").setText("4: " + oData[aCode[3] - 1].word);

			// Save indexes of used codewords in descending order
			for (var i = 0; i < 4; i++) {
				aIndex[i] = aCode[i] - 1;
			}
			aIndex.sort(function (a, b) {
				return b - a;
			});
			oSettingsModel.setProperty("/savedIndex", aIndex);

			// backup words & index in case of page being reloaded
			aGeneratedWords = [
				oData[aCode[0] - 1].word, 
				oData[aCode[1] - 1].word, 
				oData[aCode[2] - 1].word, 
				oData[aCode[3] - 1].word
			];
			oStorage.put("generatedWords", aGeneratedWords);
			oStorage.put("generatedIndex", aIndex);

			// If generated for first time
			if (!oSettingsModel.getProperty("/wordsGenerated")) {
				oButtonGenerateWords.setText(oView.getModel("i18n").getProperty("regenerateWords"));
				oSettingsModel.setProperty("/wordsGenerated", true);
			}

		},

		onStartGame: function () {
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");

			oSettingsModel.setProperty("/gameStarted", true);
			oSettingsModel.setProperty("/codeExpanded", true);
		},

		onHideCode: function () {
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");
			oSettingsModel.setProperty("/codeExpanded", !oSettingsModel.getProperty("/codeExpanded"));
		},

		formatHideShowButton: function (sExpanded) {
			if (sExpanded) {
				return this.getView().getModel("i18n").getProperty("hideCode");
			} else {
				return this.getView().getModel("i18n").getProperty("showCode");
			}
		},

		onGenerateNewCode: function () {
			var aCode = [];
			var oView = this.getView();
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			
			aCode = this.generateCode(4);

			oView.byId("code1").setText(aCode[0]);
			oView.byId("code2").setText(aCode[1]);
			oView.byId("code3").setText(aCode[2]);

			oStorage.put("generatedCode", aCode);

		},

		generateCode: function (iLength) {
			var aCode = [];
			var j;
			var temp;

			// FISHER-YATES SHUFFLE METHOD
			for (var i = 0; i < iLength; i++) {
				aCode[i] = i + 1;
			}
			while (iLength--) {
				j = Math.floor(Math.random() * (iLength + 1));
				temp = aCode[iLength];
				aCode[iLength] = aCode[j];
				aCode[j] = temp;
			}

			return aCode;
		},

		onCancelGame: function () {
			var oView = this.getView();
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			var oHistory = History.getInstance();
			var sPreviousHash = oHistory.getPreviousHash();
			
			var oSettingsModel = this.getOwnerComponent().getModel("settingsModel");

			var sConfirmQuestion = oView.getModel("i18n").getProperty("confirmCancelText");
			var sConfirmTitle = oView.getModel("i18n").getProperty("cancelGame");

			if (oSettingsModel.getProperty("/wordsGenerated")) {
				MessageBox.confirm(sConfirmQuestion, {
					icon: MessageBox.Icon.WARNING,
					title: sConfirmTitle,
					actions: [
						MessageBox.Action.CANCEL,
						MessageBox.Action.YES
					],
					onClose: function (sAction) {

						if (sAction === "YES") {
							var oButtonGenerateWords = oView.byId("generateWordsButton");
							var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 

							// delete stored data
							if (oStorage.get("generatedWords")) {
								oStorage.remove("generatedWords");
							}	
				
							if (oStorage.get("generatedIndex")) {
								oStorage.remove("generatedIndex");
							}	
				
							if (oStorage.get("generatedCode")) {
								oStorage.remove("generatedCode");
							}	
							
							if (sPreviousHash !== undefined) {
								window.history.go(-1);
							} else {
								oRouter.navTo("RouteIntro", true);
							}
							
							// set the page to the default state
							oSettingsModel.setProperty("/wordsGenerated", false);
							oSettingsModel.setProperty("/gameStarted", false);
							oSettingsModel.setProperty("/codeExpanded", false);
							oButtonGenerateWords.setText(oView.getModel("i18n").getProperty("generateWords"));
							oView.byId("code1").setText("X");
							oView.byId("code2").setText("X");
							oView.byId("code3").setText("X");
							oView.byId("codeWord01").setText("");
							oView.byId("codeWord02").setText("");
							oView.byId("codeWord03").setText("");
							oView.byId("codeWord04").setText("");
						}
					}
				});

			} else {
				oRouter.navTo("RouteIntro");
			}

		},

		onFinishGame: function () {
			var oView = this.getView();
			var oComponent = this.getOwnerComponent();
			var oModel = oComponent.getModel();
			var oSettingsModel = oComponent.getModel("settingsModel");
			var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 
			
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			var oHistory = History.getInstance();
			var sPreviousHash = oHistory.getPreviousHash();

			var sConfirmTitle = oView.getModel("i18n").getProperty("finishGame");
			var sConfirmQuestion = oView.getModel("i18n").getProperty("confirmFinishText");

			if (oSettingsModel.getProperty("/wordsGenerated")) {
				MessageBox.confirm(sConfirmQuestion, {
					icon: MessageBox.Icon.SUCCESS,
					title: sConfirmTitle,
					actions: [
						MessageBox.Action.CANCEL,
						MessageBox.Action.YES
					],
					onClose: function (sAction) {

						if (sAction === "YES") {
							var aData = oModel.getProperty("/WordsCollection");
							var oButtonGenerateWords = oView.byId("generateWordsButton");
							var aIndex = [];
							var numberOfWords;
							var oStorage = jQuery.sap.storage(jQuery.sap.storage.Type.local); 

							// delete stored data
							if (oStorage.get("generatedWords")) {
								oStorage.remove("generatedWords");
							}	
				
							if (oStorage.get("generatedIndex")) {
								oStorage.remove("generatedIndex");
							}	
				
							if (oStorage.get("generatedCode")) {
								oStorage.remove("generatedCode");
							}	

							if (sPreviousHash !== undefined) {
								window.history.go(-1);
							} else {
								oRouter.navTo("RouteIntro", true);
							}

							// set the page to the default state
							oSettingsModel.setProperty("/wordsGenerated", false);
							oSettingsModel.setProperty("/gameStarted", false);
							oSettingsModel.setProperty("/codeExpanded", false);
							oButtonGenerateWords.setText(oView.getModel("i18n").getProperty("generateWords"));
							oView.byId("code1").setText("X");
							oView.byId("code2").setText("X");
							oView.byId("code3").setText("X");
							oView.byId("codeWord01").setText("");
							oView.byId("codeWord02").setText("");
							oView.byId("codeWord03").setText("");
							oView.byId("codeWord04").setText("");

							// delete used codewords
							aIndex = oSettingsModel.getProperty("/savedIndex");
							for (var i = 0; i < aIndex.length; i++) {
								aData.splice(aIndex[i], 1);
							}
							oModel.setProperty("/WordsCollection", aData);

							var oData = oModel.getData();
							oStorage.put("wordsDtb", oData);

							// update the number of words in settingsModel
							numberOfWords = oModel.getProperty("/WordsCollection/length");
							oComponent.getModel("settingsModel").setProperty("/numberOfWords", numberOfWords);
						}
					}
				});

			} else {
				if (sPreviousHash !== undefined) {
					window.history.go(-1);
				} else {
					oRouter.navTo("RouteIntro", true);
				}
			}
		}

	});

});