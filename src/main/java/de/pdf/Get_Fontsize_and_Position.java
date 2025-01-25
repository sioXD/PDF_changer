package de.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Get_FontSize_and_Position {

    public static void analyzeFontSizesAndPositions(File pdfFile, BufferedWriter writer) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    StringBuilder wordBuilder = new StringBuilder();
                    List<TextPosition> wordPositions = new ArrayList<>();
                    float lastX = -1; // last X position
                    float threshold = 2.5f; // Distance threshold between characters

                    for (TextPosition textPosition : textPositions) {
                        float currentX = textPosition.getXDirAdj();
                        if (lastX != -1 && (currentX - lastX > threshold)) {
                            // New word recognized, therefore output
                            printWord(wordBuilder.toString(), wordPositions, writer);
                            wordBuilder.setLength(0); // Reset word
                            wordPositions.clear();
                        }
                        wordBuilder.append(textPosition.getUnicode());
                        wordPositions.add(textPosition);
                        lastX = currentX + textPosition.getWidthDirAdj();
                    }

                    // Output last word
                    if (wordBuilder.length() > 0) {
                        printWord(wordBuilder.toString(), wordPositions, writer);
                    }
                }

                private void printWord(String word, List<TextPosition> positions, BufferedWriter writer) throws IOException {
                    if (positions.isEmpty()) return;
                    float fontSize = positions.get(0).getFontSizeInPt(); // Font size of the first character
                    float yPosition = positions.get(0).getYDirAdj(); // Y position of the first character
                    writer.write("Size: " + fontSize + "pt, " + "Y-Position: " + yPosition + " - Word: " + word);
                    writer.newLine(); // Line break after each word
                }
            };

            pdfStripper.setSortByPosition(true); // Sort by position
            pdfStripper.getText(document); // Process the PDF document
        }
    }

    public static void main(String[] args) {
        File pdfFile = new File("src\\PdfToTxt\\Pdf\\Classroom of the Elite (Light Novel) Vol. 2.pdf");
        File outputFile = new File("output.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            analyzeFontSizesAndPositions(pdfFile, writer);
            System.out.println("The text was successfully written to 'output.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
