//Martinez Ramirez Sergi Alberto 4CM12
//Zárate González Erik Daniel 4CM14
//MÉNDEZ MARTÍNEZ YOSELIN ELIZABETH 4CM13

$( document ).ready(function() {
    console.log( "ready!" );

    var button = $("#submit_button");   
    var searchBox = $("#search_text"); 
    var resultsTable = $("#results table tbody"); 
    var resultsWrapper = $("#results"); 

    button.on("click", function(){

        $.ajax({
          method : "POST",
          contentType: "application/json",
          data: createRequest(),
          url: "procesar_datos",
          dataType: "json",
          success: onHttpResponse
          });
      });

    function createRequest() {
        var searchQueryTmp = searchBox.val();

        var frontEndRequest = {
            searchQuery: searchQueryTmp,
        };
        
        return JSON.stringify(frontEndRequest);
    }

    function onHttpResponse(data, status) {
        if (status === "success" ) {
            console.log(data);
            addResults(data);
        } else {
            alert("Error al conectarse al servidor: " + status);
        }
    }

    function addResults(data) {
        resultsTable.empty();

        var frase = data.frase;
        var coincidencias = data.coincidencias;
        $("#frase").text(frase);
        $("#coincidencias").text(coincidencias);

        resultsWrapper.show();
    }
});

