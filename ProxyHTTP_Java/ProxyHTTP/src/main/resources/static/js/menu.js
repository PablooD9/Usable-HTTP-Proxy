function openNav() {
	// Let's check if it's already open
	if (document.getElementById("mySidenav").style.width == "14.5rem" 
		|| document.getElementById("mySidenav").style.width == "8rem")
	{
		this.closeNav();
	}
	else { // We can open it
		if ($(window).width() > 450)
		{
			document.getElementById("mySidenav").style.width = "14.5rem";
		}
		else
			document.getElementById("mySidenav").style.width = "8rem";
	}
}

function closeNav() {
	document.getElementById("mySidenav").style.width = "0";
}