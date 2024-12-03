# changePDF

This is a Software for two things:

first: cropping, renaming and deleting link for PDFs, made in Java.

second: scraping PDF Documents and copying all the text in an .txt document.

## Folder Structure

`src\IllustrationChanger` - Main Folder for the first task

- `src\IllustrationChanger\PDF_Files` - all original PDF files (not cropped)

- `src\IllustrationChanger\Cropped_Files` - all cropped files

---

`src\PdfToTxt` - cropped Files

- `src\PdfToTxt\Pdf` - PDF files where the text should be extractet from

- `src\PdfToTxt\Txt` - output txt files

## Getting Startet

- go to the `main` folder and run the projekt

- _[OPTIONAL]_ : you can change the input and output folder inside the main

### Javascrpt Code for common.js (first task)

you can find the commen.js on: on : <https://you-zitsu.fandom.com/wiki/User:______/common.js>   -on "_____" needs to be your username

```javascript
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


if (window.location.pathname.endsWith("/Illustrations")) {        // Check if the current page URL ends with "/Illustrations"
    //automatically close the Japanese section
    if (document.getElementById('English')) {
        document.getElementById('Japanese').className = "";
        document.getElementById('Japanese').remove();
    }else{
        alert("English version don't exist!");
    }


    // crop it
    var footer = document.getElementsByClassName('article-footer');
    if (footer.length != 1) {    //catching an error
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

    // Loop it infinitely    // the 500 is the time the function waits bevor the next loop in ms
    setTimeout(changeImageUrl, 500);
}

changeImageUrl();
```

---

### Infos (second)

<u>Where to get PDF files:</u>

In gerneral you can get them anywhere, but if you want to to use them for audiobooks, it's recommend to use for Example: <https://mp4directs.com/threads/classroom-of-the-elite-light-novels-all-volumes-pdf.549/> (here COTE)

- if you have problems finding the Fontsize of your headers, use: `Get_Fontsize_and_Position.java`
