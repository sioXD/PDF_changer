package de.pdf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;


public class IllustrationChanger {

    final Boolean delete_folder_contents = true;

    public void IllustrationChange(File inputDir, File outputDir)throws Exception{

        // dir where PDFs end
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
                try (PDDocument document = Loader.loadPDF(file)) {
                    count += 1;

                    // crop for each page (its only 1 page (OperaGX-download as PDF))
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
                                newUpperRightY - originalBox.getHeight(), // new y (measured from top)
                                originalBox.getWidth(),       // same
                                originalBox.getHeight()       // same
                        );

                        page.setCropBox(cropBox); // crop the pdf
                    }//EO_for

                    //for Access
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
                    document.save(new File(outputDir,outputFileName));    
                    document.close();
                    System.out.println(count + "/" + files.length);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//OFif
    }//EOF

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
}//EOC
