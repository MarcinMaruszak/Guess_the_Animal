package animals;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String language = "en";
        String type = "json";
        if(args.length>0){
            //language = args[2].replaceAll(".+=", "");
            type = args[1];
            System.out.println(Arrays.toString(args
            ));
        }

        new UserInterface(new Scanner(System.in), type, language).start();

    }
}
