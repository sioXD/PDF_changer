package de.pdf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;



public class IllustrationExtractor {

    final Boolean delete_folder_contents = true;

    public <PDOptionalContentProperties> void onlyPic(File inputDir, File outputDir)throws Exception{

        if (!outputDir.exists()) {
            outputDir.mkdirs();  // create dir, if not exists
        }
        if (delete_folder_contents) {
            FileUtils.cleanDirectory(outputDir); // delete all files in the folder
        }

        File[] files = inputDir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files != null) {
            int count = 0;

            for (File file : files) {
                count += 1;

                try (PDDocument document = Loader.loadPDF(file)) {

                    //! look here: https://stackoverflow.com/questions/7063324/extract-image-from-pdf-using-java //TODO


                    //Font Size for Header
                    List<Float> headerFontSizes = Arrays.asList(17.0f);

                    HeaderAwarePDFTextStripper pdfStripper = new HeaderAwarePDFTextStripper(headerFontSizes); //extends PDFTextStripper  bla bla ...
                    StringBuilder fullText = new StringBuilder();
                    PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();

                    //processing pdf file
                    String fileName = file.getName();
                    File outputFile = new File(outputDir, fileName);

                    //console output
                    String year = fileName.matches(".*\\bYear\\s*\\d+.*") ? fileName.replaceAll(".*\\b(Year\\s*\\d+).*", "$1") : ""; // gets used later for the intro too
                    String year_console = year != "" ? year + " " : "" ; // if year is "", then same, else: year + " "
                    year = year + " "; // I hate regex to much for changing the line over this
                    String version = fileName.replaceAll(".*Vol\\.\\s*(\\d+(\\.\\d+)?).*", "$1"); 
                    System.out.print(count + "/" + files.length); // show count
                    System.out.print(" ~~~ (" + year_console + "Volume " + version + ")"); // Names of the files




                    //custom starting point //TODO needs to be improved
                    if (getBookmark(outline) == null) {throw new Exception("\u001B[31m" + "no starting point found" + "\u001B[0m");}
                    PDPage destinationPage = getBookmark(outline).findDestinationPage(document);
                    int pageNumber = document.getPages().indexOf(destinationPage);

                    //custom end point
                    int pageNumberEnd;
                    if(getBookmarkEnd(outline, document) == null){
                        pageNumberEnd = document.getNumberOfPages(); //if no postscript --> last page is the end
                    }else{
                        pageNumberEnd = document.getPages().indexOf(getBookmarkEnd(outline, document).findDestinationPage(document)); 
                    }

                    //delete all layers

                    PDDocumentCatalog catalog = document.getDocumentCatalog();
                    PDOptionalContentProperties ocProperties = (PDOptionalContentProperties) catalog.getOCProperties();
          


                    document.getDocumentCatalog().setOCProperties(null);

                    for (PDPage page : document.getPages()) {
                        PDResources resources = page.getResources();
                        // Durchsuche Ressourcen nach OCGs (indirekt über COS-Namen)
                        for (COSName name : resources.getCOSObject().keySet()) {
                            COSBase item = resources.getCOSObject().getItem(name);
                            if (item instanceof COSDictionary) {
                                COSDictionary dict = (COSDictionary) item;
                                if (dict.containsKey(COSName.TYPE) && dict.getNameAsString(COSName.TYPE).equals("OCG")) {
                                    resources.getCOSObject().removeItem(name); // Entfernt Layer-Referenz
                                }
                            }
                        }
                    }

                    
                    // Go through all sides of the PDF
                    for (int page = pageNumber; page < pageNumberEnd;) { 
                        pdfStripper.setStartPage(page + 1);
                        pdfStripper.setEndPage(page + 1);

                        PDPage currentPage = document.getPage(page);
                        boolean hasImages = false;
                 
                        
                        String text = pdfStripper.getText(document);
                        text = removeLinesWithLinks(text);

                        // Check whether the page is blank or only consists of images
                        if (text.isEmpty()) {
                            page++;
                            
                        } else {
                            document.removePage(currentPage); 
                            int currentPageCount = document.getNumberOfPages();
                            if (page >= currentPageCount) break; // Verhindert Endlosschleife

                        } 
                    }



                    //for Access
                    AccessPermission accessPermissions = new AccessPermission();
                    accessPermissions.setCanModify(false);
                    accessPermissions.setCanExtractContent(true);
                    accessPermissions.setCanPrint(false);
                    accessPermissions.setReadOnly();
                    accessPermissions.setCanAssembleDocument(true);

    
                    StandardProtectionPolicy spp = new StandardProtectionPolicy(UUID.randomUUID().toString(), "", accessPermissions);
                    document.protect(spp);



                    
                    // Write the edited text to the output file
                    String outputFileName = file.getName();
                    document.save(new File(outputDir, outputFileName));    
                    document.close();

                    //performFinalScan(outputFile); //Scan for errors

                } catch (Exception e) {
                    System.err.println(" --- " + "\u001b[31;1m" +"Error processing PDF file: " + "\u001B[0m" + e);
                }

            }//EO-for
        }//OFif

    }//EOF

    

    //find Bookmarks start
    public PDOutlineItem getBookmark(PDOutlineNode bookmark) throws IOException{
        PDOutlineItem current = bookmark.getFirstChild();
        while (current != null){
            if(current.getTitle().contains("1")){ 
                return current;
            }
            getBookmark(current);
            current = current.getNextSibling();
        }
        return null; 
    }

    //find Bookmarks end
    public PDOutlineItem getBookmarkEnd(PDOutlineNode bookmark, PDDocument document) throws IOException{
        PDOutlineItem current = bookmark.getFirstChild();
        while (current != null){
            if(current.getTitle().contains("Postscript")){ 
                int postscript = document.getPages().indexOf(current.findDestinationPage(document));
                getBookmark(current);
                current = current.getNextSibling();
                int afterPostscript = document.getPages().indexOf(current.findDestinationPage(document));

                if(current.getTitle().contains("Postscript")){return null;}// if no bookmark after postscript
                if(postscript > afterPostscript){return null;}


                return current;
            }
            getBookmark(current);
            current = current.getNextSibling();
        }
        return null;
    }

    
        

    
    // Function that removes lines with "mp4directs.com"
    private static String removeLinesWithLinks(String text) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n"); // Split text into lines
        for (String line : lines) {
            if (line.contains("mp4directs.com")) { // Ignore lines that contain "mp4directs.com" 
            }else{
                result.append(line).append("\n");
            }
        }
        return result.toString().trim(); // Return result
    }


}//EOC

