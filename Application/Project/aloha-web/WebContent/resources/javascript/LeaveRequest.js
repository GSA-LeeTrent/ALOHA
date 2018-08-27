function rowIsVisible(index) {
	var rowObj = document.getElementById("scheduleItem_" + index);
	return (rowObj.style.display == '');
}				
function isFloat0to2d0to1(str) {
    /* Verify float 3 to 5 digits, decimal max 3 digits
     *    if decimal point must have at least 1 decimal digit
     * Return boolean
     */
    str = str.replace(/^\s+|\s+$/g, '');
    return /^[-+]?\d{0,2}(\.\d{0,1})?$/.test(str);
}
function calcTotalLeaveHours() {
	var workDayCount = parseInt(document.getElementById("workDayCount").value);
	var index = parseInt(0,10);
	var totalHours = parseFloat("0.0");
	for ( index = 1; index <= workDayCount; index++ ) {
		var itemValue = document.getElementById("numberOfLeaveHours_" + index).value;
		if ( (itemValue != null) && (itemValue.length > 0) ) {
			if ( isFloat0to2d0to1(itemValue) ) {
				var floatValue = parseFloat(itemValue);
				totalHours = totalHours + floatValue;							
			}
		}
	}
	document.getElementById("totalLeaveHours").innerHTML = totalHours.toFixed(1);				
}
function getIndex(obj) {
	var objId = obj.id;
	var indexOf = parseInt(objId.indexOf('_'), 10);
	indexOf++;
	var index = parseInt(objId.substring(indexOf), 10);
	return index;
}
function copyRow(obj) {
	var toIndex = getIndex(obj);
	var fromIndex = parseInt((toIndex-1),10);
	if ( ! rowIsVisible(fromIndex) ) {
		--fromIndex;
	}	
	document.getElementById("selectedLeaveTypeCode_" + toIndex).selectedIndex 
		= document.getElementById("selectedLeaveTypeCode_" + fromIndex).selectedIndex;
	document.getElementById("numberOfLeaveHours_" + toIndex).value 
		= document.getElementById("numberOfLeaveHours_" + fromIndex).value;
	document.getElementById("startTime_" + toIndex).selectedIndex 
		= document.getElementById("startTime_" + fromIndex).selectedIndex;
	calcTotalLeaveHours();
	return false;					
}
function clearRow(obj) {
	var index = getIndex(obj);
	document.getElementById("selectedLeaveTypeCode_" + index).selectedIndex = 0;		
	document.getElementById("numberOfLeaveHours_" + index).value = "";
	document.getElementById("startTime_" + index).selectedIndex = 0;					
	calcTotalLeaveHours();
	return false;
}
function clearAll() {
	var workDayCount = parseInt(document.getElementById("workDayCount").value);
	var index = parseInt(0,10);
	for ( index = 1; index <= workDayCount; index++ ) {
		document.getElementById("selectedLeaveTypeCode_" + index).selectedIndex = 0;
		document.getElementById("numberOfLeaveHours_" + index).value = "";
		document.getElementById("startTime_" + index).selectedIndex = 0;
	}
	document.getElementById("totalLeaveHours").innerHTML = "0.0";
	calcTotalLeaveHours();
	return false;
}
function insertRow(obj) {
	var objId = obj.id;
	
	var indexOf = parseInt(objId.indexOf('_'), 10);
	indexOf++;

	var theNbr = parseInt(objId.substring(indexOf), 10);
	obj.style.visibility = "hidden";
	theNbr++;

	var rowObj = document.getElementById("scheduleItem_" + theNbr);
	rowObj.style.display = '';
	return false;
}		
function collapseRow(obj) {
	var objId = obj.id;
	var indexOf = parseInt(objId.indexOf('_'), 10);
	indexOf++;
	var theNbr = parseInt(objId.substring(indexOf), 10);
	
	document.getElementById("selectedLeaveTypeCode_" + theNbr).selectedIndex = 0;		
	document.getElementById("numberOfLeaveHours_" + theNbr).value = "";
	document.getElementById("startTime_" + theNbr).selectedIndex = 0;					
	
	var rowObj = document.getElementById("scheduleItem_" + theNbr);
	rowObj.style.display = "none";
	
	theNbr--;
	document.getElementById("outputLink_" + theNbr).style.visibility = "visible";	

	calcTotalLeaveHours();
	return false;
}				
function processLeaveHours(leaveObj) {
	var errorCount = parseInt(0,10);
	var workDayCount = parseInt(document.getElementById("workDayCount").value);
	var index = parseInt(0,10);	
	var errorMsg = "";
	
	for ( index = 1; index <= workDayCount; index++ ) {
		var value = document.getElementById("numberOfLeaveHours_" + index).value;		
		if ( (value != null) && (value.length > 0) ) {
			if ( ! isFloat0to2d0to1(value) ) {
				errorMsg += "<li>'" + value + "' is not a valid leave hour value for " + document.getElementById("calendarDate_" + index).innerHTML + "</li>";
				errorCount++;
			} 
		}
	}	 
	if ( errorCount > 0 ) {
		document.getElementById("errorDiv").innerHTML = errorMsg;
		document.getElementById("errorDiv").style.display="block";
		document.getElementById("lrCreateAmendSubmitButton").disabled=true;
	} else {
		calcTotalLeaveHours();
		document.getElementById("errorDiv").innerHTML = "";
		document.getElementById("errorDiv").style.display="none";	
		document.getElementById("lrCreateAmendSubmitButton").disabled=false;
	}
}