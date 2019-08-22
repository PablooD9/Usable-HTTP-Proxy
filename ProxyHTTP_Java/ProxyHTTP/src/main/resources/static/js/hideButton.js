function hideUpdateButton(){
	document.getElementsByClassName("buttonUpdate")[0].style.display="none";
	document.getElementsByClassName("buttonLoading")[0].style.display="block";
	
	var currentURL = window.location.href;
	var urlUpdateMaliciousHosts = 'updateMaliciousHosts';
	
	var URL = currentURL + urlUpdateMaliciousHosts;
	
	window.location.href=URL;
}