import java.util.Random;

public class DiceCalculator {

    public static int _x_Dy_plus_z_ (int x, int y, int z) {
        int res = z;

        Random random = new Random();

        for(int i=0; i<x; i++){
            res += random.nextInt(y) + 1;
        }

        return res;
    }
}
