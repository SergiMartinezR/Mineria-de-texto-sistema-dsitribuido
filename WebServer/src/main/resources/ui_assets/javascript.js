//Martinez Ramirez Sergi Alberto 4CM12
//Zárate González Erik Daniel 4CM14
//MÉNDEZ MARTÍNEZ YOSELIN ELIZABETH 4CM13

$(document).ready(function() {
  (function($){
    var search_button = $('.fa-search'),
      close_button  = $('.close'),
      input = $('.input')
      h3 = $('h3');
    var newListDiv = $('<div>').addClass('list-div');

    search_button.on('click',function(){
      $(this).parent().addClass('open');
      close_button.fadeIn(500);
      input.fadeIn(500);
      h3.fadeIn(500);
    });
  
    close_button.on('click',function(){
      search_button.parent().removeClass('open');
      close_button.fadeOut(500);
      input.fadeOut(500);
      h3.fadeOut(500);
      input.animate({top: '50%'}, 0);
      h3.animate({top: '50%'}, 0);
      newListDiv.remove();
    });

    input.on('keydown', function(events) {
      if (event.key === 'Enter') {
        var inputValue = input.val();
        $.ajax({
          method: "POST",
          contentType: "application/json",
          data: createRequest(inputValue),
          url: "procesar_datos",
          dataType: "json",
          success: onHttpResponse
        });
        console.log('Se presionó Enter');
        input.val('');
      }
    });

    function createRequest(inputValue) {
      var searchQueryTmp = inputValue;

      var frontEndRequest = {
        searchQuery: searchQueryTmp,
      };

      return JSON.stringify(frontEndRequest);
    }

    function onHttpResponse(data, status) {
      if (status === "success") {
        console.log(data);
        addResults(data);
      } else {
        alert("Error al conectarse al servidor: " + status);
      }
    }

    function addResults(data) {
      newListDiv.empty();

      var frase = data.frase;
      var coincidencias = data.coincidencias;

      console.log("coincidencias" + coincidencias)

      input.animate({top: '40%'}, 500);
      h3.animate({top: '40%'}, 500);

      newListDiv.append($('<h3>').addClass('list-title').text('Mejores coincidencias'));
      var listItems = coincidencias.split('\n');
      listItems.pop();
      var list = $('<ul>');

      listItems.forEach(function (item) {
        var listItem = $('<li>').text(item);
        list.append(listItem);
      });

      newListDiv.append(list);
      newListDiv.append($('<p>').addClass('frase-class').text('Para la frase: ' + frase));
      $('body').append(newListDiv);
    }
  })(jQuery);
});
