  $(function() {
	    $("#create").click(function() {
	      var igraph = $("input#input-graph").val();
	      var ograph = $("input#output-graph").val();
	      $.ajax({
	          type: "POST",
	          url: "http://localhost:8080/sparqled/rest/summaries/create",
	          data: "input-graph=" + igraph + "&output-graph=" + ograph,
	          success: function(data) {
	            $('#create-result').html("<h2>" + data.message + "</h2>");
	          },
	          error: function (jqXHR, exception) {
	            $('#create-result').html("<h2>" + jqXHR.responseText + "</h2>");
	          }
	        });
	        return false;
	    });
	  });

$(function() {
	    $("#list").click(function() {
	      $.ajax({
	          type: "GET",
	          url: "http://localhost:8080/sparqled/rest/summaries/list",
	          success: function(data) {
	            $('#list-result').html("<h2>" + data.message + "</h2>");
	          },
	          error: function (data) {
	        	var a = "err";
	            $('#list-result').html("<h2>ERROR</h2>");
	          }
	        });
	        return false;
	    });
	  });