public class IllegalPIDValueException extends Exception {
    public IllegalPIDValueException(){
        super("Can't add a pre-existing PID value.");
    }
}
