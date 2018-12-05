import org.apache.spark.rdd.RDD
import org.apache.spark.graphx._
import org.apache.spark.{SparkConf, SparkContext}

import scala.math.sqrt

object Exercice2 extends App
{
  val conf = new SparkConf().setAppName("Exercice2").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.setLogLevel("ERROR")

  def exercice2() : Unit = {

    println("Populating armies")

    val ANGEL_SOLAR = "Angel Solar"

    var vertexArray = Array((0L, CombattantFactory.makeSolar()))

    val ALLY = true
    val FOE = false
    var edgeArray = Array[Edge[Boolean]]()

    val ORC_WORG_RIDER = "Orc Worg Rider"

    for( i <- 1L to 9L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcWorgRider())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
    }

    val ORC_BRUTAL_WARLORD = "Orc Brutal Warlord"

    vertexArray = vertexArray :+ (10L, CombattantFactory.makeOrcBrutalWarlord())
    edgeArray = edgeArray :+ Edge(0L, 10L, FOE)
    edgeArray = edgeArray :+ Edge(10L, 0L, FOE)

    val ORC_DOUBLE_AXE_FURY = "Orc Double Axe Fury"

    for( i <- 11L to 14L){
      vertexArray = vertexArray :+ (i, CombattantFactory.makeOrcDoubleAxeFury())
      edgeArray = edgeArray :+ Edge(0L, i, FOE)
      edgeArray = edgeArray :+ Edge(i, 0L, FOE)
    }

    val vertexRDD: RDD[(Long, Combattant)] = sc.parallelize(vertexArray)
    val edgeRDD: RDD[Edge[Boolean]] = sc.parallelize(edgeArray)
    var myGraph: Graph[Combattant, Boolean] = Graph(vertexRDD, edgeRDD)

    val ACTION_ATTAQUER = "attaquer"
    val ACTION_HEAL = "heal"
    val ACTION_DEPLACEMENT = "deplacement"

