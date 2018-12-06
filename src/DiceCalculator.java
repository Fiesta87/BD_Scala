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

    public static int jetToucher (int bonus) {
        Random random = new Random();

        int res = random.nextInt(20) + 1;

        // critique, touche automatique (interprété ici par un resultat immense)
        if(res == 20){
            return 9999999;
        }

        return res + bonus;
    }
}
