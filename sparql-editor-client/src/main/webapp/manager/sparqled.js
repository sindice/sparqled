// Create a summary
$(function() {
  $("#create").click(function() {
    var igraph = $("input#input-graph").val();
    var ograph = $("input#output-graph").val();
    $.ajax({
      type : "POST",
      url : "http://localhost:8080/sparqled/rest/summaries/create",
      data : "input-graph=" + igraph + "&output-graph=" + ograph,
      success : function(data) {
        $('#create-result').html("<h2>" + data.message + "</h2>");
      },
      error : function(jqXHR, exception) {
        $('#create-result').html("<h2>" + jqXHR.responseText + "</h2>");
      }
    });
    return false;
  });
});

// List available summaries
$(function() {
  $("#list").click(function() {
    $.ajax({
      type : "GET",
      url : "http://localhost:8080/sparqled/rest/summaries/list",
      success : function(data) {
        $('#list-result').html("<h2>" + data.message + "</h2>" + data.data);
      },
      error : function(data) {
        $('#list-result').html("<h2>ERROR</h2><h3>" + data.message + "</h3>");
      }
    });
    return false;
  });
});

// Select a summary
$(function() {
  $("#select").click(
      function() {
        var graph = $("input#dg").val();
        $.ajax({
          type : "PUT",
          url : "http://localhost:8080/sparqled/AssistedSparqlEditorServlet?dg=" + graph,
          success: function(data) {
            $('#select-result').html("<h2>The graph " + graph + " is currently selected.<h2>");
          },
          error : function(jqXHR, exception) {
            $('#select-result').html("<h2>" + jqXHR.responseText + "</h2>");
          }
        });
        return false;
      });
})
