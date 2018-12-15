class Attaque(val nom : String, val degatFixe: Int, val nbDes : Int, val valeurDes : Int, val touches : Array[Int], val portee : Float) extends Serializable {

}

object Attaque {
  val GREAT_SWORD = "great sword"
  val LONG_BOW = "long bow"
  val BATTLE_AXE = "battle axe"
  val VICIOUS_FLAIL = "vicious flail"
  val DOUBLE_AXE = "double axe"
  val WARHAMMER = "warhammer"
  val BITE = "bite"
  val CLAW = "claw"
  val WINGS = "wings"
  val TAIL_SLAP = "tail slap"
  val GREATAXE = "greataxe"
  val COMPOSITE_LONG_BOW = "composite long bow"
  val CLAWS_AND_BITE = "claws and bite"
}