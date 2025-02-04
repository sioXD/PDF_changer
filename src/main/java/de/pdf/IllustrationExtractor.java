package de.pdf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javax.imageio.ImageIO;



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

                    //* look here: https://stackoverflow.com/questions/7063324/extract-image-from-pdf-using-java

                    int imageCount = 1;
            
                    for (PDPage page : document.getPages()) {  // :cite[3]:cite[6]
                        PDResources resources = page.getResources();
                        
                        for (COSName name : resources.getXObjectNames()) {  // :cite[6]:cite[8]
                            if (resources.isImageXObject(name)) {  // :cite[8]:cite[10]
                                PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                                
                                // Generate unique filename
                                String suffix = image.getSuffix();
                                if (suffix.isEmpty()) suffix = "png";  // Default format
                                String fileName = String.format("image-%03d.%s", imageCount++, suffix);
                                
                                // Save image using ImageIO
                                ImageIO.write(image.getImage(), suffix, new File(fileName));  // :cite[6]:cite[8]
                                System.out.println("Saved image: " + fileName);
                            }
                        }
                    }




                } catch (Exception e) {
                    System.err.println(" --- " + "\u001b[31;1m" +"Error processing PDF file: " + "\u001B[0m" + e);
                }

            }//EO-for
        }//OFif
    }//EOF
}//EOC