    // messages des actions possibles / demandées
    def sendMessagesChoixAction(ctx: EdgeContext[Combattant, Boolean, MessageChoixAction]): Unit = {

      if(ctx.srcAttr.pvActuel > 0 && ctx.dstAttr.pvActuel > 0){
        //println(ctx.srcAttr.name + " " + ctx.srcId + " ---- " + ctx.dstAttr.name + " " + ctx.dstId)
        if(ctx.attr == FOE) {

          var distance = calculeDistance(ctx.srcAttr, ctx.dstAttr)

          //println("distance " + ctx.srcAttr.name + " " + ctx.srcId + " ---- " + ctx.dstAttr.name + " " + ctx.dstId + " : " + distance)

          if(ctx.srcAttr.name == ANGEL_SOLAR) {
            if(distance <= 110){
              //println(ctx.srcAttr.name + " " + ctx.srcId + " recoit " + ACTION_ATTAQUER + " contre " + ctx.dstAttr.name + " " + ctx.dstId)
              ctx.sendToSrc(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId))
            }
          }

          else {

            if(distance > 10) {
              //println(ctx.srcAttr.name + " " + ctx.srcId + " recoit " + ACTION_DEPLACEMENT + " contre " + ctx.dstAttr.name + " " + ctx.dstId)
              ctx.sendToSrc(MessageFactory.makeMessageChoixAction(distance, ACTION_DEPLACEMENT, ctx.dstId))
            }

            else {
              //println(ctx.srcAttr.name + " " + ctx.srcId + " recoit " + ACTION_ATTAQUER + " contre " + ctx.dstAttr.name + " " + ctx.dstId)
              ctx.sendToSrc(MessageFactory.makeMessageChoixAction(distance, ACTION_ATTAQUER, ctx.dstId))
            }
          }
        }
      }
    }

    def selectBestAction(msg1: MessageChoixAction, msg2: MessageChoixAction): MessageChoixAction = {

      //println("messages 1 : " + msg1.cible + ", " + msg1.action + " || messages 2 : " + msg2.cible + ", " + msg2.action)

      // On priorise le heal
      if(msg1.action == ACTION_HEAL) msg1
      if(msg2.action == ACTION_HEAL) msg2

      // si on doit choisir entre se déplacer et attaquer, on attaque
      if(msg1.action == ACTION_DEPLACEMENT && msg2.action == ACTION_ATTAQUER) msg2
      if(msg2.action == ACTION_DEPLACEMENT && msg1.action == ACTION_ATTAQUER) msg1

      // sinon on attaque le plus proche
      if(msg2.distance > msg1.distance) msg1
      else msg2
    }

    def calculeDistance(combattant1: Combattant, combattant2: Combattant) : Float = {
      var dx = combattant1.positionX - combattant2.positionX
      var dy = combattant1.positionY - combattant2.positionY
      var dz = combattant1.positionZ - combattant2.positionZ

      sqrt(dx*dx + dy*dy + dz*dz).asInstanceOf[Float]
    }

    def sendMessagesRealisationAction(ctx: EdgeContext[Combattant, Boolean, Array[MessageRealisationAction]]): Unit = {

      if(ctx.srcAttr.msgRetenu != null && ctx.srcAttr.msgRetenu.cible == ctx.dstId) {

        if(ctx.srcAttr.msgRetenu.action == ACTION_HEAL) {

        } else if(ctx.srcAttr.msgRetenu.action == ACTION_DEPLACEMENT) {

          var deplacement = ctx.srcAttr.moveToward(ctx.dstAttr)

          ctx.sendToSrc(Array(MessageFactory.makeMessageRealisationAction(ACTION_DEPLACEMENT, deplacement._1, deplacement._2, deplacement._3)))

        } else if(ctx.srcAttr.msgRetenu.action == ACTION_ATTAQUER) {

          if(ctx.srcAttr.name == ANGEL_SOLAR) {

            if(ctx.srcAttr.msgRetenu.distance <= 10) {  // attaque au CàC avec greatsword

              ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator._x_Dy_plus_z_(1, 20, 35), DiceCalculator._x_Dy_plus_z_(3, 6, 18))))

            } else if(ctx.srcAttr.msgRetenu.distance <= 110) {  // attaque à distance avec Arc

              ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator._x_Dy_plus_z_(1, 20, 31), DiceCalculator._x_Dy_plus_z_(2, 6, 14))))
            }
          }

          else if(ctx.srcAttr.name == ORC_WORG_RIDER) {

           ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator._x_Dy_plus_z_(1, 20, 6), DiceCalculator._x_Dy_plus_z_(1, 8, 2))))
          }

          else if(ctx.srcAttr.name == ORC_BRUTAL_WARLORD) {

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator._x_Dy_plus_z_(1, 20, 20), DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
          }

          else if(ctx.srcAttr.name == ORC_DOUBLE_AXE_FURY) {

            ctx.sendToDst(Array(MessageFactory.makeMessageRealisationAction(ACTION_ATTAQUER, DiceCalculator._x_Dy_plus_z_(1, 20, 19), DiceCalculator._x_Dy_plus_z_(1, 8, 10))))
          }
        }
      }
    }

    def combineAction(msg1: Array[MessageRealisationAction], msg2: Array[MessageRealisationAction]): Array[MessageRealisationAction] = {

      msg1 ++ msg2
    }

    println("Fighting")

    var tourCombat = 1

    while(true){

      println("")
      println("#########")
      println("Tour " + tourCombat)
      println("#########")
      println("")

      var messagesChoixActions = myGraph.aggregateMessages[MessageChoixAction](
        sendMessagesChoixAction,
        selectBestAction,
        TripletFields.All
      )

      if (messagesChoixActions.isEmpty()) {
        println("Fin du combat")
        return
      }

      myGraph = myGraph.joinVertices(messagesChoixActions)(
        (id, combattant, msgRetenu) => {

          var combattantResult = CombattantFactory.copyCombattantWithMsg(combattant, msgRetenu)

          combattantResult
        })

      var affichageChoixActions = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageChoixActions){
          affichageChoixActions = false
          println("Choix des Actions")
          println("")
        }

        if(combattant.msgRetenu == null) {
          println(combattant.name + " " + id + " est mort !")
        } else {
          println(combattant.name + " " + id + " : " + combattant.msgRetenu.action + ", cible : " + combattant.msgRetenu.cible)
        }
      }}

      println("***************************************")

      var messagesRealisationActions = myGraph.aggregateMessages[Array[MessageRealisationAction]](
        sendMessagesRealisationAction,
        combineAction,
        TripletFields.All
      )

      myGraph = myGraph.joinVertices(messagesRealisationActions)(
        (id, combattant, msgs) => {

          var combattantResult = CombattantFactory.copyCombattant(combattant)

          msgs.foreach( msg => {

            //println(msg.action + " : " + combattantResult.name + " " + id)

            if(msg.action == ACTION_ATTAQUER) {

              if(msg.valeur1 >= combattantResult.AC) {
                combattantResult.pvActuel = combattantResult.pvActuel - msg.valeur2.asInstanceOf[Int]
                if(combattantResult.pvActuel < 0){
                  combattantResult.pvActuel = 0
                }
              }
            }

            else if(msg.action == ACTION_HEAL) {
              combattantResult.pvActuel = combattantResult.pvActuel + msg.valeur2.asInstanceOf[Int]
              if(combattantResult.pvActuel > combattantResult.pvMax){
                combattantResult.pvActuel = combattantResult.pvMax
              }
            }

            else if(msg.action == ACTION_DEPLACEMENT) {
              combattantResult.positionX = combattantResult.positionX + msg.valeur1
              combattantResult.positionY = combattantResult.positionY + msg.valeur2
              combattantResult.positionZ = combattantResult.positionZ + msg.valeur3
            }
          })

          combattantResult
        })

      var affichageStatus = true

      myGraph.vertices.collect.foreach { case (id, combattant: Combattant) => {

        if(affichageStatus){
          affichageStatus = false
          println("")
          println("Status")
          println("")
        }
        println(combattant.name + " " + id  + " : " + combattant.pvActuel + "/" + combattant.pvMax )
      }}

      tourCombat = tourCombat + 1
    }
  }

  exercice2()

}
