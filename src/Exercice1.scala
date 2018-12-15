import org.apache.spark.{SparkConf, SparkContext}

object Exercice1 extends App {

  val conf = new SparkConf().setAppName("Exercice1").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  def exercice1() : Unit = {

    // get the data
    val creatures = sc.parallelize(CreatureCrawler.findCreatures().toArray())

    // set each spell as a key and the creature as the value
    val pairsSpellCreature = creatures.flatMap(c => {

      val name = c.asInstanceOf[Creature].name
      var result : List[(String,String)] = List()

      c.asInstanceOf[Creature].spells.foreach(spell => {
        result :+= (spell, name)
      })

      result
    })

    // reduce by key to group creatures in a single string, also remove duplicate
    val listCreaturesParSpell = pairsSpellCreature.reduceByKey((val1, val2) => {

      var toAdd = ""

      if(!val1.contains(val2) && !val2.contains(val1)){
        toAdd = Console.WHITE + "; " + Console.MAGENTA + Console.BOLD + val2
      }

      val1 + toAdd
    })

    // print result in a nice way
    listCreaturesParSpell.collect().foreach(record => {
      println("Spell : " + Console.CYAN + Console.BOLD + record._1 + Console.WHITE + ", Creatures : " + Console.MAGENTA + Console.BOLD + record._2 + Console.WHITE)
    })
  }

  exercice1()
}
