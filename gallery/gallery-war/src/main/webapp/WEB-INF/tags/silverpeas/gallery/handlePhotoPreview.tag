<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="jquerySelector" required="true"
              type="java.lang.String"
              description="The jQuery selector." %>

<style type="text/css">
  .photoPreviewTip {
    max-width: none;
    max-height: none;
  }
</style>

<script type="text/javascript">

var selectedVideoId = null;
var intervalVariable = null;

$(document).ready(function() {
  $('${jquerySelector}').each(function() {
    $(this).qtip({
      style : {
        classes : 'qtip-bootstrap photoPreviewTip'
      },
      content : {
        title : {
          text : $(this).attr("tipTitle")
        },
        text : "<img src='" + $(this).attr("tipUrl") + "' id='tipUrl_" + $(this).attr("id") + "' />"
      },
      position : {
        adjust : {
          method : "flip"
        },
        viewport : $(window)
      }
    });
  });
});

$(document).delegate( ".videoPreview", "mouseover", function(event) {
  if ( window.console && window.console.log ) {
    console.log("event raised on id= " + $(this).attr("id"));
  }
  selectedVideoId = $(this).attr('id');
  intervalVariable = setInterval(function() {
    changePicture();
  }, 1000);
 });
$(document).delegate( ".videoPreview", "mouseout", function(event) {
  window.clearInterval(intervalVariable);
  resetPicture();
});

function changePicture() {
  var src = $("#tipUrl_" + selectedVideoId).attr("src");
  if (src.indexOf("thumbnail/0") >= 0) {
    src = src.replace("thumbnail/0", "thumbnail/1");
  } else if (src.indexOf("thumbnail/1") >= 0) {
    src = src.replace("thumbnail/1", "thumbnail/2");
  } else if (src.indexOf("thumbnail/2") >= 0) {
    src = src.replace("thumbnail/2", "thumbnail/3");
  } else if (src.indexOf("thumbnail/3") >= 0) {
    src = src.replace("thumbnail/3", "thumbnail/4");
  } else if (src.indexOf("thumbnail/4") >= 0) {
    src = src.replace("thumbnail/4", "thumbnail/0");
  }
  $("#tipUrl_" + selectedVideoId).attr("src", src);
}
function resetPicture() {
  var src = $("#tipUrl_" + selectedVideoId).attr("src");
  if (src.indexOf("thumbnail/1") >= 0) {
    src = src.replace("thumbnail/1", "thumbnail/0");
  } else if (src.indexOf("thumbnail/2") >= 0) {
    src = src.replace("thumbnail/2", "thumbnail/0");
  } else if (src.indexOf("thumbnail/3") >= 0) {
    src = src.replace("thumbnail/3", "thumbnail/0");
  } else if (src.indexOf("thumbnail/4") >= 0) {
    src = src.replace("thumbnail/4", "thumbnail/0");
  }
  $("#tipUrl_" + selectedVideoId).attr("src", src);
}
</script>