function deleteException(element){
	element.style.display="none";
	var hostExceptionToDelete = element.getAttribute("data1");
	// element.nextSibling.style.display="inline-block";
	var buttonToShow = document.getElementsByClassName("host-reference-" + hostExceptionToDelete);
	buttonToShow[0].style.display="inline-block";
	
	$.ajax({
	     type: "DELETE",
	     url: "/deleteSecurityException",
	     data: { host: hostExceptionToDelete }
	})
}