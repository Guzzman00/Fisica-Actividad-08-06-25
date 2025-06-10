import org.scalajs.dom
import org.scalajs.dom.{document, window, HTMLCanvasElement, CanvasRenderingContext2D, HTMLElement, HTMLParagraphElement, HTMLButtonElement, Element}
import scala.util.Random
import scala.math.{Pi, sin, floor}
import scala.scalajs.js

// Case class para mantener los datos de la onda de forma organizada y tipada.
case class WaveData(
                     numCycles: Int,
                     totalTime: Double,
                     amplitude: Double,
                     wavelength: Double,
                     period: Double,
                     frequency: Double,
                     speed: Double
                   )

// --- CAMBIO CLAVE ---
// Se ha quitado la anotación `@JSExportTopLevel("default")`.
// El `build.sbt` ya está configurado para encontrar y ejecutar este objeto `Main`
// como punto de entrada de la aplicación. Esta es la forma más moderna y robusta de hacerlo.
object Main {

  private def getElementById[T <: Element](id: String): Option[T] =
    Option(document.getElementById(id)).map(_.asInstanceOf[T])

  // Variables para los elementos del DOM.
  private lazy val canvasOpt = getElementById[HTMLCanvasElement]("waveCanvas")
  private lazy val ctxOpt = canvasOpt.map(_.getContext("2d").asInstanceOf[CanvasRenderingContext2D])
  private lazy val problemTextOpt = getElementById[HTMLParagraphElement]("problem-text")
  private lazy val newExerciseBtnOpt = getElementById[HTMLButtonElement]("new-exercise-btn")
  private lazy val showAnswerBtnOpt = getElementById[HTMLButtonElement]("show-answer-btn")
  private lazy val answerCardOpt = getElementById[HTMLElement]("answer-card")
  private lazy val answerContentOpt = getElementById[HTMLElement]("answer-content")

  // Variable para mantener el estado actual del ejercicio.
  private var waveDataOpt: Option[WaveData] = None

  def main(args: Array[String]): Unit = {
    // El código se ejecuta cuando el DOM está completamente cargado.
    document.addEventListener("DOMContentLoaded", (e: dom.Event) => {
      setupUI()
      generateExercise()
    })
  }

  private def setupUI(): Unit = {
    // Asignar eventos a los botones.
    newExerciseBtnOpt.foreach(_.onclick = (e: dom.MouseEvent) => generateExercise())
    showAnswerBtnOpt.foreach(_.onclick = (e: dom.MouseEvent) => showAnswers())
    // Redibujar el canvas si la ventana cambia de tamaño.
    window.addEventListener("resize", (e: dom.Event) => draw())
  }

  private def generateExercise(): Unit = {
    // Generar valores aleatorios para el problema.
    val numCycles = Random.nextInt(4) + 2 // 2 a 5 ciclos
    val totalTime = (Random.nextInt(6) + 2) * numCycles
    val amplitude = Random.nextInt(4) + 1
    val wavelength = (Random.nextInt(4) + 2) * 2

    // Calcular las respuestas.
    val period = totalTime.toDouble / numCycles
    val frequency = 1 / period
    val speed = wavelength * frequency

    val currentWaveData = WaveData(numCycles, totalTime, amplitude, wavelength, period, frequency, speed)
    waveDataOpt = Some(currentWaveData)

    // Actualizar el texto del problema en el HTML.
    problemTextOpt.foreach { p =>
      p.innerHTML = s"La siguiente onda completa <strong>${currentWaveData.numCycles} ciclos</strong> en un tiempo total de <strong>${currentWaveData.totalTime} segundos</strong>. Analiza la gráfica y calcula sus propiedades."
    }

    // Ocultar las respuestas y dibujar la nueva onda.
    answerCardOpt.foreach(_.classList.add("answer-hidden"))
    draw()
  }

