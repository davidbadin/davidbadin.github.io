/* global QUnit */
QUnit.config.autostart = false;

sap.ui.getCore().attachInit(function () {
	"use strict";

	sap.ui.require([
		"Project/RasPiDB/test/integration/AllJourneys"
	], function () {
		QUnit.start();
	});
});