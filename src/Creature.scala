import scala.collection.mutable.ArrayBuffer

class Creature(val name : String) extends Serializable {
  var spells =  ArrayBuffer[String]()
  def addspell(spell : String) : Unit = {
    spells += spell
  }
}