  private def draw(): Unit = {
    // Solo dibujar si tenemos datos y un contexto de canvas.
    (for {
      waveData <- waveDataOpt
      canvas <- canvasOpt
      ctx <- ctxOpt
    } yield {

      val padding = 40.0
      val gridSquareSize = 20.0
      val totalDistance = waveData.numCycles * waveData.wavelength

      val containerWidth = canvas.parentElement.clientWidth
      canvas.width = containerWidth

      val maxY = waveData.amplitude + 2
      val requiredHeight = (2 * maxY) * gridSquareSize + 2 * padding
      canvas.height = requiredHeight.toInt

      val xPixelsPerUnit = (canvas.width - 2 * padding) / totalDistance

      ctx.clearRect(0, 0, canvas.width, canvas.height)
      ctx.fillStyle = "#f9fafb"
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      // Dibujar la cuadrícula
      ctx.beginPath()
      ctx.strokeStyle = "#e5e7eb"
      ctx.lineWidth = 1
      for (i <- 0 until (canvas.width / gridSquareSize).toInt) {
        ctx.moveTo(i * gridSquareSize, 0)
        ctx.lineTo(i * gridSquareSize, canvas.height)
      }
      for (i <- 0 until (canvas.height / gridSquareSize).toInt) {
        ctx.moveTo(0, i * gridSquareSize)
        ctx.lineTo(canvas.width, i * gridSquareSize)
      }
      ctx.stroke()

      // Dibujar ejes
      val originX = padding
      val originY = canvas.height / 2.0

      ctx.beginPath()
      ctx.strokeStyle = "#4b5563"
      ctx.lineWidth = 2
      ctx.moveTo(0, originY)
      ctx.lineTo(canvas.width, originY)
      ctx.moveTo(originX, 0)
      ctx.lineTo(originX, canvas.height)
      ctx.stroke()

      // Dibujar etiquetas de los ejes
      ctx.fillStyle = "#4b5563"
      ctx.font = "12px Inter"
      ctx.textAlign = "center"
      ctx.textBaseline = "middle"

      val yGridUnits = floor(waveData.amplitude).toInt + 1
      for (i <- -yGridUnits to yGridUnits if i != 0) {
        val yPos = originY - i * gridSquareSize
        ctx.fillText(i.toString, originX - 20, yPos)
      }

      val xGridUnits = totalDistance
      for (i <- 1 to xGridUnits.toInt if i % waveData.wavelength.toInt == 0 || i % 2 == 0) {
        val xPos = originX + i * xPixelsPerUnit
        ctx.fillText(i.toString, xPos, originY + 20)
      }

      // Dibujar la onda
      ctx.beginPath()
      ctx.strokeStyle = "#3b82f6"
      ctx.lineWidth = 3

      val pixelsPerMeterX = (canvas.width - 2 * padding) / totalDistance

      for (xPx <- 0 until (canvas.width - originX).toInt) {
        val xMeters = xPx / pixelsPerMeterX
        val yMeters = waveData.amplitude * sin((2 * Pi / waveData.wavelength) * xMeters)
        val yPx = originY - yMeters * gridSquareSize

        if (xPx == 0) ctx.moveTo(originX, yPx)
        else ctx.lineTo(originX + xPx, yPx)
      }
      ctx.stroke()
    }).getOrElse(println("Error: No se pudo dibujar. Faltan datos o elementos del DOM."))
  }

  private def showAnswers(): Unit = {
    waveDataOpt.foreach { wd =>
      answerContentOpt.foreach { content =>
        // Usamos interpolación de strings de Scala para generar el HTML de forma segura.
        content.innerHTML = s"""
          <div class="p-4 bg-blue-50 border-l-4 border-blue-400 rounded">
              <h3 class="font-bold">1. Amplitud (A)</h3>
              <p>Es la máxima elongación o altura de la onda desde su punto de equilibrio. Se puede leer directamente en el eje Y.</p>
              <p class="mt-1 font-mono bg-blue-100 p-2 rounded"><strong>A = ${"%.2f".format(wd.amplitude)} m</strong></p>
          </div>
          <div class="p-4 bg-green-50 border-l-4 border-green-400 rounded">
              <h3 class="font-bold">2. Longitud de Onda (λ)</h3>
              <p>Es la distancia que recorre una onda en un ciclo completo. Se puede medir en el eje X, desde un punto hasta el siguiente punto idéntico (ej. de cresta a cresta).</p>
              <p class="mt-1 font-mono bg-green-100 p-2 rounded"><strong>λ = ${"%.2f".format(wd.wavelength)} m</strong></p>
          </div>
          <div class="p-4 bg-yellow-50 border-l-4 border-yellow-400 rounded">
              <h3 class="font-bold">3. Periodo (T)</h3>
              <p>Es el tiempo que tarda en completarse un solo ciclo. Se calcula dividiendo el tiempo total por el número de ciclos.</p>
              <p class="mt-1 font-mono bg-yellow-100 p-2 rounded">T = Tiempo Total / N° de Ciclos<br>T = ${wd.totalTime} s / ${wd.numCycles} ciclos<br><strong>T = ${"%.2f".format(wd.period)} s</strong></p>
          </div>
          <div class="p-4 bg-purple-50 border-l-4 border-purple-400 rounded">
              <h3 class="font-bold">4. Frecuencia (f)</h3>
              <p>Es el número de ciclos que ocurren por segundo. Es el inverso del periodo (f = 1/T).</p>
              <p class="mt-1 font-mono bg-purple-100 p-2 rounded">f = 1 / T<br>f = 1 / ${"%.2f".format(wd.period)} s<br><strong>f = ${"%.2f".format(wd.frequency)} Hz</strong></p>
          </div>
          <div class="p-4 bg-red-50 border-l-4 border-red-400 rounded">
              <h3 class="font-bold">5. Rapidez de Propagación (v)</h3>
              <p>Es la velocidad a la que se mueve la onda. Se calcula multiplicando la longitud de onda por la frecuencia.</p>
              <p class="mt-1 font-mono bg-red-100 p-2 rounded">v = λ * f<br>v = ${"%.2f".format(wd.wavelength)} m * ${"%.2f".format(wd.frequency)} Hz<br><strong>v = ${"%.2f".format(wd.speed)} m/s</strong></p>
          </div>
        """
        answerCardOpt.foreach(_.classList.remove("answer-hidden"))
        answerCardOpt.foreach(_.scrollIntoView(true)) // true es equivalente a { behavior: 'smooth' } en algunos navegadores
      }
    }
  }
}