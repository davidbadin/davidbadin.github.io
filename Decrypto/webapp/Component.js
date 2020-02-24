sap.ui.define([
	"sap/ui/core/UIComponent",
	"sap/ui/Device",
	"Project/Decrypto/model/models",
	"sap/ui/model/json/JSONModel",
	"./controller/AddWordDialog"
], function (UIComponent, Device, models, JSONModel, AddWordDialog) {
	"use strict";

	return UIComponent.extend("Project.Decrypto.Component", {

		metadata: {
			manifest: "json"
		},

		/**
		 * The component is initialized by UI5 automatically during the startup of the app and calls the init method once.
		 * @public
		 * @override
		 */
		init: function () {
			// call the base component's init function
			UIComponent.prototype.init.apply(this, arguments);

			// enable routing
			this.getRouter().initialize();

			// set the device model
			this.setModel(models.createDeviceModel(), "device");
			
			// set the "wordsModel" for the whole app
			var oWordsModel = new JSONModel();
			this.setModel(oWordsModel);
			
			// set dialog
			this._addWordDialog = new AddWordDialog(this.getRootControl());
		},
		
		exit: function () {
			this._addWordDialog.destroy();
			delete this._addWordDialog;
		},
		
		openAddWordDialog: function () {
			this._addWordDialog.open();
		}
		
	});
});