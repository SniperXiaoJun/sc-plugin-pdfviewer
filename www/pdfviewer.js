var exec = require("cordova/exec");
module.exports = {
	viewPdf: function(content,successCallback, errorCallback){
		exec(
		successCallback,
		errorCallback,
		"SCPDFViewer",//feature name
		"viewpdf",//action
		content//要传递的参数，string格式
		);
	}
}
