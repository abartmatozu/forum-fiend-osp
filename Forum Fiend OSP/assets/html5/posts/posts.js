(function(window){
	"use strict";
	
	var outputDiv;
	
	window.downloadPosts = downloadPosts;
	
	function downloadPosts(thread,page,output){
		outputDiv = output;
		
		var postsURL = "http://www.discussions-online.com/app_resource.php?rt=8&t=" + thread + "&p=" + page;
		
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange=function(){
			if(xmlhttp.readyState==4 && xmlhttp.status==200){
				output.innerHTML=xmlhttp.responseText;
				} else {
					output.innerHTML= "cockfuck: " + xmlhttp.readyState + "#" + xmlhttp.status + "<br />" + postsURL;
				}
			}
		xmlhttp.open("GET",postsURL,true);
		xmlhttp.send();
	}
	
})(window);