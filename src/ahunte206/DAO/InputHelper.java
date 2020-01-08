package ahunte206.DAO;

import java.util.Scanner;

public class InputHelper {

    private final Scanner reader;

    public InputHelper() {
        reader = new Scanner(System.in);
    }

    // Read String
    public String readString(String prompt) {

        System.out.println(prompt + ": ");
        return reader.nextLine();
    }

    //Read File Name
    public String readFileName(String folderPath, String extension){
        String file = folderPath + readString("Please enter the name of the file: ");
        if(!file.endsWith("." + extension)) file = file + "." + extension;
        return file;
    }

    // Read Int
    public int readInt(String prompt, int max, int min) {
        int inputNumber = 0;
        boolean inputError;
        do {
            inputError = false;
            System.out.println(prompt + ": ");

            try {
                inputNumber = Integer.parseInt(reader.nextLine());
                if (inputNumber < min || inputNumber > max) {
                    inputError = true;
                    System.out.println("Number out of range: please re-enter\n");
                }
            } catch (NumberFormatException e) {
                inputError = true;
                System.out.println("Not a valid number: please re-enter: ");
            }
        } while (inputError);
        return inputNumber;
    }

    // Read Int
    public int readInt(String prompt) {
        int inputNumber = 0;
        boolean inputError;
        do {
            inputError = false;
            System.out.println(prompt + ": ");

            try {
                inputNumber = Integer.parseInt(reader.nextLine());
            } catch (NumberFormatException e) {
                inputError = true;
                System.out.println("Not a valid number: please re-enter: ");
            }
        } while (inputError);
        return inputNumber;
    }

}
