import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;



public class PDFCrop {
    public static void main(String[] args) {
        // dir where PDFs end
        File outputDir = new File("src/Zugeschnittene_Datein");
        if (!outputDir.exists()) {
            outputDir.mkdirs();  // create dir, if not exists
        }
        
        File dir = new File("src/PDF_Datein");
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
        
        if (files != null) {
            for (File file : files) {
                try (PDDocument document = Loader.loadPDF(file)) {

                    // cropp for each page (its only 1 page (OperaGX-download as PDF))
                    for (PDPage page : document.getPages()) {

                        // delete Hyperlinks
                        List<PDAnnotation> annotations = page.getAnnotations();
                        Iterator<PDAnnotation> iterator = annotations.iterator(); 
                        
                        while (iterator.hasNext()) {
                            PDAnnotation annotation = iterator.next();

                            if (annotation instanceof PDAnnotationLink) {
                                iterator.remove();
                            }
                        }

                        page.setAnnotations(annotations);



                        PDRectangle originalBox = page.getMediaBox();

                        // values
                        float offsetFromTop = 100;                       
                        float newUpperRightY = originalBox.getUpperRightY() - offsetFromTop;

                        PDRectangle cropBox = new PDRectangle(
                                originalBox.getLowerLeftX(),  // same
                                newUpperRightY - originalBox.getHeight(), // new y (mesured from top)
                                originalBox.getWidth(),       // same
                                originalBox.getHeight()       // same
                        );

                        page.setCropBox(cropBox); // crop the pdf
                    }//EO_for

                    /*  String name = file.getName();*/
                    System.err.println("\nPDF cropped!"); 


                    AccessPermission accessPermissions = new AccessPermission();
                    accessPermissions.setCanModify(false);
                    accessPermissions.setCanExtractContent(true);
                    accessPermissions.setCanPrint(false);
                    accessPermissions.setReadOnly();
                    accessPermissions.setCanAssembleDocument(true);

    
                    StandardProtectionPolicy spp = new StandardProtectionPolicy(UUID.randomUUID().toString(), "", accessPermissions);
                    document.protect(spp);

                     // change the name and save
                    String outputFileName = adjustFileName(file.getName());
                    document.save(new File("src/Zugeschnittene_Datein",outputFileName));    

                    
                    System.err.println("safed!");   
                    document.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//OFif
        System.out.println("\nFinished! :)");
    }//EOF -- End of funktion

    //Method for name change
    public static String adjustFileName(String originalFileName) {
        // delete everything before "_ You-Zitsu Wiki _ Fandom"
        String pattern = "(.*)_ You-Zitsu Wiki _ Fandom\\.pdf";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(originalFileName);
        
        if (matcher.find()) {
            String baseName = matcher.group(1); 
            return baseName + ".pdf";
        }

        //if the function finds not "_ You-Zitsu"
        return "cropped_" + originalFileName;
    }//EOF
}//ENC -- End of class