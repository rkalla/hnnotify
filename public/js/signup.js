$(document).ready(function() {
	addFocusHandler($("#email"));
	addFocusHandler($("#username"));
});

function addFocusHandler(field) {
	var val = field.val();
	
	if(val == "your@email.com" || 
			val == "HN Username") {
		field.addClass("unfocused");
		field.focus(function () {
			$(this).val("");
			$(this).removeClass("unfocused");
			$(this).addClass("focused");
		});
	}
}