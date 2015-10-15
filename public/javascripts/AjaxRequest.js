
var ajaxCall = function(serializedData) {

    var ajaxCallBack = {
        data : JSON.stringify(serializedData),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        success : onSuccess,
        error : onError
    }
    jsRoutes.controllers.Application.ajaxCall().ajax(ajaxCallBack);
};


var  onSuccess = function(response) {
    var html = '';
    for (var key in response) {
       if (response.hasOwnProperty(key)) {
           html += response[key];
       }
    }
    $('#response-container').empty().append(html);
    $('#response-container').hide().fadeIn(1000);
}

var onError = function(error) {
    alert(error);
}

$(document).ready(function() {

	$("#js-ajax-php-json").submit(function(event) {

		/* Stop form from submitting normally */
		event.preventDefault();

		$("#js-ajax-php-json :submit").prop("disabled", true);

		setTimeout(function() {
	        $('#js-ajax-php-json :submit').prop('disabled', false);
	    }, 5000);

		var inputs = $(this).serializeArray();
		var formObj = {};
		$.each(inputs, function(i, input) {
			formObj[input.name] = input.value;
		});

        ajaxCall(formObj);
	});

});