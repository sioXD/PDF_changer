package de.pdf;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception{
        System.out.println();
        System.out.println();
        
        int m = 0;
        int decision;

        while (m == 0) {
            m = 1;

            System.out.println("\nSelect the Software you want use: ");
            System.out.println("0: quit");
            System.out.println("1: IllustrationChanger");
            System.out.println("2: PdfToTxt");
            System.out.println();

            Scanner s = new Scanner(System.in); // must be outside of try, so catch can see it -(i hate it too)

            try {
                decision = s.nextInt();    
                
                if (decision == 0){
                    System.exit(0);
                }else if (decision == 1) {
                    IllustrationChanger Illu = new IllustrationChanger();
                    Illu.IllustrationChange(new File("src/main/resources/IllustrationChanger/PDF_Files"), 
                                        new File("src/main/resources/IllustrationChanger/Cropped_Files"));

                }else if (decision == 2) {
                    PdfToTxt PTT = new PdfToTxt();
                    PTT.ToTxt(new File("src/main/resources/PdfToTxt/Pdf"), 
                          new File("src/main/resources/PdfToTxt/Txt"));

                }else{
                    throw new Exception("wrong number entered");
                }

            }catch(NoSuchElementException  ex){
                System.out.println("\u001b[31;1m" + "Invalid input, please enter a number.\n" + "\u001B[0m");
                m = 0; // try while again
                
                if (s.hasNextLine()) {
                    s.nextLine(); // delete illegal input
                }
            }catch(Exception e){
                System.out.println("\u001b[31;1m" + "Error with input: " + e + "\u001B[0m");
                m = 0;

                //just to make sure
                if (s.hasNextLine()) {
                    s.nextLine(); 
                }
            }finally{
                s.close();
            }
        }

        System.out.println("\nFinished! :)");

    }//EOF
}//EOC