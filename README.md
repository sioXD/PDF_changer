## PDFCrop

This is an Software for cropping, renaming and deleting link for PDFs, made in Java.

## Folder Structure

PDF_Datien - Original Files 

Zugeschnittene_Datein - cropped Files

## Javascrpt Code for common.js

```
// function for better image quality. (good for illustrations on mobile device)

// select images with the class "lazyload"
var lazyImages = document.querySelectorAll('img.lazyloaded, img.lazyload');
// loop through them
for (var i = 0; i < lazyImages.length; i++) {
  var img = lazyImages[i];
  // delete the classes
  img.classList.remove('lazyloaded', 'lazyload');
  // cahange the "src"
  if (img.getAttribute('data-src')) {
	img.setAttribute('src', img.getAttribute('data-src'));
  }
  // delete 'loading'
  img.removeAttribute('loading');
}


if (window.location.pathname.endsWith("/Illustrations")) {		// Check if the current page URL ends with "/Illustrations"
	//automatically close the Japanese section
	if (document.getElementById('English')) {
		document.getElementById('Japanese').className = "";
		document.getElementById('Japanese').remove();
	}else{
		alert("English version don't exist!");
	}
	

	// crop it
	var footer = document.getElementsByClassName('article-footer');
	if (footer.length != 1) {	//catching an error
		alert("Error with footer!");
	}
	 // delete everything after footer, footer includet
	var footerElement = footer[0];
	var sibling = footerElement.nextSibling;
	while (sibling) { 
	    var nextSibling = sibling.nextSibling; 
	    sibling.remove(); 
	    sibling = nextSibling; 
	}
	footerElement.remove(); //remove the footer
	
	var gfooter = document.getElementsByClassName('global-footer');
	gfooter[0].outerHTML = "<p><br></p>";
	var edit_top = document.getElementsByClassName('wiki-page-header__link-container');
	edit_top[0].remove();
	var context_link = document.getElementsByClassName('context-link');
	context_link[0].remove();
	var edit = document.getElementsByClassName('section-edit-link');
	while(edit.length != 0){
		edit[0].remove();
	}
	
	var styleElement = document.getElementById('mw-content-text');
	styleElement.style.paddingRight = '24px';
}

function changeImageUrl() {
	
	// Check if the current page URL ends with "/Illustrations"
	if (window.location.pathname.endsWith("/Illustrations")) {
		
	    // Select all images that have "/revision/" in their src
	    var images = document.querySelectorAll('img[src*="/revision/"]');
	
	    // Loop through all selected images
	    Array.prototype.forEach.call(images, function (img) {
	    	
	        // Find the number after "/scale-to-width-down/"
	        var match = img.src.match(/scale-to-width-down\/(\d+)/);
	
	        // Check if the number exists and is greater than 25 OR that the img-src include "/smart/"
	        if (match && parseInt(match[1], 10) > 25 || img.src.includes("/smart/")) {
	        	
	            // Get the current image source
	            var currentSrc = img.src;
	
				 // Use split to remove everything after and including "/revision/"
	            var newSrc = currentSrc.split('/revision/')[0];
	            
	             img.src = newSrc;
	        }
	    });
	 }
	    
    // Loop it infinitely	// the 500 is the time the function waits bevor the next loop in ms
    setTimeout(changeImageUrl, 500);
}

changeImageUrl();
```