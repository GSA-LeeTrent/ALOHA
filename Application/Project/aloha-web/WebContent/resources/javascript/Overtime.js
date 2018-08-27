function onSelectedPayPeriod() {
	var value = "Pay Period: " + document.getElementById("payPeriods").options[document.getElementById("payPeriods").selectedIndex].text;		
	document.getElementById("otTaskListCaption").innerHTML = value;
}
function addTaskRow() {
	var indexForLastVisibleRow = findIndexForLastVisibleRow();
	var startIndex = parseInt( (indexForLastVisibleRow + 1) ,10);
	for ( var ii = startIndex; ii <= 25; ii++ ) {
		if ( document.getElementById("otTask_" + ii).style.display == "none" ) {
			document.getElementById("otTask_" + ii).style.display = '';
			break;
		}  
	}
	return false;
}
function findIndexForLastVisibleRow() {
	var ii = parseInt(1,10);
	var indexForLastVisibleRow = parseInt(1,10);
	for ( ii = 25; ii > 0; ii-- ) {
		if ( document.getElementById("otTask_" + ii).style.display == '' ) {
			indexForLastVisibleRow = ii;
			break;
		}  
	}
	return indexForLastVisibleRow;
}
function removeTaskRow(rowId, descId, estHoursId) {
	var hourObj = document.getElementById(estHoursId);
	if ( !isFloat0to2d0to1(hourObj.value) ) {
		hourObj.style.backgroundColor="white";
		document.getElementById("otTaskErrorList").innerHTML = "";
		document.getElementById("otTaskErrorList").style.display="none";
	}	
	
	document.getElementById(descId).value = "";
	document.getElementById(estHoursId).value = "";
	
	document.getElementById(descId).style.backgroundColor = "#FFF";
	document.getElementById(estHoursId).style.backgroundColor = "#FFF";

	var indexOf = parseInt(rowId.indexOf('_'), 10);
	indexOf++;
	var index = parseInt(rowId.substring(indexOf), 10);
	
	if ( document.getElementById("otTaskDescError_" + index ) != null ) {
		document.getElementById("otTaskDescError_" + index).innerHTML = "";
	}
	if ( document.getElementById("otEstHoursError_" + index ) != null ) {
		document.getElementById("otEstHoursError_" + index).innerHTML = "";
	}	

	document.getElementById(rowId).style.display = "none";
	calcTotalOvertimeHours();
	recheckOvertimeHours();
}	
function removeTaskErrorRow(rowId, descId, estHoursId, descErrorId, estHoursErrorId) {
	if ( document.getElementById(descErrorId) ) {
		document.getElementById(descErrorId).innerHTML = "";	
	}
	if (document.getElementById(estHoursErrorId)) {
		document.getElementById(estHoursErrorId).innerHTML = "";	
	}
	removeTaskRow(rowId, descId, estHoursId);
}	

function onCreateOTFormButtonClicked() {
	document.getElementById("otCreateAvailableActions").style.display="none";
}
function recheckOvertimeHours() {
	var errorCount = parseInt(0,10);
	var index = parseInt(0,10);	
	
	for ( index = 1; index <= 25; index++ ) {
		var value = document.getElementById("otEstHours_" + index).value;		
		if ( (value != null) && (value.length > 0) ) {
			if ( ! isFloat0to2d0to1(value) ) {
				errorCount++;
			} 
		}
	}	 
	if ( errorCount == 0 ) {
		document.getElementById("otTaskErrorList").innerHTML = "";
		document.getElementById("otTaskErrorList").style.display="none";	
		document.getElementById("otCreateSubmitButton").disabled=false;
	}		
}
function processOvertimeHours(otObj) {
	var errorMsg = "";
	if ( isFloat0to2d0to1(otObj.value) ) {
		document.getElementById("otTaskErrorList").innerHTML = "";
		document.getElementById("otTaskErrorList").style.display="none";
		otObj.style.backgroundColor="white";
		document.getElementById("otCreateSubmitButton").disabled=false;
		calcTotalOvertimeHours();
	} else {
		errorMsg += "<li>'" + otObj.value + "' is not a valid hour value</li>";
		document.getElementById("otTaskErrorList").innerHTML = errorMsg;
		document.getElementById("otTaskErrorList").style.display="block";
		otObj.style.backgroundColor="#FAAFBE";
		otObj.focus();
		document.getElementById("totalOvertimeHours").innerHTML = "";
		document.getElementById("otCreateSubmitButton").disabled=true;
	}
}
function calcTotalOvertimeHours() {
	var taskListSize = parseInt(document.getElementById("taskListSize").value);
	var index = parseInt(0,10);	
	var totalHours = parseFloat("0.0");
	for ( index = 1; index <= taskListSize; index++ ) {
		var itemValue = document.getElementById("otEstHours_" + index).value;
		if ( (itemValue != null) && (itemValue.length > 0) ) {
			if ( isFloat0to2d0to1(itemValue) ) {
				var floatValue = parseFloat(itemValue);
				totalHours = totalHours + floatValue;							
			}
		}
	}
	document.getElementById("totalOvertimeHours").innerHTML = totalHours.toFixed(1);
}
function isFloat0to2d0to1(str) {
	/* Verify float 3 to 5 digits, decimal max 3 digits
     *    if decimal point must have at least 1 decimal digit
     * Return boolean
     */
    str = str.replace(/^\s+|\s+$/g, '');
    return /^[+]?\d{0,2}(\.\d{0,1})?$/.test(str);
}