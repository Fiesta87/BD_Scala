import java.util.Random;

public class CombattantFactory {

    private static Random random = new Random();

    private static float addRandom(float range) {
        return (random.nextFloat() * 2.0f - 1.0f) * range;
    }

    public static Combattant copyCombattantWithMsg (Combattant original, MessageChoixAction[] msgs) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), msgs, original.attaques(), original.spells(), original.status(), original.nbTourStatus(), original.SR(), original.DR(), original.Reflex(), original.Will());
    }

    public static Combattant copyCombattant (Combattant original) {
        return new Combattant(original.name(), original.pvActuel(), original.pvMax(), original.AC(), original.positionX(), original.positionY(), original.positionZ(), original.speed(), null, original.attaques(), original.spells(), original.status(), original.nbTourStatus(), original.SR(), original.DR(), original.Reflex(), original.Will());
    }

    public static Combattant makeAngelSolar () {
        Combattant solar =  new Combattant(Combattant.ANGEL_SOLAR(), 363, 363, 44, 0 + addRandom(30.0f), 0, 0 + addRandom(60.0f), 50, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0, 34, 15, 14, 23);
        solar.addAttaque(new Attaque(Attaque.GREAT_SWORD(),18, 3,6, new int[]{35,30,25,20}, 10.0f));
        solar.addAttaque(new Attaque(Attaque.LONG_BOW(),14, 2,6, new int[]{31,26,21,16}, 110.0f));
        solar.addSpell(new Spell(Spell.HEAL(), 0, false, true, 150,0,0));
        solar.addSpell(new Spell(Spell.MASS_HEAL(), 0, true, true, 200,0,0));
        return solar;

    }

    public static Combattant makeOrcWorgRider () {
        Combattant OrcWorgRider = new Combattant(Combattant.ORC_WORG_RIDER(), 13, 13, 18, 110 + addRandom(10.0f), 0, 0 + addRandom(50.0f), 20, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,0,2,1);
        OrcWorgRider.addAttaque(new Attaque(Attaque.BATTLE_AXE(), 2,1,8,new int[]{6}, 10.0f));
        return OrcWorgRider;
    }

    public static Combattant makeOrcBrutalWarlord () {
        Combattant OrcBrutalWarlord =  new Combattant(Combattant.ORC_BRUTAL_WARLORD(), 141, 141, 27, 130 + addRandom(10.0f), 0, 0 + addRandom(50.0f), 30, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,0,9,8);
        OrcBrutalWarlord.addAttaque(new Attaque(Attaque.VICIOUS_FLAIL(), 10,1,8,new int[]{20,15,10}, 10.0f));
        return OrcBrutalWarlord;
    }

    public static Combattant makeOrcDoubleAxeFury () {
        Combattant OrcDoubleAxeFury = new Combattant(Combattant.ORC_DOUBLE_AXE_FURY(), 142, 142, 17, 120 + addRandom(10.0f), 0, 0 + addRandom(50.0f), 40, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,3,9,9);
        OrcDoubleAxeFury.addAttaque(new Attaque(Attaque.DOUBLE_AXE(), 10,1,8,new int[]{19,14,9}, 10.0f));
        return OrcDoubleAxeFury;
    }

    public static Combattant makeAngelPlanetar(){
        Combattant Planetar = new Combattant(Combattant.ANGEL_PLANETAR(), 229, 229,32,0 + addRandom(50.0f),0,0 + addRandom(50.0f),30, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,27,10,11,19);
        Planetar.addAttaque(new Attaque(Attaque.GREAT_SWORD(),15, 3,6, new int[]{27,22,17}, 10.0f));
        return Planetar;
    }

    public static Combattant makeAngelMovanicDeva(){
        Combattant movanicDeva = new Combattant(Combattant.ANGEL_MOVANIC_DEVA(), 126, 126,24,0 + addRandom(50.0f),0,0 + addRandom(50.0f),40, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,21,10,11,9);
        movanicDeva.addAttaque(new Attaque(Attaque.GREAT_SWORD(),7, 2,6, new int[]{17,12,7}, 10.0f));
        return movanicDeva;
    }

    public static Combattant makeAngelAstralDeva(){
        Combattant astralDeva = new Combattant(Combattant.ANGEL_ASTRAL_DEVA(), 172, 172,29,0 + addRandom(50.0f),0,0 + addRandom(50.0f),50, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,25,10,13,11);
        astralDeva.addAttaque(new Attaque(Attaque.WARHAMMER(),14, 1,8, new int[]{26,21,16}, 10.0f));
        return astralDeva;
    }

    public static Combattant makeRedDragon(){
        Combattant redDragon = new Combattant(Combattant.RED_DRAGON(), 449, 449,39,0 + addRandom(50.0f),0,70 + addRandom(40.0f),40, null, new Attaque[]{}, new Spell[]{}, Status.DEGUISE(),0,33,20,14,24);
        redDragon.addAttaque(new Attaque(Attaque.BITE(),24, 4,8, new int[]{37}, 10.0f));
        redDragon.addAttaque(new Attaque(Attaque.CLAW(),16, 4,6, new int[]{37,37}, 10.0f));
        redDragon.addAttaque(new Attaque(Attaque.WINGS(),8, 2,8, new int[]{35,35}, 10.0f));
        redDragon.addAttaque(new Attaque(Attaque.TAIL_SLAP(),24, 4,6, new int[]{35}, 10.0f));
        redDragon.addSpell(new Spell(Spell.FULL_ATTACK(), 0, true, true, 0,0,0));
        redDragon.addSpell(new Spell(Spell.BREATH_WEAPON(), 33, false, true, 24,10,0));
        redDragon.addSpell(new Spell(Spell.POWER_WORD_STUN(), 24, false, true, 1,4,0));
        redDragon.addSpell(new Spell(Spell.SUMMON_DIRE_TIGER(), 0, false, true, 0,0,0));
        return redDragon;
    }

    public static Combattant makeOrcGreatAxe(){
        Combattant greatAxeOrc = new Combattant(Combattant.ORC_GREAT_AXE(), 42, 42,15,100 + addRandom(60.0f),0,0 + addRandom(50.0f),50, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,0,2,3);
        greatAxeOrc.addAttaque(new Attaque(Attaque.GREATAXE(),10, 1,12, new int[]{11}, 10.0f));
        return greatAxeOrc;
    }

    public static Combattant makeOrcAngelSlayer(){
        Combattant angelSlayer = new Combattant(Combattant.ORC_ANGEL_SLAYER(), 112, 112,26,150 + addRandom(60.0f),0,0 + addRandom(50.0f),50, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,0,17,12);
        angelSlayer.addAttaque(new Attaque(Attaque.DOUBLE_AXE(),7, 1,8, new int[]{21,21,16,16,11,11}, 10.0f));
        angelSlayer.addAttaque(new Attaque(Attaque.COMPOSITE_LONG_BOW(),6, 1,8, new int[]{19,14,9}, 110.0f));
        angelSlayer.addSpell(new Spell(Spell.CURE_MODERATE_WOUNDS(), 0, false, true, 2,8,10));
        return angelSlayer;
    }

    public static Combattant makeDireTiger(){
        Combattant direTiger = new Combattant(Combattant.DIRE_TIGER(), 105, 105,17,0 + addRandom(10.0f),0,0 + addRandom(10.0f),40, null, new Attaque[]{}, new Spell[]{}, Status.VIVANT(),0,0,0,11,5);
        direTiger.addAttaque(new Attaque(Attaque.CLAWS_AND_BITE(),16, 4,8, new int[]{18,18,18}, 10.0f));
        return direTiger;
    }
}
