function log(message) {
    var li = document.createElement('li');
    li.innerHTML = message;
    document.getElementById('messages').appendChild(li);
}

$(document).ready(function() {

    var ws;

    $("#disconnect").attr("disabled", true);

    $('#disconnect').click(function() {
        ws.close();
    });

    // Send a message when the form is submitted.
    $("#js-ajax-php-json").submit(function(event) {

        /* Stop form from submitting normally */
        event.preventDefault();

        var inputs = $(this).serializeArray();
        var url = jsRoutes.controllers.Application.socket().webSocketURL();
        ws = new WebSocket(url);

        // Show a connected message when the WebSocket is opened.
        ws.onopen = function(event) {

            var formObj = {};
            $.each(inputs, function(i, input) {
                formObj[input.name] = input.value;
            });

            var msg = "Connecting to Socket.." + event.currentTarget["url"];
            log(msg);
            ws.send(JSON.stringify(formObj));
            $("#connect").attr("disabled", true);
            $("#disconnect").attr("disabled", false);
        };


        // Show a disconnected message when the WebSocket is closed.
        ws.onclose = function(event) {
            var msg = "Disconnecting to Socket.." + event.currentTarget["url"];
            log(msg);
            $("#disconnect").attr("disabled", true);
            $("#connect").attr("disabled", false);
        };


        // Handle messages sent by the server.
        ws.onmessage = function(e) {
            log('Received: ' + e.data);
        };

        // Handle any errors that occur.
        ws.onerror = function() {
            log('Error in Connection');
        };

    });

});