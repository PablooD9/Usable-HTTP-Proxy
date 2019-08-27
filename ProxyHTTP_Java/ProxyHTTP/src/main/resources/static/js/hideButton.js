function hideUpdateButton(){
	document.getElementsByClassName("buttonUpdate")[0].style.display="none";
	document.getElementsByClassName("buttonLoading")[0].style.display="block";
	
	var currentURL = window.location.href;
	if (currentURL.includes("https"))
		var URL = "https://localhost:8090/updateMaliciousHosts";
	else
		var URL = "http://localhost:8090/updateMaliciousHosts";
	
	window.location.href=URL;
}