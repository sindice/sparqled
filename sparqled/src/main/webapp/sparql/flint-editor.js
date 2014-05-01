/*******************************************************************************
 * Copyright (c) 2012 National University of Ireland, Galway. All Rights Reserved.
 *
 *
 * This project is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * This software is based on flint-editor v0.5
 * <http://openuplabs.tso.co.uk/demos/sparqleditor>
 *
 *
 * This software has been improved by:
 * Thomas Perry <thomas.perry@deri.org>
 * Pierre Bailly <pierre.bailly@deri.org> (Actual developper)
 */
// Main flint editor class
function FlintEditor(container, imagesPath, config) {

    if (config.endpoints == null) {
        alert("There must be at least one endpoint defined");
        return;
    }

    var editor = new Flint(container, imagesPath, config);
    this.getEditor = function() {return editor}

    // var global
    var g_isInAutocompletionPanel = true;
    function Flint(container, imagesPath, config) {
        var flint = this;
        var editorTitle = "SindiceTech SparQLed";
        var editorId = "flint-editor";
        var codeId = 'flint-code';
        var initialQuery = "PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;\n\nSELECT * WHERE {\n   ?s ?p ?o\n}\nLIMIT 10"
        var clearError = function() {};
        var markerHandle = null;
        var showResultsInSitu = true;
        var cm;
        this.windowClosing = false;

        // don't display dialogs when navigating away from page
        $(window).bind('beforeunload', function(event) {
            editor.windowClosing = true;
        });

        this.allKeywords=
            [
            "BASE",
            "PREFIX",
            "SELECT",
            "ASK",
            "CONSTRUCT",
            "DESCRIBE",
            "DISTINCT",
            "REDUCED",
            "FROM",
            "NAMED",
            "WHERE",
            "GRAPH",
            "UNION",
            "FILTER",
            "OPTIONAL",
            "ORDER",
            "LIMIT",
            "OFFSET",
            "BY",
            "ASC",
            "DESC",
            "STR",
            "LANG",
            "LANGMATCHES",
            "DATATYPE",
            "BOUND",
            "SAMETERM",
            "ISIRI",
            "ISURI",
            "ISBLANK",
            "ISLITERAL",
            "REGEX"]

        try {
            // Path to images directory
            this.getImagesPath = function() {return imagesPath};

            // Returns the version of the software
            this.getVersion = function() {
                return "0.9"
            };

            // Returns the title of the software
            this.getTitle = function() {
                if(config.editorTitle){
                    return config.editorTitle;
                }
                return editorTitle;
            };

            if ($.browser.msie) {
                $('#' + container).append("<form id='" + editorId + "' action='" + config.endpoints[0].uri + "' method='post'></form>");
            } else {
                $('#' + container).append("<div id='" + editorId + "'></div>");
            }

            $('#' + editorId).append("<h1 id='flint-title'>" + this.getTitle() + " - " + this.getVersion() + "</h1>");

            // Add menu
            if (config.interface.menu) {
                var createMenu = new FlintMenu(flint);
                createMenu.display(editorId);
                this.getMenu = function() {return createMenu};
            }

            // Add toolbar
            if (config.interface.toolbar) {
                var createToolbar = new FlintToolbar(flint);
                createToolbar.display(editorId);
                this.getToolbar = function() {return createToolbar};
            }

            // Add endpoint bar
            try {
                var createEndpointBar = new FlintEndpointBar(config, flint);
                createEndpointBar.display(editorId);
                this.getEndpointBar = function() {return createEndpointBar};

                var endpointItem = createEndpointBar.getItems()[0];
                //var endpointGetInfoButton = createEndpointBar.getItems()[2];
                //var endpointMimeTypeItem = createEndpointBar.getItems()[3];
                var endpointMimeTypeItem = createEndpointBar.getItems()[2];
                endpointMimeTypeItem.setDisableElements("SELECT");

                // Add errorbox - this reuses the standard Flint dialog
                var errorBox = new FlintError(flint);
                this.getErrorBox = function() {return errorBox}
            } catch (e) {errorBox.show(e);}

            // Add coolbar
            var createCoolbar = new FlintCoolbar(config, flint);
            createCoolbar.display(editorId);
            this.getCoolbar = function() {return createCoolbar};

            // Add sidebar
            var createSidebar = new FlintSidebar(flint, config);
            createSidebar.display(editorId);
            createSidebar.showActiveTab();

            // Get a handle to the dataset item
            var datasetItems = createCoolbar.getItems()[0];
            datasetItems.setChangeAction(function() {
                if ($.browser.msie) {
                    $('#' + editorId).attr('action', datasetItems.getEndpoint());
                } else {
                    createSidebar.updateProperties(endpointItem.getDomain(),datasetItems.getItem(datasetItems.getEndpoint()));
                    createSidebar.updateClasses(endpointItem.getDomain(), datasetItems.getItem(datasetItems.getEndpoint()));
                    createSidebar.updateSamples(datasetItems.getItem(datasetItems.getEndpoint()));
                }
            });

            // Get a handle to the formats bar
            var datasetMimeTypeItem = createCoolbar.getItems()[2];
            datasetMimeTypeItem.setDisableElements("SELECT");

            // Add about box
            var aboutBox = new FlintAbout(flint);

            // Add confirmation dialog
            var confirmDialog = new FlintDialog();
            confirmDialog.display(editorId);
            this.getConfirmDialog = function() {return confirmDialog}

            // Add results area
            if (!$.browser.msie) {
              var resultsArea = new FlintResults(flint);
              resultsArea.showLoading(false);
            }

            // Get a handle to the submit button
            var submitItemCoolbar = createCoolbar.getItems()[1];
            var submitItemEndpointBar = createEndpointBar.getItems()[1];

            createCoolbar.hide();
            createEndpointBar.show();

            // Physically set for now but we want this in the configuration so users can override and provide custom submissions
            this.sendDatasetQuery = function() {
                try {
                    if (!$.browser.msie) {
                        resultsArea.setResults("","no-format");
                        resultsArea.showLoading(true);
                    }
                    // Collect query parameters
                    var paramsData = {};
                    paramsData[config.defaultEndpointParameters.queryParameters.query] = cm.getValue();

                    var mimeType = datasetMimeTypeItem.getMimeType();
                    $.ajax({
                    url: datasetItems.getEndpoint(),
                    type: 'post',
                      //data: paramsData,
                    data: "&query="+encodeURIComponent(paramsData[config.defaultEndpointParameters.queryParameters.query])+"&format=application%2Fsparql-results%2Bjson",
                    headers: {"Accept": "application/sparql-results-json"},
                      //headers: {"Accept": mimeType},
                      //dataType: 'text',
                    crossDomain: true,
                    dataType: 'jsonp',
                    processData: false,
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        if (XMLHttpRequest.status == 0) errorBox.show(textStatus+": The request was not sent. You may be offline");
                        else errorBox.show("Dataset Request: HTTP Status: " + XMLHttpRequest.status + "; " + textStatus);
                        resultsArea.showLoading(false);
                    },
                    success: function(data) {resultsArea.setResults(JSON.stringify(data),"no-format");}
                    });
                } catch (e) {errorBox.show(e);}
            }

            this.sendEndpointQuery = function() {
                try {
                    if (!$.browser.msie) {
                    resultsArea.setResults("","no-format");
                    resultsArea.showLoading(true);
                    }
                    // Collect query parameters
                    var paramsData = {};
                    paramsData[config.defaultEndpointParameters.queryParameters.query] = cm.getValue();
                    var mimeType = endpointMimeTypeItem.getMimeType();
                    $.ajax({
                    url: config.endpoints[1].uri,
                    type: 'post',

                    data: "&query="+encodeURIComponent(paramsData[config.defaultEndpointParameters.queryParameters.query])+"&format=application%2Fsparql-results%2Bjson",
                    headers: {"Accept": "application/sparql-results-json"}, // force to use json because we call cross-site directly as sparql endpoint
                    crossDomain: true,
                    dataType: 'jsonp',
                    processData: false,
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        if (XMLHttpRequest.status == 0) errorBox.show("The request was not sent. You may be offline.");
                        else errorBox.show("Endpoint Request: HTTP Status: " + XMLHttpRequest.status + "; " + textStatus);
                        resultsArea.showLoading(true);
                    },
                    success: function(data) {
                      resultsArea.setResults(data,mimeType);}
                    });
                }
                catch (e) {errorBox.show(e);}
            }

            this.sendIEDatasetQuery = function() {
                $("#" + editorId).attr('action', datasetItems.getEndpoint());
            }

            this.sendIEEndpointQuery = function() {
                $("#" + editorId).attr('action', endpointItem.getEndpoint());
            }

            if (!$.browser.msie) {
                submitItemCoolbar.setSubmitAction(this.sendDatasetQuery);
                submitItemEndpointBar.setSubmitAction(this.sendEndpointQuery);
            } else {
                submitItemCoolbar.setSubmitAction(this.sendIEDatasetQuery);
                submitItemEndpointBar.setSubmitAction(this.sendIEEndpointQuery);
            }


            //----------------------------------------------------------------
            // Autocompletion code, based on the example for javascript

            function autocompleteKeyEventHandler(i, e) {
                // Hook into ctrl-space
                if (e.keyCode == 32 && (e.ctrlKey || e.metaKey) && !e.altKey) {
                    e.stop();
                    return startComplete();
                }
            }

            function stopEvent() {
                if (this.preventDefault) {
                    this.preventDefault();
                    this.stopPropagation();
                } else {
                    this.returnValue = false;
                    this.cancelBubble = true;
                }
            }
            function addStop(event) {
                if (!event.stop) event.stop = stopEvent;
                return event;
            }
        function connect(node, type, handler) {
            function wrapHandler(event) {handler(addStop(event || window.event));}
            if (typeof node.addEventListener == "function")
                node.addEventListener(type, wrapHandler, false);
            else
                node.attachEvent("on" + type, wrapHandler);
            }

            function forEach(arr, f) {
                for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
            }

            function memberChk(el,arr) {
                for (var i = 0, e = arr.length; i < e; ++i)
                    if (arr[i]==el) return(true);
                return false;
            }

            // Extract context info needed for autocompletion / keyword buttons
            // based on cursor position
            function getPossiblesAtCursor() {
                // We want a single cursor position.
                if (cm.somethingSelected()) return;
                // Find the token at the cursor
                var cur = cm.getCursor(false);
                var cur1={ line: cur.line, ch: cur.ch };

                // Before cursor
                var charBefore= cm.getRange({line: cur.line, ch: cur.ch-1},
                                {line: cur.line, ch: cur.ch  });

                // Cursor position on the far left (ch=0) is problematic
                // - if we ask CodeMirror for token at this position, we don't
                // get back the token at the beginning of the line
                // - hence use adjusted position cur1 to recover this token.
                if (cur1.ch==0 && cm.lineInfo(cur1.line).text.length>0)
                    cur1.ch=1;
                var token = cm.getTokenAt(cur1);
                var charAfter;
                var possibles, insertPos=null;

                var start= token.string.toLowerCase();
                var insertPos=null;
                var insertEnd=false;
                var insertStart=false;

                // if the token is whitespace, use empty string for matching
                //  and set insertPos, so that selection will be inserted into
                //  into space, rather than replacing it.
                if (token.className=="sp-ws") {
                    start="";
                    // charAfter is char after cursor
                    charAfter= cm.getRange({line: cur.line, ch: cur.ch },
                               {line: cur.line, ch: cur.ch+1 });
                    insertPos= cur;
                } else {
                    // charAfter is char after end of token
                    charAfter= cm.getRange({line: cur.line, ch: token.end },
                               {line: cur.line, ch: token.end+1 });
                    if (token.string !== "a" && token.className!="sp-invalid" &&
                    token.className!="sp-prefixed" &&
                    (token.string!="<" || !memberChk("IRI_REF",token.state.possibleCurrent))
                    // OK when "<" is start of URI
                       ) {
                        if (token.end==cur.ch && token.end!=0) {
                            insertEnd=true;
                            start="";
                            insertPos=cur;
                        } else if (token.start==cur.ch) {
                            insertStart=true;
                            start="";
                            insertPos=cur;
                        }
                    }
                }

                if (token.className=="sp-comment")
                    possibles=[];
                else if (cur1.ch > 0 && !insertEnd) {
                    possibles= token.state.possibleCurrent;
                } else {
                    possibles= token.state.possibleNext;
                }

                return {"token": token,              // codemirror token object
                    "possibles": possibles,      // array of possibles terminals from grammar
                    "insertPos": insertPos,      // Position in line to insert text, or null if replacing existing text
                    "insertStart": insertStart,  // true if position of insert adjacent to start of a non-ws token
                    "insertEnd": insertEnd,      // true if ...                        ... end of a ...
                    "charAfter": charAfter,      // char found straight after cursor
                    "cur": cur,                  // codemirror {line,ch} object giving pos of cursor
                    "start": start               // Start of token for autocompletion
                       }
            }

        var start2 = function startComplete2(completions) {
            if (!completions.length) return;

            var tkposs = getPossiblesAtCursor();
            if (completions.length == 1) {
                // Replace automatically
                if (completions[0].value !== "a") {
                    // TODO Correct SPARQL PARSER.
                    // this part is a trick to remove unwanted "a" token for a recommandation for "abc"
                    // Flint parser is invalid, but I cannot achieve to fix it correctly
                    var tempCursor = {ch: tkposs.token.start, line:tkposs.cur.line};
                    if (cm.getTokenAt(tempCursor).string === "a") {
                        tkposs.token.string = "a" + tkposs.token.string;
                        tkposs.token.start -= 1;
                        tkposs.start = "a" + tkposs.start;
                    }
                    // End of trick

                    flint.insertOrReplace(completions[0].value,tkposs);
                }
                return;
            }

        // When there is only one completion, use it directly.

        // Build the select widget
        var complete = document.createElement("div");
        complete.className = "completions";
        var sel = complete.appendChild(document.createElement("select"));
        sel.multiple = true;
        for (var i = 0; i < completions.length; ++i) {
            var opt = sel.appendChild(document.createElement("option"));
            opt.appendChild(document.createTextNode(completions[i].value));
        }
        sel.firstChild.selected = true;
        sel.size = Math.min(10, completions.length);
        var pos = cm.cursorCoords();

        complete.style.position= "absolute";
        complete.style.left = pos.x + "px";
        complete.style.top = pos.yBot + "px";

        document.body.appendChild(complete);

        var contextPos = cm.cursorCoords();

        // Hack to hide the scrollbar.
        if (completions.length <= 10)
            complete.style.width = (sel.clientWidth - 1) + "px";

        function showContext(){
            if(completions[sel.selectedIndex].class_attributes != null  && completions[sel.selectedIndex].class_attributes.length >0 &&
               completions[sel.selectedIndex].ca_replace){

            var ca_substitution = completions[sel.selectedIndex].ca_substitution;
            var cursorStart = ca_substitution.start;
            // TODO handle the -1 in java side
            cursorStart.ch -= 1;
            var cursorEnd = ca_substitution.end;
            var tokenAct = cm.getTokenAt(cursorEnd);

            if ((tokenAct.string !== "a"
                 || ((ca_substitution.value !== "a")
                 && (ca_substitution.value !== "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))
                && (tokenAct.string !== "<" + ca_substitution.value + ">")) {
                editor.getErrorBox().show("Invalid token");
            }
            else {
                if (completions[sel.selectedIndex].class_attributes.length > 1) {
                var context = document.createElement("div");
                context.className = "context-info";
                var contextSel = context.appendChild(document.createElement("select"));
                contextSel.multiple = true;
                for (var i = 0; i < completions[sel.selectedIndex].class_attributes.length; ++i) {
                    var contextOption = contextSel.appendChild(document.createElement("option"));
                    contextOption.appendChild(document.createTextNode(completions[sel.selectedIndex].class_attributes[i].value + "  " +completions[sel.selectedIndex].class_attributes[i].count));
                }
                contextSel.firstChild.selected = true;
                contextSel.size = Math.min(10, completions[sel.selectedIndex].class_attributes.length);


                context.style.position= "absolute";
                context.style.left = contextPos.x + "px";
                context.style.top = contextPos.yBot + "px";

                document.body.appendChild(context);

                var contextDone = false;
                function contextClose() {
                    //console.log("blur called");
                    if (contextDone) return;
                    contextDone = true;
                    context.parentNode.removeChild(context);
                }
                function contextPick() {
                    var toks = {value: tokenAct.string,
                        line: cursorStart.line,
                        start: cursorStart.ch,
                        end: cursorEnd.ch};
                    // TODO Improve
                    cm.replaceRange(
                    "<"+completions[sel.selectedIndex].class_attributes[contextSel.selectedIndex].value+">",
                    {line: toks.line, ch: toks.start},
                    {line: toks.line, ch: toks.end});

                    //console.log("pick called");
                    if (contextDone) return;
                    contextDone = true;
                    context.parentNode.removeChild(context);

                    setTimeout(function(){cm.focus();}, 50);
                }
                function contextFocus(){
                    //console.log("focus called");
                }

                connect(contextSel, "focus", contextFocus);

                connect(contextSel, "blur", contextClose);
                connect(contextSel, "keydown", function(event) {
                    var code = event.keyCode;
                    // Enter and space
                    if ((!event.ctrlKey) && (code == 13 || code == 32)) {
                    g_isInAutocompletionPanel = true;
                    event.stop();
                    contextPick();
                    }
                    // Escape
                    else if (code == 27) {
                    g_isInAutocompletionPanel = true;
                    event.stop();
                    contextClose();
                    cm.focus();
                    }
                    else if ((code == 32) && (event.ctrlKey || event.metaKey) && (!event.altKey)) {
                    contextClose();
                    cm.focus();
                    }
                    else if (!event.ctrlKey && !event.metaKey && code != 38 && code != 40) {
                    g_isInAutocompletionPanel = true;
                    close();
                    cm.focus();
                    }
                });
                connect(contextSel, "dblclick", contextPick);
                setTimeout(function(){contextSel.focus();}, 50);
                }else {
                // one element, juste pick it, if needed
                if ((tokenAct.string !== "a"
                     || (completions[sel.selectedIndex].class_attributes[0].value
                     !== "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    && (tokenAct.string
                    !== "<" + completions[sel.selectedIndex].class_attributes[0].value + ">")) {
                    cm.replaceRange(
                    "<" + completions[sel.selectedIndex].class_attributes[0].value + ">",
                    cursorStart,
                    cursorEnd);
                }
                }
            }

            }
        }

        var done = false;
        function close() {
            if (done) return;
            done = true;
            complete.parentNode.removeChild(complete);
        }
        function pick() {
            // TODO Correct SPARQL PARSER.
            // this part is a trick to remove unwanted "a" token for a recommandation for "abc"
            // Flint parser is invalid, but I cannot achieve to fix it correctly
            var tempCursor = {ch: tkposs.token.start, line:tkposs.cur.line};
            if ((tkposs.token.string.indexOf(" ") == -1)
            && (cm.getTokenAt(tempCursor).string === "a")) {
            tkposs.token.string = "a" + tkposs.token.string;
            tkposs.token.start -= 1;
            tkposs.start = "a" + tkposs.start;
            }
            // End of trick

            flint.insertOrReplace(sel.options[sel.selectedIndex].value,tkposs);
            close();
            //thoper commented this out because the context information did not appear with it.
            setTimeout(function(){cm.focus();}, 40);
            showContext();

        }
        function focus2(){
            //console.log("focus 1 called");
        }

        connect(sel, "focus", focus2);

        connect(sel, "blur", close);
        connect(sel, "keydown", function(event) {
            var code = event.keyCode;

            // Enter and  space
            if ((!event.ctrlKey) && (code == 13 || code == 32)) {
            g_isInAutocompletionPanel = true;
            event.stop();
            pick();
            }
            // Escape
            else if (code == 27) {
            g_isInAutocompletionPanel = true;
            event.stop();
            close();
            cm.focus();
            }
            else if ((code == 32) && (event.ctrlKey || event.metaKey) && (!event.altKey)) {
            close(); cm.focus();
            setTimeout(startComplete, 50);
            }
            else if (!event.ctrlKey && !event.metaKey && code != 38 && code != 40) {
            g_isInAutocompletionPanel = true;
            close();
            cm.focus();
            }
        });
        connect(sel, "dblclick", pick);


        sel.focus();
        // Opera sometimes ignores focusing a freshly created node
        if (window.opera) setTimeout(function(){if (!done) sel.focus();}, 100);
        return true;
        }

        //Called From autocompleteKeyEventHandler
        function startComplete() {
        statusArea.setQueryResponse("none");
        statusArea.updateStatus();
        // We want a single cursor position.
        if (cm.somethingSelected()) return;

        //Get the possible completions for current cursor position
        var tkposs= getPossiblesAtCursor();
        var stack=tkposs.token.state.stack;

        var completions= getCompletions(tkposs.token, tkposs.start, tkposs.possibles, start2);
        }

        var allKeywords = this.allKeywords;

        // FIXME: This is duplicated from elsewhere, and it shouldn't be!
        var keywords= /^(BASE|PREFIX|SELECT|CONSTRUCT|DESCRIBE|ASK|FROM|NAMED|ORDER|BY|LIMIT|ASC|DESC|OFFSET|DISTINCT|REDUCED|WHERE|GRAPH|OPTIONAL|UNION|FILTER|STR|LANG|LANGMATCHES|DATATYPE|BOUND|SAMETERM|ISIRI|ISURI|ISBLANK|ISLITERAL|REGEX|TRUE|FALSE)$/i;
        // Punctuation omits "a" and "<"
        // - because we might want to autocomplete a URI
        var punct= /^(\*|\.|\{|\}|,|\(|\)|;|\[|\]|\|\||&&|=|!=|!|<=|>=|>|\+|-|\/|\^\^)$/

        function getCompletions(token,start,possibles, startFunction2) {
        var found = [];
        // test the case of the 1st non-space char
        var startIsLowerCase= /^ *[a-z]/.test(token.string);

        // Where case is flexible
        function maybeAdd(str) {
            if (str.toUpperCase().indexOf(start.toUpperCase()) == 0) {
            if (startIsLowerCase){
                str = str.toLowerCase();
            }else{
                str = str.toUpperCase();
            }

            var t = {"value": str,
                 "class_attributes": null };
            found.push(t);
            }
        }

        // Where case is not flexible
        function maybeAddCS(str) {
            maybeAddCS(str, false);
        }

        function maybeAddCS(str, ignore) {
            maybeAddCS(str,ignore, null, false, null)
        }

        function maybeAddCS(str, ignore, class_attributes, ca_replace, ca_substitution) {
            if (str.toUpperCase().indexOf(start.toUpperCase())==0  || ignore){
            var newElement = true;
            for (var i = 0; i < found.length; ++i) {
                if (found[i].value === str) {
                newElement = false;
                }
            }
            if (newElement) {
                var tmp = {"value": str,
                       "class_attributes": class_attributes,
                       "ca_replace": ca_replace,
                       "ca_substitution": ca_substitution};
                found.push(tmp);
            }
            }
        }

        function clearCS() {
            found = [];
        }


            function gatherCompletions(startFunction2) {
            var uriNoWait = true;

            for (var i=0; i<possibles.length; ++i)
            {
            if (possibles[i]=="VAR1")
                maybeAddCS("?");
            else if (keywords.exec(possibles[i]))
            {
                //  keywords - the strings stand for themselves
                maybeAdd(possibles[i]);
            }
            else if (punct.exec(possibles[i]))
            {
                //  punctuation - the strings stand for themselves
                maybeAddCS(possibles[i]);
            }
            else if (possibles[i]=="STRING_LITERAL1") {
                maybeAddCS('"');
                maybeAddCS("'");
            }
            else if (possibles[i]=="IRI_REF" ) {
                maybeAddCS("<");

                if (g_isInAutocompletionPanel) {
                // autocompletion => clear CS, add the new CS
                clearCS();
                uriNoWait=false;
                var q = cm.getValue();
                if (/^\"/.test(start)) {
                    var cur = cm.getCursor(false);
                    cur.ch = cur.ch - 1;
                    var begin = cm.getRange({line: 0, ch:0}, cur);
                    q = begin + "< " + q.substring(begin.length + 1, q.length);
                }else if (!/^</.test(start)) {
                    var cur = cm.getCursor(false);
                    var begin = cm.getRange({line: 0, ch:0}, cur);
                    q = begin + "< " + q.substring(begin.length, q.length);
                }

                statusArea.setQueryResponse("posted request");
                statusArea.updateStatus();

                $.ajax({
                    url: config.endpoints[0].uri,
                    type: 'post',
                    data: "data=autocomplete&query="+encodeURIComponent(q),
                    headers: {"Accept": "application/sparql-results-json"},
                    crossDomain: true,
                    dataType: 'jsonp',
                    success: function(data) {
                    statusArea.setQueryResponse(data.status);
                    statusArea.updateStatus();

                    if (data.status.toUpperCase().indexOf("SUCCESS") == 0) {
                        // get the bindings from rankings list
                        var rankingFound = false;
                        var bindings = data.results.bindings;
                        for (var j = 0; j < bindings.length;  j++) {
                            if ((typeof bindings[j].status !== "undefined")
                            && (bindings[j].status === "URI")) {
                            // URI
                            maybeAddCS("<" + bindings[j].value + ">",
                                   true,
                                   bindings[j].class_attributes,
                                   data.ca_replace,
                                   data.ca_substitution);
                            } else if (bindings[j].status === "QNAME") {
                            // QNAME
                            maybeAddCS(start + bindings[j].value,
                                   true,
                                   bindings[j].class_attributes,
                                   data.ca_replace,
                                   data.ca_substitution);
                            } else {
                            // Litteral
                            maybeAddCS("\"" + bindings[j].value + "\"",
                                   true,
                                   bindings[j].class_attributes,
                                   data.ca_replace,
                                   data.ca_substitution);
                            }
                        }
                        startFunction2(found);
                    } else {
                        // Error
                        resultsArea.showLoading(false);
                        console.log(data.message);
                        $('#flint-results').show();
                        $('#flint-results').val(data.message);
                        $('#formatted-results').hide();
                        $('#formatted-results').html("");

                    }
                    }
                });
                g_isInAutocompletionPanel = !g_isInAutocompletionPanel;
                // we don't want other completion
                break;
                }
                else {
                // URI => Add some selector
                maybeAddCS("a", true, null);
                g_isInAutocompletionPanel = !g_isInAutocompletionPanel;
                }
            }
            else if (possibles[i]=="BLANK_NODE_LABEL") {
                maybeAddCS("_:");
            }
            else if (possibles[i]=="a") {
                // Property expected at cursor position - fetch possibilities
                maybeAddCS("a");
                if (/:/.test(start)) {
                // Prefix has been entered - give matching prefixed properties
                var activeDataItem= createSidebar.getActiveDataItem();
                if (activeDataItem) {
                    for (var k = 0; k < activeDataItem.prefixes.length; k++) {
                    var ns= activeDataItem.prefixes[k].uri;
                    for (var j = 0; j < activeDataItem.properties.results.bindings.length; j++) {
                        var fragments=
                        activeDataItem.properties.results.bindings[j].value
                        .match(/(^\S*[#\/])([^#\/]*$)/);
                        if (fragments.length==3 && fragments[1]==ns)
                        maybeAddCS(activeDataItem.prefixes[k].prefix+":"+fragments[2]);
                    }
                    }
                }
                }
            }
            else if (possibles[i]=="PNAME_LN" && !/:$/.test(start)) {
                // prefixed names - offer prefixes
                /*var activeDataItem= createSidebar.getActiveDataItem();
                  if (activeDataItem && activeDataItem.prefixes.length) {
                  for (var j = 0; j < activeDataItem.prefixes.length; j++) {
                  maybeAddCS(activeDataItem.prefixes[j].prefix+":");
                  }
                  }*/
            }
            }
            if(uriNoWait){
            startFunction2(found);
            }
             }
        gatherCompletions(startFunction2);
        return found;
        }
        // End of autocompletion code

        var cmUpdate = function() {
        if (cm != undefined) {
            var queryValid=true;
            if (clearError!=null) { clearError(); clearError=null };
            if (markerHandle != null) cm.clearMarker(markerHandle);
            var state;
            for (var l=0; l<cm.lineCount(); ++l) {
            state= cm.getTokenAt({line:l, ch: cm.getLine(l).length}).state;
            if (state.OK==false) {
                markerHandle=
                cm.setMarker(l,
                         "<span style=\"color: #f00 ; font-size: large;\">&rarr;</span> %N%");
                clearError = cm.markText(
                {line:l, ch:state.errorStartPos},
                {line:l, ch:state.errorEndPos},
                "sp-error");
                queryValid=false;
                break;
            }
            }
            var stack=state.stack, len=state.stack.length;
            // Because incremental parser doesn't receive end-of-input
            // it can't clear stack, so we have to check that whatever
            // is left on the stack is nillable
            if (len>5) queryValid=false;
            else {
            var states=["bindingsClause","solutionModifier", "?limitoffsetClauses","?limitOffsetClauses","?offsetClause",
                    "?orderClause","*orderCondition","?havingClause", "*havingCondition","*groupCondition"];
            for (i=0;i<len;i++)
            {
                if (states.indexOf(stack[i]) < 0){
                    queryValid=false;
                }
            }
            }

            if (queryValid) {
            submitItemCoolbar.enable();
            submitItemEndpointBar.enable();
            datasetMimeTypeItem.setDisableElements(state.queryType);
            endpointMimeTypeItem.setDisableElements(state.queryType);
            statusArea.setQueryValid(true);
            statusArea.updateStatus();
            } else {
            submitItemCoolbar.disable();
            submitItemEndpointBar.disable();
            datasetMimeTypeItem.setDisableElements(state.queryType);
            endpointMimeTypeItem.setDisableElements(state.queryType);
            statusArea.setQueryValid(false);
            statusArea.updateStatus();
            }
        }
        }

        // Enable/disable the keyword buttons depending on the possibilities at cursor position
        var updateKeywordTable= function() {

        var tkposs = getPossiblesAtCursor();
        if (tkposs!=undefined)
        {
            function getButtonFn(str,tkposs)
            { return function(e) {
            flint.insertOrReplace(str,tkposs);
            cm.focus();
            e.stopPropagation(); }
            };
            var token = tkposs.token;
            var possibles = tkposs.possibles;
            for( var i=0; i<flint.allKeywords.length; ++i) {
            var enabled=false;
            var keyword= flint.allKeywords[i];
            for( var j=0; j<possibles.length && !enabled; ++j) {
                if (keyword==possibles[j])
                enabled=true;
            }
            var button= $('#flint-keyword-'+keyword+'-button');
            if (enabled) {
                button.attr("disabled",false);
                button.unbind("click");
                button.click(getButtonFn(keyword,tkposs));
            } else {
                button.attr("disabled",true);
            }
            }
        }
        }

        var cmCursor = function() {
        updateKeywordTable();
        if (cm != undefined) {
            // Activate cut?
            if (cm.somethingSelected() != "") {
            createToolbar.setEnabled("Cut", true);
            createMenu.setEnabled("Cut", true);
            }
            else {
            createToolbar.setEnabled("Cut", false);
            createMenu.setEnabled("Cut", false);
            }
            if (cm.historySize().undo > 0) {
            createToolbar.setEnabled("Undo", true);
            createMenu.setEnabled("Undo", true)
            }
            else {
            createToolbar.setEnabled("Undo", false);
            createMenu.setEnabled("Undo", false);
            }
            if (cm.historySize().redo > 0) {
            createToolbar.setEnabled("Redo", true);
            createMenu.setEnabled("Redo", true);
            }
            else {
            createToolbar.setEnabled("Redo", false);
            createMenu.setEnabled("Redo", false);
            }

            statusArea.setLine(cm.getCursor().line);
            statusArea.setPosition(cm.getCursor().ch);
            statusArea.updateStatus();
        }
        }

        // Add actual code editing area
        $('#' + editorId).append("<textarea id='" + codeId + "' name='query' cols='100' rows='1'>" + initialQuery + "</textarea>");
        var cm =CodeMirror.fromTextArea(document.getElementById("flint-code"), {
        mode: "sparql",
        //      workDelay: 50,
        //      workTime: 100,
        lineNumbers: true,
        indentUnit: 3,
        tabMode: "indent",
        matchBrackets: true,
        //      onChange: cmUpdate,
        onHighlightComplete: cmUpdate,
        onCursorActivity: cmCursor,
        onKeyEvent: autocompleteKeyEventHandler
        });

        this.getCodeEditor = function() {return cm};

        // Add status area
        var statusArea = new FlintStatus();
        statusArea.display(editorId);
        statusArea.updateStatus();

        // Clear the editor area
        this.clearEditorTextArea = function() {
        if (cm.getValue() != "") {
            confirmDialog.setCloseAction(function() {
            var result = confirmDialog.getResult();
            if (result == "Okay") cm.setValue("");
            });
            confirmDialog.show("New Query", "<p>Are you sure you want to abandon the current text?</p>");
        }
        }

        this.undo = function() {cm.undo();}

        this.redo = function() {cm.redo();}

        this.cut = function() {
        cm.replaceSelection("");
        cm.focus();
        }

        this.insert = function(text) {
        cm.replaceSelection(text);
        cm.focus();
        }

        this.insertOrReplace= function(str,tkposs) {
        if ((tkposs.insertStart || tkposs.charAfter!=" ") && /^[A-Za-z\*]*$/.exec(str) ) str=str+" ";
        if (tkposs.insertEnd) str=" "+str;
        if (tkposs.insertPos) {
            // Insert between spaces
            cm.replaceRange(str,tkposs.insertPos);
        } else {
            // Replace existing token
            cm.replaceRange(
            str,
            {line: tkposs.cur.line, ch: tkposs.token.start},
            {line: tkposs.cur.line, ch: tkposs.token.end});
        }
        }


        this.toggleTools = function() {$('#flint-sidebar-grabber').click();}

        this.showEndpointBar = function() {
        createCoolbar.hide();
        createEndpointBar.show();
        createToolbar.setEnabled("Show Endpoints", false);
        createToolbar.setEnabled("Show Datasets", true);
        createMenu.setEnabled("Show Endpoints", false);
        createMenu.setEnabled("Show Datasets", true);
        createSidebar.clearActiveItem();
        createSidebar.showActiveTab();
        }

        this.showDataSetsBar = function() {
            createCoolbar.show();
        createEndpointBar.hide();
        createToolbar.setEnabled("Show Endpoints", true);
        createToolbar.setEnabled("Show Datasets", false);
        createMenu.setEnabled("Show Endpoints", true);
        createMenu.setEnabled("Show Datasets", false);
        createSidebar.updateProperties(endpointItem.getDomain(), datasetItems.getItem(datasetItems.getEndpoint()));
        createSidebar.updateClasses(endpointItem.getDomain(), datasetItems.getItem(datasetItems.getEndpoint()));
        createSidebar.updateSamples(datasetItems.getItem(datasetItems.getEndpoint()));
        }

        this.insertSelectQuery = function() {
        if (cm.getValue() != "") {
            confirmDialog.setCloseAction(function() {
            var result = confirmDialog.getResult();
            if (result == "Okay") cm.setValue(createSidebar.getPrefixes()  + "\nSELECT * WHERE {?s ?p ?o}\nLIMIT 10");
            });
            confirmDialog.show("New Select Query", "<p>Are you sure you want to abandon the current text?</p>");
        }
        else cm.setValue(createSidebar.getPrefixes()  + "\nSELECT * WHERE {?s ?p ?o}\nLIMIT 10");
        }

        this.insertConstructQuery = function() {
        if (cm.getValue() != "") {
            confirmDialog.setCloseAction(function() {
            var result = confirmDialog.getResult();
            if (result == "Okay") cm.setValue(createSidebar.getPrefixes()  + "\nCONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}\nLIMIT 10");
            });
            confirmDialog.show("New Construct Query", "<p>Are you sure you want to abandon the current text?</p>");
        }
        else cm.setValue(createSidebar.getPrefixes()  + "\nCONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}\nLIMIT 10");
        }

        this.showAbout = function() {
        aboutBox.show();
        }

        if (!$.browser.msie) {
        resultsArea.display(editorId);
        try {
            createSidebar.updateProperties(endpointItem.getDomain(), datasetItems.getItem(datasetItems.getEndpoint()));
            createSidebar.updateClasses(endpointItem.getDomain(), datasetItems.getItem(datasetItems.getEndpoint()));
            createSidebar.updateSamples(datasetItems.getItem(datasetItems.getEndpoint()));

        }
        catch (e) {errorBox.show(e);}
        }

        resultsArea.showLoading(false);
    }
    catch (e) {alert(e);}
    }

    function FlintError(editor) {

    this.show = function(message) {
        try {

        editor.getConfirmDialog().setCloseAction();
        editor.getConfirmDialog().show("SparQLed Error", "<p>" + message.toString() + "</p>", true);
        }
        catch (e) {alert(e);}

    }
    }

    function FlintAbout(editor) {

    this.show = function() {
        var aboutText = "<p>" + editor.getTitle() + ", version " + editor.getVersion() + "</p>"
        + "<p>SparQLed is provided by <a href='htt://sindicetech.com'>SindiceTech</a>.<br />"
        + "It is based on Flint at <a href='http://www.tso.co.uk'>TSO</a>.</p>";
        editor.getConfirmDialog().setCloseAction();
        editor.getConfirmDialog().show("About SparQLed", aboutText, true);
    }
    }


    function FlintDialog() {

    var button = "";
    var closeAction = {};

    this.show = function(title, text, closeOnly) {

        if (!editor.windowClosing) {
        $('#flint-dialog-title-text').text(title);
        $('#flint-dialog-text').html(text);
        if (closeOnly) {
            $('#flint-dialog-okay-button').css('visibility', 'hidden');
        }
        else {
            $('#flint-dialog-okay-button').css('visibility', 'visible');
        }
        $('.flint-dialog-body').css('margin-top', ($('#flint-editor').position().top + 200) + "px");
        $('#flint-dialog').css('visibility', 'visible');
        }
    }

    this.getResult = function() {
        return button;
    }

    this.setCloseAction = function(callback) {
        if (callback != null) {
            closeAction = callback;
        }
        else {
            closeAction = function() {}
        }
    };

    this.display = function(container) {
        var aboutText = "<div id='flint-dialog'' class='flint-dialog'><div class='flint-dialog-body'><div class='flint-dialog-body-container'><h2 id='flint-dialog-title'><span id='flint-dialog-close' class='flint-close'></span><span id='flint-dialog-title-text'>Title goes here</span></h2>"
        + "<div id='flint-dialog-text'></div>"
        + "<div id='flint-dialog-buttons'><span id='flint-dialog-close-button' class='flint-close-button''>Close</span><span id='flint-dialog-okay-button' class='flint-okay-button'>Okay</span></div>"
        + "</div></div></div>";
        $('#' + container).append(aboutText);

        $('#flint-dialog-close').click(function() {
        $('#flint-dialog-okay-button').css('visibility', 'hidden');
        $('#flint-dialog').css('visibility', 'hidden');
        button = "Close";
        closeAction();
        });
        $('#flint-dialog-okay-button').click(function() {
        try {
            $('#flint-dialog-okay-button').css('visibility', 'hidden');
            $('#flint-dialog').css('visibility', 'hidden');
            button = "Okay";
            closeAction();
        }
        catch (e) {editor.getErrorBox().show(e);}
        });
        $('#flint-dialog-close-button').click(function() {
        $('#flint-dialog-okay-button').css('visibility', 'hidden');
        $('#flint-dialog').css('visibility', 'hidden');
        button = "Close";
        closeAction();
        });
    }
    }

    function generatePlainText(json){
    var vars = json.head.vars;
    var result = "";

    if (isArray(vars)) {
        for(var i=0; i<vars.length; i++){
        result += vars[i] +"\t";
        }result += "\n";

        if (isArray(json.results.bindings)) {
        for(var i=0; i< json.results.bindings.length; i++){
            for(var j=0; j<vars.length; j++){
            result += json.results.bindings[i][vars[j]].value +"\t";
            }
            result += "\n";
        }
        } else {
        for(var j=0; j<vars.length; j++){
            result += json.results.bindings[vars[j]].value +"\t";
        }
        result += "\n";
        }
    }
    else {
        result += vars + "\n";

        if (isArray(json.results.bindings)) {
        for(var i=0; i< json.results.bindings.length; i++){
            result += json.results.bindings[i][vars].value +"\n";
        }
        } else {
        result += json.results.bindings[vars].value +"\n";
        }
    }
    return result;
    }

    function isArray(what) {
    return Object.prototype.toString.call(what) === '[object Array]';
    }

    function generateArrayVars(json, vars){
    var result = "<table class=\"sparql\" border=\"1\"> <tr> ";
    for(var i = 0; i < vars.length; i++){
        result += " <th><font size=3>" + vars[i] + "</font></th> ";
    }

    result += " </tr> ";

    var bindings = json.results.bindings;
    if (isArray(bindings)) {
        for(var i = 0; i < bindings.length; i++){
        result += " <tr>" ;
        for(var j = 0; j < vars.length; j++){
            if (bindings[i][vars[j]].value.substr(-42) === "<http://www.w3.org/2001/XMLSchema#integer>")
            {
                result += "<td><font size=3>" + bindings[i][vars[j]].value.substr(0, bindings[i][vars[j]].value.length - 44);
                    + "</font></td> ";
            } else {
                result += "<td><font size=3>" + bindings[i][vars[j]].value
                    + "</font></td> ";
            }
        }
        result += " </tr> ";
        }
    } else {
        result += " <tr>" ;
        for(var j = 0; j < vars.length; j++){
        if (bindings[vars[j]].value.substr(-42) === "<http://www.w3.org/2001/XMLSchema#integer>")
        {
            result += "<td><font size=3>" + bindings[vars[j]].value.substr(0, bindings[vars[j]].value.length - 44);
            + "</font></td> ";
        } else {
            result += "<td><font size=3>" + bindings[vars[j]].value
            + "</font></td> ";
        }
        }
        result += " </tr> ";
    }
    result += "</table>";
    return result;
    }


    function generateSingleVars(json, vars){
    var result = "<table class=\"sparql\" border=\"1\"> <tr> ";
    result += " <th><font size=3>" + vars + "</font></th> ";
    result += " </tr> ";

    var bindings = json.results.bindings;
    if (isArray(bindings)) {
        for(var i = 0; i < bindings.length; i++){
        result += " <tr>" ;
        if (bindings[i][vars].value.substr(-42) === "<http://www.w3.org/2001/XMLSchema#integer>")
        {
            result += "<td><font size=3>" + bindings[i][vars].value.substr(bindings[i][vars].value.length - 44);
            + "</font></td> ";
        } else {
            result += "<td><font size=3>" + bindings[i][vars].value
            + "</font></td> ";
        }
        result += " </tr> ";
        }
    } else {
        result += " <tr>" ;
        if (bindings[vars].value.substr(-42) === "<http://www.w3.org/2001/XMLSchema#integer>")
        {
        result += "<td><font size=3>" + bindings[vars].value.substr(0, bindings[vars].value.length - 44);
            + "</font></td> ";
        } else {
        result += "<td><font size=3>" + bindings[vars].value
            + "</font></td> ";
        }
        result += " </tr> ";
    }
    result += "</table>";
    return result;
    }

    function generateFormattedText(json){
    var vars = json.head.vars;
    if (vars !== undefined){
        if (isArray(vars)) {
        return  generateArrayVars(json, vars);
        }
        else {
        return generateSingleVars(json, vars);
        }
    }
    else {
        return "<font size=3>" + json.boolean +"</font>";
    }
    }

    function FlintStatus() {

    var line = 0;
    var position = 0;
    var queryValid= "valid";
    var autocompleteStatus = "none"

    this.setLine = function(cursorLine) {line = cursorLine;}

    this.setQueryValid = function(valid) {
        valid ? queryValid = "valid" : queryValid = "invalid";
    }

    this.setQueryResponse = function(response){
        autocompleteStatus = response;
    }

    this.setPosition = function(cursorPosition) {position = cursorPosition;}

    this.display = function(container) {$('#' + container).append("<div id='flint-status'>...</div>");}

    this.updateStatus = function() {
        $('#flint-status').text("Line: " + (line+1) + "; Position: " + (position+1) + "; Query is " + queryValid +"; Status: "+autocompleteStatus);
    }
    }

    function FlintResults(editor) {
    var results = "";
    var formatted = false;
    this.setResults = function(json, mimeType) {
        formatted = false;

        //no-formate == initialisation
        if (mimeType ==="no-format" || (typeof json.status == "undefined") || json.status.toUpperCase() === "SUCCESS") {
        if(mimeType == "text/plain"){
            results = generatePlainText(json)
        }else if(mimeType == "application/sparql-results+json"){
            results = JSON.stringify(json);
            //Update to actually provide XML
        }else if (mimeType == "application/sparql-results+xml"){
            results = JSON.stringify(json);
        }else if(mimeType == "text/formatted") {
            results = generateFormattedText(json)
            formatted = true;

        }else {
            results = "";
        }
        this.showLoading(false);
        try {
            //for old types
            if(formatted){
            $('#formatted-results').html(results);
            $('#flint-results').val("");
            }else{
            $('#flint-results').val(results);
            $('#formatted-results').html("");
            }


            //for formated results
            //create html as a string and do

            //$('#formatted-results').html(html);

        }
        catch (e) {editor.getErrorBox().show(e);}
        }
        else {
        this.showLoading(false);
        $('#flint-results').val(json.message);
        $('#formatted-results').html("");
        var statusArea = new FlintStatus();
        statusArea.setQueryResponse(json.status);
        statusArea.updateStatus();
        }
    }

    this.getResults = function() {return results;}

    this.showLoading = function(showLoader) {
        if (showLoader) {
        $('#flint-results-loader').show();
        $('#flint-results').hide();
        $('#formatted-results').hide();
        }
        else {
        $('#flint-results-loader').hide();
        if(formatted){
            $('#formatted-results').show();
            $('#flint-results').hide();
        }else{
            $('#flint-results').show();
            $('#formatted-results').hide();
        }
        }
    }

    this.display = function(container) {
        $('#' + container).append("<h2 id='flint-results-heading'>Query Results</h2>");
        $('#' + container).append("<div id='flint-results-area';><p id='flint-results-loader'><img src='" + editor.getImagesPath()  + "/ajax-loader-red.gif'/> Running query ... please wait</p><div id='formatted-results' ></div><textarea id='flint-results' cols='100' rows='10'></textarea></div>");
    }
    }

    function FlintMenu(editor) {

    try {
        var newMenuItems = new Array();
        newMenuItems.push(new FlintMenuItem("EmptyQuery", "Empty Query", "New_16x16.png", true, function() {editor.clearEditorTextArea();}));
        newMenuItems.push(new FlintMenuItem("SelectQuery", "Select", "Properties_16x16.png", true, function() {editor.insertSelectQuery();}));
        newMenuItems.push(new FlintMenuItem("ConstructQuery", "Construct", "Key_16x16.png", true, function() {editor.insertConstructQuery();}));

        var editMenuItems = new Array();
        editMenuItems.push(new FlintMenuItem("Undo", "Undo", "Undo_16x16.png", false, function() {editor.undo();}));
        editMenuItems.push(new FlintMenuItem("Redo", "Redo", "Redo_16x16.png", false, function() {editor.redo();}));
        editMenuItems.push(new FlintMenuItem("Cut", "Cut", "Cut_16x16.png", false, function() {editor.cut();}));

        var viewMenuItems = new Array();
        viewMenuItems.push(new FlintMenuItem("Show Tools", "Show Tools Pane", "Prev_16x16.png", true, function() {editor.toggleTools();}));
        viewMenuItems.push(new FlintMenuItem("Hide Tools", "Hide Tools Pane", "Next_16x16.png", false, function() {editor.toggleTools();}));

        var helpMenuItems = new Array();
        helpMenuItems.push(new FlintMenuItem("About", "About", "Information_16x16.png", true, function() {editor.showAbout();}));

        var menuItems = new Array();

        var newMenuItem = new FlintMenuItem("New", "New", true);
        newMenuItem.setSubMenu(newMenuItems);
        menuItems.push(newMenuItem);

        var editMenuItem = new FlintMenuItem("Edit", "Edit", true);
        editMenuItem.setSubMenu(editMenuItems);
        menuItems.push(editMenuItem);

        var viewMenuItem = new FlintMenuItem("View", "View", true);
        viewMenuItem.setSubMenu(viewMenuItems);
        menuItems.push(viewMenuItem);

        var helpMenuItem = new FlintMenuItem("Help", "Help", true);
        helpMenuItem.setSubMenu(helpMenuItems);
        menuItems.push(helpMenuItem);

        this.getItems = function() {return menuItems};
    }
    catch (e) {editor.getErrorBox().show(e);}

    this.setEnabled = function(id, enabled) {
        for (var i = 0; i < menuItems.length; i++) {
        if (menuItems[i].getSubMenu() != null) {
            for (var j = 0; j < menuItems[i].getSubMenu().length; j++) {
            if (menuItems[i].getSubMenu()[j].getId() == id) {
                menuItems[i].getSubMenu()[j].setEnabled(enabled);
                if (enabled) $("#flint-submenu-item-" + i + "-" + j).attr("class", "flint-menu-enabled")
                else $("#flint-submenu-item-" + i + "-" + j).attr("class", "flint-menu-disabled");
                break;
            }
            }
        }
        }
    }

    this.display = function(container) {
        var listItems = "";
        for (var i = 0; i < menuItems.length; i++) {
        listItems += "<li class='flint-menu-item' id='flint-menu-" + i + "'><span>";
        listItems += menuItems[i].getLabel();
        listItems += "</span>"
        if (menuItems[i].getSubMenu() != null) {
            var subList = "";
            for (var j = 0; j < menuItems[i].getSubMenu().length; j++) {
            subList += "<li class='";
            menuItems[i].getSubMenu()[j].getEnabled() ? subList += "flint-menu-enabled" : subList += "flint-menu-disabled";
            subList += "' id='flint-submenu-item-" + i + "-" + j + "'><span>";
            if (menuItems[i].getSubMenu()[j].getIcon() != "") {
                subList += "<img src='"+editor.getImagesPath()+"/"+ menuItems[i].getSubMenu()[j].getIcon() + "'/>";
            }
            else {
                subList += "<span class='flint-menu-filler'></span>";
            }
            subList += menuItems[i].getSubMenu()[j].getLabel();
            subList += "</span></li>";
            }
            listItems += "<ul class='flint-submenu' id='flint-submenu-" + i + "'>" + subList + "</ul>";
        }
        listItems += "</li>";
        }
        $('#' + container).append("<ul id='flint-menu'>" + listItems + "</ul>");

        // Now add events
        for (var i = 0; i < menuItems.length; i++) {
        if (menuItems[i].getSubMenu() != null) {
            for (var j = 0; j < menuItems[i].getSubMenu().length; j++) {
            $("#flint-submenu-item-" + i + "-" + j).click(menuItems[i].getSubMenu()[j].getCallback());
            }
        }
        }
    }
    }

    function FlintMenuItem(itemId, itemLabel, itemIcon, itemEnabled, itemCallback) {

    var id = itemId;
    var subMenu = null;
    var icon = itemIcon;
    var callback = itemCallback;
    var enabled = itemEnabled;

    this.getId = function() {return id};
    this.getIcon = function() {return icon};
    this.getLabel = function() {return itemLabel};
    this.setSubMenu = function(menu) {subMenu = menu;}
    this.getSubMenu = function() {return subMenu;}
    this.getCallback = function() {return callback};
    this.setEnabled = function(value) {enabled = value};
    this.getEnabled = function() {return enabled};
    }

    function FlintToolbar(editor) {

    try {
        var toolbarItems = new Array();
        toolbarItems.push(new FlintToolbarItem("New", "New empty query", "New_24x24.png", true, function() {editor.clearEditorTextArea()}, false));
        toolbarItems.push(new FlintToolbarItem("Select", "New select query", "Properties_24x24.png", true, function() {editor.insertSelectQuery()}, false));
        toolbarItems.push(new FlintToolbarItem("Construct", "New construct query", "Key_24x24.png", true, function() {editor.insertConstructQuery()}, false));
        toolbarItems.push(new FlintToolbarItem("Undo", "Undo last edit", "Undo_24x24.png", false, function() {editor.undo()}, true));
        toolbarItems.push(new FlintToolbarItem("Redo", "Redo last edit", "Redo_24x24.png", false, function() {editor.redo()}, false));
        toolbarItems.push(new FlintToolbarItem("Cut", "Cut selected text", "Cut_24x24.png", false, function() {editor.cut()}, false));
        toolbarItems.push(new FlintToolbarItem("Show Tools", "Show tools pane", "Prev_24x24.png", true, function() {editor.toggleTools()}, true));
        toolbarItems.push(new FlintToolbarItem("Hide Tools", "Hide tools pane", "Next_24x24.png", false, function() {editor.toggleTools()}, false));
        this.getItems = function() {return toolbarItems};
    }
    catch (e) {editor.getErrorBox().show(e);}

    // This is probably a bit inefficient. Need to find a better way
    this.setEnabled = function(id, enabled) {
        for (var i = 0; i < toolbarItems.length; i ++) {
        if (toolbarItems[i].getId() == id) {
            toolbarItems[i].setEnabled(enabled);
            var itemClass = "";
            if (enabled) itemClass = "flint-toolbar-enabled";
            else itemClass = "flint-toolbar-disabled";
            if (toolbarItems[i].getStartGroup()) itemClass += " flint-toolbar-start";
            $("#flint-toolbar-" + i).attr("class", itemClass);
            break;
        }
        }
    }

    this.display = function(container) {
        var listItems = "";
        for (var i = 0; i < toolbarItems.length; i++) {
        listItems += "<li id='flint-toolbar-" + i + "' class='";
        toolbarItems[i].getEnabled() ? listItems += "flint-toolbar-enabled" : listItems += "flint-toolbar-disabled";
        if (toolbarItems[i].getStartGroup()) listItems += " flint-toolbar-start";
        listItems += "'><img src='"+editor.getImagesPath()+"/"+toolbarItems[i].getIcon() + "' title='" + toolbarItems[i].getLabel() + "' alt='" + toolbarItems[i].getLabel() + "'/></li>";
        }
        $('#' + container).append("<ul id='flint-toolbar'>" + listItems + "</ul>");

        for (var i = 0; i < toolbarItems.length; i++) {
        $("#flint-toolbar-" + i).click(toolbarItems[i].getCallback());
        }
    }
    }

    function FlintToolbarItem(itemId, itemLabel, itemIcon, itemEnabled, itemCallback, itemStartGroup) {

    var id = itemId;
    var label = itemLabel;
    var icon = itemIcon;
    var callback = itemCallback;
    var enabled = itemEnabled;
    var startGroup = itemStartGroup;

    this.getId = function() {return id};
    this.getLabel = function() {return label};
    this.getIcon = function() {return icon};
    this.getCallback = function() {return callback};
    this.setEnabled = function(value) {enabled = value};
    this.getEnabled = function() {return enabled};
    this.getStartGroup = function() {return startGroup};
    }


    function FlintEndpointBar(config, editor) {

    try {
        var barItems = new Array();
        barItems.push(new FlintEndpointEntry(config, editor));
        barItems.push(new FlintEndpointQuerySubmitButton(editor));
        barItems.push(new FlintEndpointMimeTypePicker(config, editor));
        this.getItems = function() {return barItems};
    }
    catch (e) {editor.getErrorBox().show(e);}

    this.hide = function() {
        $('#flint-endpoint-bar').show();
        // disable form elements
        barItems[2].setDisableElements("HIDE");
    }

    this.show = function() {
        $('#flint-endpoint-bar').show();

        // disable form elements
        barItems[2].setDisableElements("SHOW");
    }

    this.display = function(container) {
        var listItems = "";
        $('#' + container).append("<div id='flint-endpoint-bar'></div>");
        for (var i = 0; i < barItems.length; i++) {
        listItems += barItems[i].display('flint-endpoint-bar');
        }
    }
    }


    function FlintCoolbar(config, editor) {

    try {
        var coolbarItems = new Array();
        coolbarItems.push(new FlintDatasetPicker(config, editor));
        coolbarItems.push(new FlintDatasetQuerySubmitButton(editor));
        coolbarItems.push(new FlintDatasetMimeTypePicker(config, editor));
        this.getItems = function() {return coolbarItems};
    }
    catch (e) {editor.getErrorBox().show(e);}

    this.hide = function() {
        $('#flint-coolbar').hide();
        coolbarItems[2].setDisableElements("HIDE");
    }

    this.show = function() {
        $('#flint-coolbar').show();
        coolbarItems[2].setDisableElements("SHOW");
    }

    this.display = function(container) {
        var listItems = "";
        $('#' + container).append("<div id='flint-coolbar'></div>");
        for (var i = 0; i < coolbarItems.length; i++) {
        listItems += coolbarItems[i].display('flint-coolbar');
        }
    }
    }

    // The endpoint entry item allows for a freeform URL of an endpoint
    function FlintEndpointEntry(config, editor) {

    try {
        var endpointItems = new Array();
        var endpoint = 'http://sparql.sindice.com/sparql/';

        this.addItem = function() {
        try {
            for (var i = 0; i < endpointItems.length; i++) {
            if (endpointItems[i].uri == this.getEndpoint()) return;
            }
            var newItem = {};
            newItem.uri = this.getEndpoint();
            endpointItems.push(newItem);
        }
        catch (e) {"EndpointEntryAddItem: " + editor.getErrorBox().show(e);}
        }

        this.getItems = function() {return endpointItems};

        this.getItem = function(endpoint) {
        for (var i = 0; i < endpointItems.length; i++) {
            if (endpointItems[i].uri == endpoint) {
            return endpointItems[i];
            }
        }
        return null;
        }

        this.display = function(container) {

        var endpoint = 'http://semantic.ckan.net/sparql/';

        var domain = 'rottentomatoes.com';
        //$('#' + container).append("<div id='flint-endpoint-input' title='Enter the domain you wish to have recommendations for'><h2>Rec Domain</h2><input id='flint-endpoint-url' type='text' value='" + domain + "' name='endpoint'></div>");
        }
        this.getEndpoint = function() {return endpoint;}
        this.getDomain = function() {return $("#flint-endpoint-url").val();}
    }
    catch (e) {"FlintEndpointEntry: " + editor.getErrorBox().show(e);}
    }

    // The dataset picker allows a user to select the endpoint that they wish to send queries to
    function FlintDatasetPicker(config, editor) {

    try {
        var datasetItems = new Array();

        // config.endpoints contains the list of endpoints that should be made available
        for (var i = 0; i < config.endpoints.length; i ++) {datasetItems.push(config.endpoints[i]);}

        this.getItems = function() {return datasetItems};

        this.getItem = function(endpoint) {
        for (var i = 0; i < datasetItems.length; i ++) {
            if (datasetItems[i].uri == endpoint) return datasetItems[i];
        }
        }

        this.display = function(container) {
        var listItems = "";

        // if only 1 dataset, display disabled textbox instead of dropdown
        if (datasetItems.length == 1) {
            $('#' + container).append("<div id='flint-dataset'><h2>Dataset</h2><input disabled='disabled' type=text id='flint-dataset-select' name='kb' value='" + datasetItems[0].uri  + "' /></div>");
        }
        else {
            for (var i = 0; i < datasetItems.length; i ++) {
                listItems += "<option value='" + datasetItems[i].uri + "'>" +datasetItems[i].name + "</option>";
            }
            $('#' + container).append("<div id='flint-dataset' title='Select the endpoint that you wish to query'><h2>Dataset</h2><select id='flint-dataset-select' name='kb'>" + listItems + "</select></div>");
            }
        }

        this.getEndpoint = function() {return $("#flint-dataset-select").val();}

        this.setChangeAction = function(callback) {$('#flint-dataset-select').change(callback);}
    }
    catch (e) {editor.getErrorBox().show(e);}
    }

    function FlintEndpointQuerySubmitButton(editor) {
        this.disable = function() {
            //$('.flint-submit-button').css('visibility','hidden');
        }
        this.enable = function() {$('.flint-submit-button').css('visibility','visible');}

        this.display = function(container) {
            try {
            $('#' + container).append("<input class='flint-submit-button btn btn-mini btn-primary' id='flint-endpoint-submit' type='submit' value='Submit' title='Submit query to endpoint'/>");
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        this.setSubmitAction = function(callback) {$('#flint-endpoint-submit').click(callback);}
    }

    function FlintDatasetQuerySubmitButton(editor) {

    this.disable = function() {
        //$('.flint-submit-button').css('visibility','hidden');
    }

    this.enable = function() {$('.flint-submit-button').css('visibility','visible');}

    this.display = function(container) {
        try {
        $('#' + container).append("<input class='flint-submit-button' id='flint-dataset-submit' type='submit' value='Submit' title='Submit query to endpoint'/>");
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.setSubmitAction = function(callback) {$('#flint-dataset-submit').click(callback);}
    }

    function FlintEndpointMimeTypePicker(config, editor) {

        var lastQueryType;

        this.setDisableElements = function(queryType)  {
            try {

            if (queryType == "SHOW") {
                queryType = lastQueryType;
            }
            else if (queryType == "HIDE") {
                $('#flint-endpoint-mimeset-select, #flint-endpoint-mimeset-construct').attr('disabled', 'disabled');
            }

            if (queryType == "SELECT") {
                $('#flint-endpoint-mimeset-select-chooser').show(300);
                //$('#flint-endpoint-mimeset-construct-chooser').hide();
                //$('#flint-endpoint-mimeset-construct').attr('disabled', '');
//              if ($('#flint-endpoint-bar').is(':visible')) {
////                    $('#flint-endpoint-mimeset-select').attr('disabled', '');
////                    $('#flint-endpoint-mimeset-construct').attr('disabled', 'disabled');
//              }
//              else {
//                  //$('#flint-endpoint-mimeset-select, #flint-endpoint-mimeset-construct').attr('disabled', 'disabled');
//              }

                lastQueryType = queryType;

                //$('#flint-endpoint-mimeset-select').removeAttr('disabled');


            }
            else if (queryType == "CONSTRUCT" || queryType == "DESCRIBE") {
                $('#flint-endpoint-mimeset-construct-chooser').show(300);

                $('#flint-endpoint-mimeset-construct').attr('disabled', '');

                lastQueryType = queryType;
            }
            }
            catch (e) {editor.getErrorBox().show(e);}
        } ;

    this.display = function(container) {
        try {
        var selectChooser = "";
        var constructChooser = "";

        // use output parameter for IE, otherwise accept header mimetype
        if ($.browser.msie)
            var type = 'format';
        else
            var type = 'type';

        for (var i = 0; i < config.defaultEndpointParameters.selectFormats.length; i++) {
            selectChooser += "<option value='" + config.defaultEndpointParameters.selectFormats[i][type] + "'>" +config.defaultEndpointParameters.selectFormats[i].name + "</option>";
        }

        for (var i = 0; i < config.defaultEndpointParameters.constructFormats.length; i++) {
            constructChooser += "<option value='" + config.defaultEndpointParameters.constructFormats[i][type] + "'>" +config.defaultEndpointParameters.constructFormats[i].name + "</option>";
        }

        $('#' + container).append("<div id='flint-endpoint-output-formats' title='Select the format in which you would like the results to be returned'><h2>Output Format</h2></div>");

        selectChooser = "<div id='flint-endpoint-mimeset-select-chooser' title='Select the output type that you wish to request'><select id='flint-endpoint-mimeset-select' name='output'>" + selectChooser + "</select></div>";
        $('#flint-endpoint-output-formats').append(selectChooser);
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.getMimeType = function() {
        try {
        if ($("#flint-endpoint-mimeset-select").is(":visible")) return $("#flint-endpoint-mimeset-select").val();
        else return $("#flint-endpoint-mimeset-construct").val();
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.setChangeAction = function(callback) {
    }
    }

    function FlintDatasetMimeTypePicker(config, editor) {

        var lastQueryType;

        this.setDisableElements = function(queryType)  {

            try {

            if (queryType == "SHOW") {
                queryType = lastQueryType;
            }
            else if (queryType == "HIDE") {
                $('#flint-dataset-mimeset-select, #flint-dataset-mimeset-construct').attr('disabled', 'disabled');
            }

            if (queryType == "SELECT") {

                $('#flint-dataset-mimeset-select-chooser').show(300);
                $('#flint-dataset-mimeset-construct-chooser').hide();

                if ($('#flint-coolbar').is(':visible')) {
                    $('#flint-dataset-mimeset-select').attr('disabled', '');
                    $('#flint-dataset-mimeset-construct').attr('disabled', 'disabled');
                }
                else {
                    $('#flint-dataset-mimeset-select, #flint-dataset-mimeset-construct').attr('disabled', 'disabled');
                }

                lastQueryType = queryType;


            }
            else if (queryType == "CONSTRUCT" || queryType == "DESCRIBE") {
                $('#flint-dataset-mimeset-construct-chooser').show(300);
                $('#flint-dataset-mimeset-select-chooser').hide();

                if ($('#flint-coolbar').is(':visible')) {
                    $('#flint-dataset-mimeset-construct').attr('disabled', '');
                    $('#flint-dataset-mimeset-select').attr('disabled', 'disabled');
                }
                else {
                    $('#flint-dataset-mimeset-select, #flint-dataset-mimeset-construct').attr('disabled', 'disabled');
                }

                lastQueryType = queryType;
            }
            }
            catch (e) {editor.getErrorBox().show(e);}
        } ;

    this.display = function(container) {
        try {
        var selectChooser = "";
        var constructChooser = "";

        // use output parameter for IE, otherwise accept header mimetype
        if ($.browser.msie)
            var type = 'format';
        else
            var type = 'type';

        for (var i = 0; i < config.defaultEndpointParameters.selectFormats.length; i++) {
            selectChooser += "<option value='" + config.defaultEndpointParameters.selectFormats[i][type] + "'>" +config.defaultEndpointParameters.selectFormats[i].name + "</option>";
        }

        for (var i = 0; i < config.defaultEndpointParameters.constructFormats.length; i++) {
            constructChooser += "<option value='" + config.defaultEndpointParameters.constructFormats[i][type] + "'>" +config.defaultEndpointParameters.constructFormats[i].name + "</option>";
        }

        $('#' + container).append("<div id='flint-dataset-output-formats' title='Select the format in which you would like the results to be returned'><h2>Output Format</h2></div>");

        selectChooser = "<div id='flint-dataset-mimeset-select-chooser' title='Select the output type that you wish to request'><select id='flint-dataset-mimeset-select' name='output'>" + selectChooser + "</select></div>";

        constructChooser = "<div id='flint-dataset-mimeset-construct-chooser' title='Select the output type that you wish to request'><select id='flint-dataset-mimeset-construct' name='output'>" + constructChooser + "</select></div>";

        $('#flint-dataset-output-formats').append(selectChooser);
        $('#flint-dataset-output-formats').append(constructChooser);
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.getMimeType = function() {
        try {
        if ($("#flint-dataset-mimeset-select").is(":visible")) return $("#flint-dataset-mimeset-select").val();
        else return $("#flint-dataset-mimeset-construct").val();
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.setChangeAction = function(callback) {
    }
    }


    function FlintSidebar(editor, config) {

    var activeDataItem;
    var activeTab = "SPARQL";
    var allKeywords = editor.allKeywords;

    function displaySparql() {
        $('#flint-sidebar-content').text("");

        var rowsize=4;
        var commandList="<table id='flint-command-table' border='0' cellpadding='0' cellspacing='0' border-style='none,none,none,none'>";
        for(var i=0; i<allKeywords.length; i+=rowsize)
        {
        commandList+="<tr>";
        for(var j=0; (j<rowsize) && (i+j<allKeywords.length); ++j) {
            commandList+='<td border-style="none"><button type="button" disabled="true" id="flint-keyword-'+allKeywords[i+j]+'-button" class="flint-keyword-button">'+allKeywords[i+j]+'</button></td>';
        }
        commandList+="</tr>";
        }
        commandList+="</table>";

        $('#flint-sidebar-content').append(commandList);
    }

    function calcPrefixes() {
        //try {
        //if (config.namespaces != null) {
            //var listText = "";
            //var prefixes = new Array();
            //for (var j = 0; j < config.namespaces.length; j++) {
            //var found = false;
            //if (activeDataItem.properties != null) {
                //var uri = config.namespaces[j].uri;
                //var prefix = config.namespaces[j].prefix;
                //for (var i = 0; i < activeDataItem.properties.results.bindings.length; i++) {
                //if (activeDataItem.properties.results.bindings[i].indexOf(uri) == 0) {
                    //prefixes.push(config.namespaces[j]);
                    //found = true;
                    //break;
                //}
                //}
            //}
            //if (!found && activeDataItem.classes != null) {
                //var uri = config.namespaces[j].uri;
                //var prefix = config.namespaces[j].prefix;
                //for (var i = 0; i < activeDataItem.classes.results.bindings.length; i++) {
                //if (activeDataItem.classes.results.bindings[i].indexOf(uri) == 0) {
                    //prefixes.push(config.namespaces[j])
                    //break;
                //}
                //}
            //}
            //}
            //activeDataItem.prefixes = prefixes;
        //}
        //}
        //catch (e) {editor.getErrorBox().show(e);}
    }

    this.getPrefixes = function() {

        if (activeDataItem == null) return "";

        if (activeDataItem.prefixes != null) {
        var prefixText = "";
        for (var i = 0; i < activeDataItem.prefixes.length; i++) {
            prefixText += "PREFIX " + activeDataItem.prefixes[i].prefix + ": <" + activeDataItem.prefixes[i].uri + ">\n";
        }
        return prefixText;
        }
        else return "";
    }

    //SNC
    this.getActiveDataItem = function() {
        return activeDataItem;
    }

    this.clearActiveItem = function() {
        activeDataItem = null;
    }

    function displayPrefixes() {
        $('#flint-sidebar-content').text("");
        if (activeDataItem) {
        if (activeDataItem.prefixes != null) {
            try {
            var listText = "";
            for (var i = 0; i < activeDataItem.prefixes.length; i++) {
                listText += "<li class='flint-prefix' title='" + activeDataItem.prefixes[i].name + "'>" + activeDataItem.prefixes[i].prefix + "</li>";
            }
            listText = "<ul>" + listText + "</ul>";
            $('#flint-sidebar-content').append(listText);
            $('.flint-prefix').click(
                function(e) {
                editor.insert($(this).text());
                e.stopPropagation();
                }
            );
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        else $('#flint-sidebar-content').append("<p>No prefixes available</p>");
        }
        else $('#flint-sidebar-content').append("<p>No prefixes have been retrieved</p>");
    }

    function displaySamples() {
        $('#flint-sidebar-content').text("");
        if (activeDataItem) {
        if (activeDataItem.queries != null) {
            try {
            var sampleText = "";
            for (var i = 0; i < activeDataItem.queries.length; i++) {
                var query = activeDataItem.queries[i].query;
                query = query.replace(/</g, "&lt;");
                query = query.replace(/>/g, "&gt;");
                if (activeDataItem.queries[i].name !== "") {
                sampleText += "<div class='flint-sample' title=''Click to insert sample into editing pane'><h3>" + activeDataItem.queries[i].name + "</h3><p>" + activeDataItem.queries[i].description + "</p><pre class='flint-sample-content'>" + query + "</pre></div>";
                } else {
                sampleText += "<div class='flint-sample' title=''Click to insert sample into editing pane'><p>" + activeDataItem.queries[i].description + "</p><pre class='flint-sample-content'>" + query + "</pre></div>";
                }
            }
            sampleText = "<div id='flint-samples'>" + sampleText + "</div>";
            $('#flint-sidebar-content').append(sampleText);
            $('.flint-sample-content').click(
                function(e) {
                var okay = true;
                var sample = $(this);
                if (editor.getCodeEditor().getValue() != "") {
                    editor.getConfirmDialog().setCloseAction(function() {
                    var result = editor.getConfirmDialog().getResult();
                    if (result == "Okay") {
                        var cm= editor.getCodeEditor();
                        cm.setValue("");
                        editor.insert(sample.text());
                        // Format query
                        var maxlines= cm.lineCount();
                        for (var ln=0; ln<maxlines; ++ln)
                        cm.indentLine(ln);
                    }
                    });
                    editor.getConfirmDialog().show("Insert Sample Query", "<p>Are you sure you want to abandon the current text?</p>");
                }
                e.stopPropagation();
                }
            );
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        else $('#flint-sidebar-content').append("<p>No samples available</p>");
        }
        else $('#flint-sidebar-content').append("<p>Samples are not applicable</p>");
    }

    function displayProperties() {
        $('#flint-sidebar-content').text("");
        if (activeDataItem) {
        if (activeDataItem.properties != null) {
            try {
            var listText = "";
            for (var i = 0; i < activeDataItem.properties.results.bindings.length; i++) {
                listText += "<li class='flint-property'>" + activeDataItem.properties.results.bindings[i] + "</li>";
            }
            listText = "<ul>" + listText + "</ul>";
            $('#flint-sidebar-content').append(listText);
            $('.flint-property').click(
                function(e) {
                editor.insert("<" + $(this).text() + ">");
                e.stopPropagation();
                }
            );
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        else $('#flint-sidebar-content').append("<p>No properties available</p>");
        }
        else $('#flint-sidebar-content').append("<p>No properties have been retrieved</p>");
    }

    function displayClasses() {
        $('#flint-sidebar-content').text("");
        if (activeDataItem) {
        if (activeDataItem.classes != null) {
            try {
            var listText = "";
            for (var i = 0; i < activeDataItem.classes.results.bindings.length; i++) {
                listText += "<li class='flint-class'>" + activeDataItem.classes.results.bindings[i] + "</li>";
            }
            listText = "<ul>" + listText + "</ul>";
            $('#flint-sidebar-content').append(listText);
            $('.flint-class').click(
                function(e) {
                    if($(this).text().substring(0,1) == "\""){
                  editor.insert( $(this).text() );
                    }else{
                  editor.insert("<" + $(this).text() + ">");
                }

                e.stopPropagation();
                }
            );
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        else $('#flint-sidebar-content').append("<p>No classes available</p>");
        }
        else $('#flint-sidebar-content').append("<p>No classes have been retrieved</p>");
    }

    function showTab(tabName, id) {
        activeTab = tabName;
        $('#flint-sidebar-options li').removeAttr("class");
        $('#' + id).attr("class", "flint-sidebar-selected");
        if (tabName == "Properties") {displayProperties();}
        else if (tabName == "Classes") {displayClasses();}
        else if (tabName == "Prefixes") {displayPrefixes();}
        else if (tabName == "Samples") {displaySamples();}
        else displaySparql();
    }

    this.showActiveTab = function() {
        showTab(activeTab);
    }

    this.display = function(container) {
        var listItems = "";
        $('#' + container).append("<div id='flint-sidebar'>"
                      + "<ul id='flint-sidebar-options''>"
                      + "<li id='flint-sidebar-sparql' title='View a list of SPARQL commands that can be inserted into the query'>SPARQL</li>"
                      + "<li id='flint-sidebar-samples'' title='View sample queries for the current dataset'>Samples</li>"
                      + "</ul><div id='flint-sidebar-content'></div></div>"
                      + "<div id='flint-sidebar-grabber'><span id='flint-sidebar-grabber-button' title='Click to expand/shrink the tools pane'></span></div>");
        $('#flint-sidebar-grabber').click(
        function() {
            try {
            if ($('#flint-sidebar').width() > 50) {
                $('#flint-sidebar').css("width", "3%");
                $('#flint-sidebar-content').css("overflow", "hidden");
                $('#flint-samples').css("white-space", "nowrap");
                $('.CodeMirror').css("width", "95%");
                if (config.interface.toolbar) {
                editor.getToolbar().setEnabled("Show Tools", true);
                editor.getToolbar().setEnabled("Hide Tools", false);
                editor.getMenu().setEnabled("Show Tools", true);
                editor.getMenu().setEnabled("Hide Tools", false);
                }
            }
            else {
                $('#flint-sidebar').css("width", "50%");
                $('#flint-sidebar-content').css("overflow", "auto");
                $('#flint-samples').css("white-space", "wrap");
                $('.CodeMirror').css("width", "48%");
                if (config.interface.toolbar) {
                editor.getToolbar().setEnabled("Show Tools", false);
                editor.getToolbar().setEnabled("Hide Tools", true);
                editor.getMenu().setEnabled("Show Tools", false);
                editor.getMenu().setEnabled("Hide Tools", true);
                }
            }
            }
            catch (e) {editor.getErrorBox().show(e);}
        }
        );
        $('#flint-sidebar-sparql').click(
        function(e) {
            showTab("SPARQL", $(this).attr("id"));
            e.stopPropagation();
        }
        );
        $('#flint-sidebar-samples').click(
            function(e) {
                showTab("Samples", $(this).attr("id"));
                e.stopPropagation();
            }
        );
    }

    this.updateSamples = function(datasetItem) {
        activeDataItem = datasetItem;
        if (activeTab == "Samples") displaySamples();
    }

    this.updateProperties = function(datasetDomain, datasetItem) {
        try {
        if (datasetItem.properties == null) {
            activeDomain = datasetDomain;
            activeDataItem = datasetItem;
            this.showActiveTab();
            var paramsData = {};
            $.ajax({
            url: config.endpoints[0].uri,
            type: 'post',
            data: "data=property&domain="+encodeURIComponent(activeDomain),
            headers: {"Accept": "application/sparql-results-json"},
            crossDomain: true,
            dataType: 'jsonp',
            //processData: false,
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                editor.getErrorBox().show(textStatus+": Properties cannot be retrieved. HTTP Status: " + XMLHttpRequest.status + ", " + errorThrown+"\n"+datasetItem.uri+"\n"+paramsData[config.defaultEndpointParameters.queryParameters.query] );
            },
            success: function(data) {
                datasetItem.properties = data;
                if (activeTab == "Properties") displayProperties();
                calcPrefixes();
                if (activeTab == "Prefixes") displayPrefixes();
                //if (datasetItem.properties.results.bindings.length == 1000) alert("The maximum number of properties has been reached - 1000");
            }
            });
        }
        else {
            activeDomain = datasetDomain;
            activeDataItem = datasetItem;
            if (activeTab == "Properties") displayProperties();
            calcPrefixes();
            if (activeTab == "Prefixes") displayPrefixes();
        }
        }
        catch (e) {editor.getErrorBox().show(e);}
    }

    this.updateClasses = function(datasetDomain,datasetItem) {
        try {
        if (datasetItem.classes == null) {
            activeDomain = datasetDomain;
            activeDataItem = datasetItem;
            this.showActiveTab();
            var paramsData = {};
            //paramsData[config.defaultEndpointParameters.queryParameters.query] = "PREFIX an: <http://sindice.com/vocab/analytics#> SELECT  DISTINCT ?label  FROM <http://sindice.com/analytics-12-09-2011> WHERE { ?node an:domain ?domain . ?node an:property ?prop . ?node an:label ?type . ?type an:label ?label . ?type an:score 1 . }  LIMIT 1000";
            $.ajax({
            url: config.endpoints[0].uri,
            type: 'post',
            data: "data=type&domain="+encodeURIComponent(activeDomain),
            headers: {"Accept": "application/sparql-results-json"},
            crossDomain: true,
            dataType: 'jsonp',
            //processData: false,
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                editor.getErrorBox().show(textStatus+": Classes cannot be retrieved. HTTP Status: " + XMLHttpRequest.status + ", " + errorThrown +"\n"+datasetItem.uri+"\n"+paramsData[config.defaultEndpointParameters.queryParameters.query] );
            },
            success: function(data) {
                datasetItem.classes = data;
                if (activeTab == "Classes") displayClasses();
                calcPrefixes();
                if (activeTab == "Prefixes") displayPrefixes();
                //if (datasetItem.classes.results.bindings.length == 1000) alert("The maximum number of classes has been reached - 1000");
            }
            });
        }
        else {
            activeDomain = datasetDomain;
            activeDataItem = datasetItem;
            if (activeTab == "Classes") displayClasses();
            calcPrefixes();
            if (activeTab == "Prefixes") displayPrefixes();
        }
        }
        catch (e) {editor.getErrorBox().show(e);}
    }
    }
}
