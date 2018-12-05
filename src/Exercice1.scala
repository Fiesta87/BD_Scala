import org.apache.spark.{SparkConf, SparkContext}

object Exercice1 extends App {

  val conf = new SparkConf().setAppName("Exercice1").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  def exercice1() : Unit = {

    // get the data
    val rdd = sc.parallelize(CreatureCrawler.getCreatures().toArray());

    // set each spell as a key and the creature as the value
    val rdd2 = rdd.map(c => {

      val name = c.asInstanceOf[Creature].name
      var result : List[(String,String)] = List()

      c.asInstanceOf[Creature].spells.foreach(spell => {
        result = result :+ (spell, name)
      })

      result

    })

    // used to flatten the RDD (remove the List aspect created at the previous step)
    val rdd3 = rdd2.flatMap( c => c)

    // reduce by key to group monster in a single string, also remove duplicate
    val rdd4 = rdd3.reduceByKey((val1, val2) => {

      var toAdd = ""

      if(!val1.contains(val2)){
        toAdd = "; " + val2
      }

      val1 + toAdd
    })

    // print result
    rdd4.collect().foreach(println)
  }

  exercice1()
}
