import scala.math.sqrt

class Combattant(val name : String, var pvActuel : Int, val pvMax : Int, val AC : Int, var positionX : Float, var positionY : Float, var positionZ : Float, val speed : Float, val msgsRetenu : Array[MessageChoixAction], var attaques: Array[Attaque], var spells: Array[Spell], var status : String, var nbTourStatus : Int , val SR : Int, val DR : Int, val Reflex : Int, val Will : Int) extends Serializable {

  // retourne le vecteur de déplacement qui doit être appliqué au combattant pour le rapprocher de sa cible
  // remarque : bien q'utilisant un vecteur 3 pour la position, les combattant ne se déplacent pas sur Y
  //            avec cette méthode mais à l'aide du système de vol
  def moveToward(combattant: Combattant): (Float, Float, Float) = {
    var dx = combattant.positionX - this.positionX
    var dy = 0.0f // car les combattant ne volent pas via cette méthode
    var dz = combattant.positionZ - this.positionZ

    val norme = sqrt(dx*dx + dy*dy + dz*dz).asInstanceOf[Float]

    var usefulSpeed : Float = speed

    if(speed > norme){
      usefulSpeed = norme
    }

    dx = (dx / norme) * usefulSpeed
    dy = (dy / norme) * usefulSpeed
    dz = (dz / norme) * usefulSpeed

    (dx,dy, dz)
  }

  def addAttaque(attaque: Attaque): Unit ={
    attaques :+= attaque
  }

  def addSpell(spell: Spell): Unit ={
    spells :+= spell
  }
}

object Combattant {
  val ANGEL_SOLAR = "Angel Solar"
  val ORC_WORG_RIDER = "Orc Worg Rider"
  val ORC_BRUTAL_WARLORD = "Orc Brutal Warlord"
  val ORC_DOUBLE_AXE_FURY = "Orc Double Axe Fury"

  val ANGEL_PLANETAR = "Angel Planetar"
  val ANGEL_MOVANIC_DEVA = "Angel Movanic Deva"
  val ANGEL_ASTRAL_DEVA = "Angel Astral Deva"
  val RED_DRAGON = "Red Dragon"
  val ORC_GREAT_AXE = "Orc Great Axe"
  val ORC_ANGEL_SLAYER = "Orc Angel Slayer"
  val DIRE_TIGER = "Dire Tiger"
}

object Status {
  val VIVANT = "vivant"
  val MORT = "mort"
  val STUNNED = "stunned"
  val DEGUISE = "deguise"
}