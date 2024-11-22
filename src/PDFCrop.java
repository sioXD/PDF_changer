import java.io.File;

public class PDFCrop {
    public static void main(String[] args) {

        IllustrationChanger Illu = new IllustrationChanger();
        Illu.IllustrationChange(new File("src/PDF_Datein"), new File("src/Zugeschnittene_Datein"));

        

        System.out.println("\nFinished! :)");
    }//EOF
}//EOC