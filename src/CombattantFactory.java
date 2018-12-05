public class CombattantFactory {

    public static Combattant copyCombattantWithMsg (Combattant original, MessageChoixAction msg) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), msg);
    }

    public static Combattant copyCombattant (Combattant original) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), null);
    }

    public static Combattant makeSolar () {
        return new Combattant("Angel Solar", 363, 363, 44, 0, 0, 0, 50, null);
    }

    public static Combattant makeOrcWorgRider () {
        return new Combattant("Orc Worg Rider", 13, 13, 18, 110, 0, 0, 20, null);
    }

    public static Combattant makeOrcBrutalWarlord () {
        return new Combattant("Orc Brutal Warlord", 141, 141, 27, 130, 0, 0, 30, null);
    }

    public static Combattant makeOrcDoubleAxeFury () {
        return new Combattant("Orc Double Axe Fury", 142, 142, 17, 120, 0, 0, 40, null);
    }
}
