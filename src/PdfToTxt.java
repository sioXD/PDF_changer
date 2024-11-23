import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfToTxt {

    public void ToTxt(File inputDir, File outputDir)throws Exception{

        if (!outputDir.exists()) {
            outputDir.mkdirs();  // create dir, if not exists
        }

        File[] files = inputDir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files != null) {
        int count = 0;

            for (File file : files) {
                count += 1;


                try (PDDocument document = Loader.loadPDF(file)){
                    PDFTextStripper stripper = new PDFTextStripper();
                    PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
                    
                    //custom starting point
                    PDPage destinationPage = getBookmark(outline).findDestinationPage(document);
                    if (destinationPage == null) {throw new Exception("keinen Startpunkt gefunden");}

                    int pageNumber = document.getPages().indexOf(destinationPage) + 1;
                    System.out.println(pageNumber);

                    // Konfiguration für PDFTextStripper
                    stripper.setSortByPosition(true); // Sortiert Text basierend auf Position
                    stripper.setStartPage(pageNumber); // start at Chapter 1
                    
                    // Extrahiere den gesamten Text aus dem Dokument
                    StringBuilder cleanedText = new StringBuilder();


                    //for Accsess -- DO NOT CHANGE -- (idk why it's working, but it works)
                    AccessPermission accessPermissions = new AccessPermission();
                    accessPermissions.setCanModify(false);
                    accessPermissions.setCanExtractContent(true);
                    accessPermissions.setCanPrint(false);
                    accessPermissions.setReadOnly();
                    accessPermissions.setCanAssembleDocument(true);
                    StandardProtectionPolicy spp = new StandardProtectionPolicy(UUID.randomUUID().toString(), "", accessPermissions);
                    document.protect(spp); 


                    // Main
                    for (int i = pageNumber; i <= document.getNumberOfPages(); i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);

                        // Extrahiere den Text pro Seite
                        String pageText = stripper.getText(document);

                        // Entferne Header/Footer nur für diese Seite
                        String pageTextCleaned = removeHeaderAndFooter(pageText);

                        cleanedText.append(pageTextCleaned).append("\n");
                    }

                    // write to .txt
                    String filename = file.getName() + ".txt";
                    File text = new File(outputDir, filename);

                    try (FileWriter writer = new FileWriter(text)) {
                        writer.write(cleanedText.toString());
                    }
                } catch (IOException e) {
                    System.err.println("Fehler: " + e.getMessage());
                }

                System.out.println(count + "/" + files.length);
            }
        }//OFif

    }//EOF

    //find Bookmarks
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

    // delete Header and Footer
    private static String removeHeaderAndFooter(String text) {
        // Beispiel: Entferne die ersten und letzten Zeilen (Header/Footer)
        String[] lines = text.split("\n");
        StringBuilder cleanedText = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            // Überspringe Header (z. B. erste Zeile) und Footer (z. B. letzte Zeile)
            if (i == lines.length - 1) {
                continue;
            }
            cleanedText.append(lines[i]).append("\n");
        }
        return cleanedText.toString();
    }

}//EOC
