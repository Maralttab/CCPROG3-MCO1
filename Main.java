import java.util.Random;


 //Entry point of the Candy Courier program.
 
public class Main {

    
     //Starts the game using the console.
    public static void main(String[] args) {
        InputHelper input = new InputHelper(System.in);
        try {
            new Game(input, new Random()).start();
        } catch (InputHelper.InputClosedException e) {
            System.out.println();
            System.out.println("Input ended. Exiting the game.");
        }
    }
}