/* global QUnit */
QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function () {
	"use strict";

	sap.ui.require([
		"Project/Lasicke/test/unit/AllTests"
	], function () {
		QUnit.start();
	});
});