sap.ui.define(["sap/ui/core/mvc/Controller", 
	"sap/m/MessageToast"], 
	function (Controller, MessageToast) {
	"use strict";
	return Controller.extend("Project.Lasicke.controller.main", {
		onInit: function () {
			// window.oThis = this;
			this.setButtonsVisible(this);
			setInterval(this.setButtonsVisible, 5000, this);
		},
		
		setButtonsVisible: function (that) {
			that.setDate();
			var oView = that.getView();
			var oSettings = that.getOwnerComponent().getModel("settingsModel");
			oView.byId("btnInd1").setVisible( that.visibility(oSettings.getProperty("/date1")) );
			oView.byId("btnInd2").setVisible( that.visibility(oSettings.getProperty("/date2")) );
			oView.byId("btnInd3").setVisible( that.visibility(oSettings.getProperty("/date3")) );
			oView.byId("btnInd4").setVisible( that.visibility(oSettings.getProperty("/date4")) );
		},
		
		setDate: function () {
			var oSettings = this.getOwnerComponent().getModel("settingsModel");
			var dDate = new Date();
			var sNow = dDate.getFullYear().toString() 
				+ ( "0" + ( dDate.getMonth() + 1 ).toString() ).slice(-2)
				+ ( "0" + ( dDate.getDate() ).toString() ).slice(-2)
				+ ( "0" + ( dDate.getHours() ).toString() ).slice(-2)
				+ ( "0" + ( dDate.getMinutes() ).toString() ).slice(-2);
			oSettings.setProperty("/dateNow", sNow.toString() );
		},
		
		
		
		visibility: function (sDate) {
			var oSettings = this.getOwnerComponent().getModel("settingsModel");
			var sNow = oSettings.getProperty("/dateNow").toString();
			return ( sNow >= sDate );
		},
		
		
		onPressOk: function () {
			var oSettings = this.getOwnerComponent().getModel("settingsModel");
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			
			var sPasswd1 = oSettings.getProperty("/passwd1").toString();
			var sPasswd2 = oSettings.getProperty("/passwd2").toString();
			var sPasswd3 = oSettings.getProperty("/passwd3").toString();

			var sInput = this.byId("input").getValue().toString().toUpperCase().trim();

			if ( sPasswd1 === sInput || sPasswd2 === sInput || sPasswd3 === sInput) {
				oRouter.navTo("reveal");
			} else {
				MessageToast.show("Nesprávne heslo");
				this.setButtonsVisible(this);
			}
			
			
			
		},
		
		onPressInd: function (sTitle, sText) {
			var oDialog = new sap.m.Dialog(
				{
					title: sTitle,
					type: 'Message',
					content: new sap.m.Text(
					{
						text: sText
					}),
					beginButton: new sap.m.Button(
					{	
						text: 'Ok',
						press: function () 
						{
							oDialog.close();
						}
					}),
					afterClose: function () 
					{
						oDialog.destroy();
					}
				}
			);
			oDialog.open();
		}
		
	});
});