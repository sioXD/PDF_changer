package de.pdf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

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

                try (PDDocument document = Loader.loadPDF(file)) {
                    //Font Size for Header
                    List<Float> headerFontSizes = Arrays.asList(17.0f);

                    HeaderAwarePDFTextStripper pdfStripper = new HeaderAwarePDFTextStripper(headerFontSizes); //extends PDFTextStripper  bla bla ...
                    StringBuilder fullText = new StringBuilder();
                    PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();

                    //verarbeitung von pdf zu txt
                    String fileName = file.getName();
                    fileName = fileName.replaceAll("\\.pdf$", ".txt");
                    File outputFile = new File(outputDir, fileName);

                    //console output
                    String year = fileName.matches(".*\\bYear\\s*\\d+.*") ? fileName.replaceAll(".*\\b(Year\\s*\\d+).*", "$1") : ""; // gets used later for the intro too
                    String year_console = year != "" ? year + " " : "" ; // if year is "", then same, else: year + " "
                    String version = fileName.replaceAll(".*Vol\\.\\s*(\\d+(\\.\\d+)?).*", "$1"); 
                    System.out.print(count + "/" + files.length); // show count
                    System.out.print(" ~~~ (" + year_console + "Volume " + version + ")"); // Namen der Dateien




                    //custom starting point
                    if (getBookmark(outline) == null) {throw new Exception("\u001B[31m" + "keinen Startpunkt gefunden" + "\u001B[0m");}
                    PDPage destinationPage = getBookmark(outline).findDestinationPage(document);
                    int pageNumber = document.getPages().indexOf(destinationPage);

                    //custom end point
                    int pageNumberEnd;
                    if(getBookmarkEnd(outline, document) == null){
                        pageNumberEnd = document.getNumberOfPages();
                    }else{
                        pageNumberEnd = document.getPages().indexOf(getBookmarkEnd(outline, document).findDestinationPage(document)); 
                    }


                    //make Introduction
                    String intro = "Hello, and thank you for listening with Pixco. Just a few reminders before we begin. Classroom of the Elite's illustrations will be announced, so please listen for the narrator to say, please view the illustration. Classroom of the Elite " + year + "Volume " + version + ", written by Syougo Kinugasa. Art by Tomo Sessionsaku. Audio by Pixco.";
                    fullText.append(processText(intro));
                    
                    // Durchlaufe alle Seiten der PDF
                    for (int page = pageNumber; page < pageNumberEnd; page++) { 
                        pdfStripper.setStartPage(page + 1);
                        pdfStripper.setEndPage(page + 1);

                        String text = pdfStripper.getText(document)/* .trim()*/;
                        text = removeLinesWithLinks(text);

                        // Prüfe, ob die Seite leer ist oder nur aus Bildern besteht
                        if (text.isEmpty()) {
                            fullText.append("\nPlease view the Illustration.\n");
                        } else {
                            fullText.append(processText(text));
                        }
                    }

                    //remove all emty lines that where createt
                    Pattern p = Pattern.compile("(?m)^[\\s]*\n");
                    Matcher m = p.matcher(fullText);
                    String cleanedText = m.replaceAll("");
                    fullText.setLength(0);
                    fullText.append(cleanedText);


                    // Schreibe den bearbeiteten Text in die Ausgabe-Datei
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writer.write(fullText.toString());
                    }


                    performFinalScan(outputFile); //Scan for errors

                } catch (Exception e) {
                    System.err.println(" --- " + "\u001b[31;1m" +"Fehler beim Verarbeiten der PDF-Datei: " + "\u001B[0m" + e);
                }

            }//EOfor
        }//OFif

    }//EOF

    // Funktion zur Textverarbeitung
    private static String processText(String text) {

        String noLineBreaks = text.replace("\r", "").replace("\n", "");// Entferne alle ursprünglichen Zeilenumbrüche
        
        //Header gets better
        noLineBreaks = combineHeader(noLineBreaks);

        // Remove diacritical marks, accents, etc.
        String cleanedText = Normalizer.normalize(noLineBreaks, Normalizer.Form.NFD); 
        cleanedText = cleanedText.replaceAll("\\p{M}", "");


        return cleanedText
            .replace("No.", "Number") //No. 11 --> Number 11
            .replace("”", "\"") 
            .replace("“", "\"") 
            .replace("—", "-") 
            .replace("–", "-") 
            .replace("‘", "'") 
            .replace("’", "'") 
            .replace("★", "")
            .replace("☆", "")
            .replace("…", "...")
            .replace("¾", "...") //editor mistake in volume 3
            .replace("°C", "Celsius")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("•", "/")
            .replace("\t", " ")
            .replace("ßß", "\n")

            // if too many: � --> problem might be ß
  
            .replaceAll("[.?!] \\s*", "$0\n") //.|?|! mit Leerzeichen, wird durch \n ersetzt
            .replaceAll("[.?!]\"\\s*", "$0\n"); //.|?|! mit ", wird durch \n ersetzt
        }

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

    //Fix Headers
    private static String combineHeader(String text) {
        StringBuilder result = new StringBuilder();
        String[] fragments = text.split("ßß"); // Text an "ßß" aufteilen 
    
        StringBuilder headerBuffer = new StringBuilder(); // Buffer für Header-Zusammenführung
        int countHead = 0;
    
        for (String fragment : fragments) {
            fragment = fragment.trim();
            //System.out.println(fragment);
    
            if (fragment.startsWith("ß")) { // Fragment gehört zum Header
                if (countHead == 0){
                    headerBuffer.append("ß" + fragment); //um am anfang ein \n zu haben
                    countHead += 1;
                }else{
                    headerBuffer.append(fragment.replaceAll("ß", "")).append(" "); // "ß" entfernen und space hinzufügen
                }
            } else { //  normal Text
                if (headerBuffer.length() > 0) { 
                    result.append(headerBuffer.toString().trim()).append("ßß");
                    headerBuffer.setLength(0); 
                }
                result.append(fragment); // add normal text
            }
        }
        // cheack for errors
        if (headerBuffer.length() > 0) {
            result.append(headerBuffer.toString().trim()).append("ßß");
        }
    
        return result.toString();
    }
    


        
    // Funktion, die Zeilen mit "https://" entfernt
    private static String removeLinesWithLinks(String text) {
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n"); // Text in Zeilen aufteilen
        for (String line : lines) {
            if (!line.contains("https://")) { // Zeilen ignorieren, die "https://" enthalten
                result.append(line).append("\n");
            }
        }
        return result.toString().trim(); // Ergebnis zurückgeben
    }

    //final Scan
    private static void performFinalScan(File file) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RED_BRIGHT = "\u001b[31;1m";
        final String ANSI_RESET = "\u001B[0m";

        try {
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
    
            int invalidCharCount = 0; //�
            int controlCharCount = 0; //other
    
            // Scan for invalid characters and control characters
            for (char c : fileContent.toCharArray()) {
                if (c == '�') {
                    invalidCharCount++;
                } else if (Character.isISOControl(c) && c != '\n' && c != '\r') {
                    controlCharCount++;
                }
            }

            // Scan for separated words
            String regex_subchapter = "\\d\\.\\d\\n";
            String regex_error = "\\d\\.\\d\\n[^\\s]{1,2}\\s";

            Pattern p = Pattern.compile(regex_subchapter);
            Pattern pe = Pattern.compile(regex_error);
            Matcher m = p.matcher(fileContent);
            Matcher me = pe.matcher(fileContent);

            int error_count = 0;
            int subchapter_count = 0;
            while (m.find()) {
                subchapter_count += 1;
            }while (me.find()) {
                error_count += 1;
            }

            float error_rate = (float) error_count / subchapter_count * 100;
            boolean failed_detection = (subchapter_count==0 && error_count==0) ? true : false;

            // Print error message if issues were detected
            if (invalidCharCount > 0 || controlCharCount > 0 || error_count >= subchapter_count-5) {
                System.err.println(ANSI_RED_BRIGHT + " --- Final scan detected issues:" + ANSI_RESET);
                if (invalidCharCount > 0) {
                    System.err.println(ANSI_RED + "  -- Detected " + ANSI_RESET + invalidCharCount +  ANSI_RED + " occurrences of the invalid character '�'." + ANSI_RESET);
                }
                if (controlCharCount > 0) {
                    System.err.println(ANSI_RED + "  -- Detected " + ANSI_RESET + controlCharCount + ANSI_RED + " control characters that may indicate encoding issues." + ANSI_RESET);
                }
                if (failed_detection) {
                    System.err.println(ANSI_RED + "  -- Detected " + ANSI_RESET + "no subchapters. " + ANSI_RED + "Please check the file manually." + ANSI_RESET);
                } else if (error_count >= subchapter_count-5) {
                    System.err.println(ANSI_RED + "  -- Detected: " + ANSI_RESET + error_rate + "%" + ANSI_RED + " of the subchapter beginnings are separated" + ANSI_RESET);
                }

                throw new Exception("Issues detected during the final scan. Please review the output file.\n");
            }
    
            System.out.println(" --- Final scan completed: No Errors Found.");
    
        } catch (Exception e) {
            System.err.println();
        }
    }

}//EOC
