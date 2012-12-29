package slack

import scala.react._
import scala.react.Domain
import slackdom._

// Initial goal: set up a signal that reports the current time,
// and show it in a Slick2D app.

import org.newdawn.slick.AppGameContainer
import org.newdawn.slick.{GameContainer, Graphics, Color}

object SlackTest extends ReactiveApp {
  val container = new AppGameContainer(new SlackTest, 800, 600, false)

  container.setTargetFrameRate(20)
  container.start
}

// Signal examples

// Signals are event sources with continuously varying values.
// Example of Var -- externally modifiable Signal.
case class Clock {
  val sig = Var[String]("")

  // Events generated from our signal
  val ticks = sig.changes 

  var turnNumber = 0
  def clockTick {
    val newDate:String = new java.util.Date().toString
    sig update newDate
    turnNumber += 1
  }
}

// this doesn't work
case class Clock2 {
  val sig = Signal.flow("")(self => {
    self update (new java.util.Date().toString)
    self.pause
  })
  val ticks = sig.changes
}

case class ClockObserver(clockEvents:Events[String]) extends Observing {
  var currentTime:String = ""
  schedule { obs } 
  def obs {
    observe(clockEvents)( timeSignal => { 
      println(s"observed clock event: ${timeSignal}")
      currentTime = timeSignal })
  }
}


class SlackTest extends ReactiveGame("Slack Test") with Observing {
  var current = new java.util.Date()
  val clock = Clock()

  // map below is an event combinator from scala.react
  val observer = ClockObserver(clock.ticks map {"Sig Var test: " + _})
  
  def obsInput {
    observe(inputEvents.keyPressed)( event => println("event: " + event))
  }
  schedule { obsInput }

  override def init(gc: GameContainer) {
    println("Slack test started.")
  }

  // Execute a turn per update
  override def update(gc: GameContainer, delta: Int) {
    current = new java.util.Date()
    clock.clockTick
  }

  override def render(gc: GameContainer, g: Graphics) {
    g.setColor(Color.white)
    g.drawString("Time signal: " + observer.currentTime, 200, 10)
  }
}
