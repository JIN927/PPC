package upmc.akka.leader

import java.util
import java.util.Date

import akka.actor._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class Tick
case class CheckerTick () extends Tick

class CheckerActor (val id:Int, val terminaux:List[Terminal], electionActor:ActorRef) extends Actor {

     var time : Int = 200
     val father = context.parent

     var nodesAlive:List[Int] = List()
     var datesForChecking:List[Date] = List()
     var lastDate:Date = null

     var leader : Int = -1

    def receive = {

         // Initialisation
        case Start => {
			 println("current id=="+ id)
             self ! CheckerTick
        }

        // A chaque fois qu'on recoit un Beat : on met a jour la liste des nodes
        case IsAlive (nodeId) => {
             var index = 0
             if (!nodesAlive.contains(nodeId)){
                nodesAlive = nodeId::nodesAlive
                datesForChecking = new Date()::datesForChecking
               }else{ 
                   //mise a jour les listes
                    datesForChecking.foreach(n=>{
                        datesForChecking.updated(index, new Date())
                        index = index+ 1
                   })
                   
               }
        }

        case IsAliveLeader (nodeId) => {
 			leader = nodeId
            father ! Message ("The leader is alive")
            self ! IsAlive(nodeId)
        }

        // A chaque fois qu'on recoit un CheckerTick : on verifie qui est mort ou pas
        // Objectif : lancer l'election si le leader est mort
        case CheckerTick => {
            this.lastDate = new Date()
            var i = 0
            datesForChecking.foreach(n=>{
                
                if(this.lastDate.getTime - n.getTime()> time){
                    
                    if (nodesAlive(i)== leader){
                        println("8888888888888")
                    electionActor ! StartWithNodeList(nodesAlive)
                    }
                    
                }
                i = i+1
            })

			Thread.sleep(time)
			self ! CheckerTick
        }

    }
}
