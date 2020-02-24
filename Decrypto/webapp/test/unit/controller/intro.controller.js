/*global QUnit*/

sap.ui.define([
	"Project/Decrypto/controller/intro.controller"
], function (Controller) {
	"use strict";

	QUnit.module("intro Controller");

	QUnit.test("I should test the intro controller", function (assert) {
		var oAppController = new Controller();
		oAppController.onInit();
		assert.ok(oAppController);
	});

});