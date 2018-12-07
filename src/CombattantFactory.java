public class CombattantFactory {

    public static Combattant copyCombattantWithMsg (Combattant original, MessageChoixAction[] msgs) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), msgs, original.attaques(), original.spells(),Combattant.STATUS_VIVANT(),0, original.SR(), original.DR(), original.Reflex(), original.Will());
    }

    public static Combattant copyCombattant (Combattant original) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), null, original.attaques(), original.spells(),Combattant.STATUS_VIVANT(),0, original.SR(), original.DR(), original.Reflex(), original.Will());
    }

    public static Combattant makeSolar () {
        Combattant solar =  new Combattant("Angel Solar", 363, 363, 44, 0, 0, 0, 50, null, new Attaque[]{}, new Spell[]{},Combattant.STATUS_VIVANT(),0, 34, 15, 14, 23);
        solar.addAttaque(new Attaque("great sword",18, 3,6, new int[]{35,30,25,20}));
        solar.addAttaque(new Attaque("long bow",14, 2,6, new int[]{31,26,21,16}));
        solar.addSpell(new Spell("Heal", 0, false, 150,0,0));
        solar.addSpell(new Spell("Mass Heal", 0, true, 200,0,0));
        return solar;

    }

    public static Combattant makeOrcWorgRider () {
        Combattant OrcWorgRider = new Combattant("Orc Worg Rider", 13, 13, 18, 110, 0, 0, 20, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,0,0,2,1);
        OrcWorgRider.addAttaque(new Attaque("battle axe", 2,1,8,new int[]{6}));
        return OrcWorgRider;
    }

    public static Combattant makeOrcBrutalWarlord () {
        Combattant OrcBrutalWarlord =  new Combattant("Orc Brutal Warlord", 141, 141, 27, 130, 0, 0, 30, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,0,0,9,8);
        OrcBrutalWarlord.addAttaque(new Attaque("vicious flail", 10,1,8,new int[]{20,15,10}));
        return OrcBrutalWarlord;
    }

    public static Combattant makeOrcDoubleAxeFury () {
        Combattant OrcDoubleAxeFury = new Combattant("Orc Double Axe Fury", 142, 142, 17, 120, 0, 0, 40, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,0,3,9,9);
        OrcDoubleAxeFury.addAttaque(new Attaque("orc double axe", 10,1,8,new int[]{19,14,9}));
        return OrcDoubleAxeFury;
    }

    public static Combattant makePlanetar(){
        Combattant Planetar = new Combattant("Angel Planetar", 229, 229,32,0,0,0,30, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,27,10,11,19);
        Planetar.addAttaque(new Attaque("great sword",15, 3,6, new int[]{27,22,17}));
        return Planetar;
    }

    public static Combattant makeMovanicDeva(){
        Combattant movanicDeva = new Combattant("Angel Movanic Deva", 126, 126,24,0,0,0,40, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,21,10,11,9);
        movanicDeva.addAttaque(new Attaque("great sword",7, 2,6, new int[]{17,12,7}));
        return movanicDeva;
    }

    public static Combattant makeAstralDeva(){
        Combattant astralDeva = new Combattant("Angel Astral Deva", 172, 172,29,0,0,0,50, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,25,10,13,11);
        astralDeva.addAttaque(new Attaque("warhammer",14, 1,8, new int[]{26,21,16}));
        return astralDeva;
    }

    public static Combattant makeRedDragon(){
        Combattant redDragon = new Combattant("Red Dragon", 449, 449,39,0,0,0,40, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,33,20,14,24);
        redDragon.addAttaque(new Attaque("bite",24, 4,8, new int[]{37}));
        redDragon.addAttaque(new Attaque("claw",16, 4,6, new int[]{37,37}));
        redDragon.addAttaque(new Attaque("wings",8, 2,8, new int[]{35,35}));
        redDragon.addAttaque(new Attaque("tail slap",24, 4,6, new int[]{35}));
        redDragon.addSpell(new Spell("Breath weapon", 33, false, 24,10,0));
        redDragon.addSpell(new Spell("power word stun", 24, false, 1,4,0));
        return redDragon;
    }

    public static Combattant makeGreatAxeOrc(){
        Combattant greatAxeOrc = new Combattant("Great Axe Orc", 42, 42,15,0,0,0,50, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,0,0,2,3);
        greatAxeOrc.addAttaque(new Attaque("greataxe",10, 1,12, new int[]{11}));
        return greatAxeOrc;
    }

    public static Combattant makeAngelSlayer(){
        Combattant angelSlayer = new Combattant("Angel Slayer", 112, 112,26,0,0,0,50, null, new Attaque[]{}, new Spell[]{}, Combattant.STATUS_VIVANT(),0,0,0,17,12);
        angelSlayer.addAttaque(new Attaque("double axe",7, 1,8, new int[]{21,21,16,16,11,11}));
        angelSlayer.addAttaque(new Attaque("longbow",6, 1,8, new int[]{19,14,9}));
        angelSlayer.addSpell(new Spell("cure moderate wounds", 0, false, 2,8,10));
        return angelSlayer;
    }


}
