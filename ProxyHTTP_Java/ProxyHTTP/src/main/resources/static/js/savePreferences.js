function save(){
	
	var op1_os = document.getElementById("op1_os");
	var op1_os_value = op1_os.options[op1_os.selectedIndex].value;
	
	var op1_browser = document.getElementById("op1_browser");
	var op1_browser_value = op1_browser.options[op1_browser.selectedIndex].value;
	
	var op2 = document.getElementById("op2");
	var op2_value = String( op2.checked );
	
	var op3 = document.getElementById("op3");
	var op3_value = String( op3.checked );
	
	var op4 = document.getElementById("op4");
	var op4_value = String( op4.checked );
	
	var op5 = document.getElementById("op5");
	var op5_value = String( op5.checked );
	
	this.changeStateButton();
	
	$.ajax({
	     type: "POST",
	     url: "/savePreferences",
	     data: { _op1_os: op1_os_value, 
	    	 	 _op1_browser: op1_browser_value,
	    	 	 _op2: op2_value,
	    	 	 _op3: op3_value,
	    	 	 _op4: op4_value,
	    	 	 _op5: op5_value } // parameters
	})
}

function changeStateButton(){
	document.getElementById("savePrefState1").style.display="none";
	document.getElementById("savePrefState2").style.display="inline-block";
}

$('.isOption').click(function(){
	document.getElementById("savePrefState1").style.display="inline-block";
	document.getElementById("savePrefState2").style.display="none";	
});