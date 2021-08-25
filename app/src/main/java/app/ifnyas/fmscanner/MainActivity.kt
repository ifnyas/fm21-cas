package app.ifnyas.fmscanner

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.text.InputType.TYPE_NUMBER_VARIATION_NORMAL
import android.view.Gravity.CENTER
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.ifnyas.fmscanner.databinding.ActivityMainBinding
import app.ifnyas.fmscanner.model.Player
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
import java.io.IOException
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.math.absoluteValue


@Suppress("PrivatePropertyName")
class MainActivity : AppCompatActivity() {

    @Suppress("unused")
    private val TAG by lazy { javaClass.simpleName }
    private val reqCodePermissions by lazy { 23 }
    private val reqPermissions by lazy { arrayOf(CAMERA) }
    private val cameraExecutor by lazy { newSingleThreadExecutor() }
    private val player by lazy { Player() }
    private lateinit var vb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        initFun()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == reqCodePermissions) {
            if (permitGranted()) initCamera() else {
                Toast.makeText(
                    this,
                    "Permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        MaterialDialog(this).show {
            lifecycleOwner(this@MainActivity)
            title(text = "Close app?")
            negativeButton(text = "Back") { dismiss() }
            positiveButton(text = "Close") { finishAfterTransition() }
        }
    }

    private fun initFun() {
        initBtn()
        if (permitGranted()) initCamera() else reqPermissions()
    }

    private fun initBtn() {
        vb.fabHelp.setOnClickListener { openHelpDialog() }
        vb.btnManual.setOnClickListener {
            player.setZeroAttrsToOne()
            openReportDialog()
        }
    }

    private fun reqPermissions() {
        ActivityCompat.requestPermissions(
            this,
            reqPermissions,
            reqCodePermissions
        )
    }

    private fun permitGranted() = reqPermissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PERMISSION_GRANTED
    }

    private fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9).build()
            .also { it.setAnalyzer(cameraExecutor, TextReaderAnalyzer(::onTextFound)) }

        cameraProviderFuture.addListener({
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(vb.viewCamera.surfaceProvider) }
            cameraProviderFuture.get().bind(preview, imageAnalyzer)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun ProcessCameraProvider.bind(
        preview: Preview,
        imageAnalyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@MainActivity,
            DEFAULT_BACK_CAMERA,
            preview,
            imageAnalyzer
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }

    @SuppressLint("SetTextI18n")
    private fun onTextFound(visionText: Text) {
        // init val
        val onlyNameLines = mutableListOf<Text.Line>()
        val onlyDigitLines = mutableListOf<Text.Line>()

        visionText.textBlocks.forEach { block ->
            // set position
            if (player.positionId == null) {
                player.getPositionList().find {
                    it.first == block.text
                }?.let { player.setPosition(block.text) }
            }

            // set preferred foot
            if (player.preferredFootId == null) {
                player.getPreferredFootList().find {
                    it.first == block.text
                }?.let { player.setPreferredFoot(block.text) }
            }

            // set attributes
            block.lines.forEach { line ->
                if (line.text.first().isLetter()) onlyNameLines.add(line)
                else onlyDigitLines.add(line)
            }
        }

        onlyNameLines.forEach { line ->
            val posNameTop = line.boundingBox?.top ?: 0
            val posNameBtm = line.boundingBox?.bottom ?: 0
            val posNameRight = line.boundingBox?.right ?: 0

            val digitsSameY = onlyDigitLines.filter {
                val posDigitTop = it.boundingBox?.top ?: 0
                val posDigitBtm = it.boundingBox?.bottom ?: 0
                val posDigitRight = it.boundingBox?.right ?: 0

                val isTopSame = (posDigitTop - posNameTop).absoluteValue < 10
                val isBtmSame = (posDigitBtm - posNameBtm).absoluteValue < 10
                val isOnRight = posNameRight < posDigitRight

                isTopSame && isBtmSame && isOnRight
            }

            val point = digitsSameY.minByOrNull {
                val posDigitLeft = it.boundingBox?.left ?: 0
                posNameRight - posDigitLeft
            }?.text?.filter { it.isDigit() }?.toIntOrNull() ?: 0

            // update attr
            if (point in 1..20) player.attributes.find { it.name == line.text }?.point = point
        }

        // show report dialog
        val hasAttrZero = player.attributes.find { it.point == 0 }
        vb.tvResult.apply {
            text = "Try to move your camera position if you are unable to detect it.\n\n"
            text = when {
                player.positionId == null ->
                    "${vb.tvResult.text}Point your camera to Player Position."
                hasAttrZero != null ->
                    "${vb.tvResult.text}Point your camera to ${hasAttrZero.name}."
                player.preferredFootId == null ->
                    "${vb.tvResult.text}Point your camera to Preferred Foot."
                else -> {
                    openReportDialog(); "Calculating CA..."
                }
            }
        }
    }

    private fun openReportDialog() {
        cameraExecutor.shutdown()

        fun openEditAttrDialog(attr: String) {
            // split val
            val split = attr.split(": ")
            val attrName = split[0]
            val attrPoint = split[1]

            // dialog
            MaterialDialog(this)
                .onDismiss { openReportDialog() }
                .show {
                    lifecycleOwner(this@MainActivity)
                    cornerRadius(24f)
                    negativeButton(text = "Back")
                    positiveButton(text = "Confirm")
                    title(text = "Edit $attrName")
                    message(text = "Current Value: $attrPoint")
                    input(
                        hint = "Value",
                        prefill = attrPoint,
                        inputType = TYPE_NUMBER_VARIATION_NORMAL
                    ) { _, input ->
                        player.attributes.find {
                            it.name == attrName
                        }?.point = "$input".toIntOrNull() ?: 1
                    }
                    getInputField().apply {
                        gravity = CENTER
                        post { selectAll() }
                        setBackgroundColor(
                            resources.getColor(
                                android.R.color.transparent, null
                            )
                        )
                    }
                }
        }

        fun openEditPosDialog() {
            val items = player.getPositionList().map { it.first }
            val initialSelection = player.getPositionList().indexOfFirst {
                it.first == player.positionName
            }

            MaterialDialog(this)
                .onDismiss { openReportDialog() }
                .show {
                    lifecycleOwner(this@MainActivity)
                    cornerRadius(24f)
                    negativeButton(text = "Back")
                    positiveButton(text = "Confirm")
                    title(text = "Edit Position")
                    message(text = "Current Value: ${player.positionName}")
                    listItemsSingleChoice(
                        items = items,
                        initialSelection = initialSelection
                    ) { _, _, text ->
                        player.setPosition("$text")
                        dismiss()
                    }
                }
        }

        // report dialog
        MaterialDialog(this).show {
            lifecycleOwner(this@MainActivity)
            cancelable(false)

            title(text = "Rating: ~${player.getRating()}")
            message(
                text = "Position: ${player.positionName}" +
                        "\nScanned Preferred Foot: ${player.preferredFootName}"
            )

            listItems(
                items = player.getReportAttrs(),
                waitForPositiveButton = false
            ) { _, _, text ->
                when {
                    "$text".contains("Position:") -> {
                        openEditPosDialog()
                        dismiss()
                    }
                    !"$text".contains("//") -> {
                        openEditAttrDialog("$text")
                        dismiss()
                    }
                }
            }

            negativeButton(text = "Close") { finishAfterTransition() }
            positiveButton(text = "Reset") { recreate() }
        }
    }

    private fun openHelpDialog() {
        fun readMore() {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.read_more))
                )
            )
            finishAfterTransition()
        }

        MaterialDialog(this).show {
            lifecycleOwner(this@MainActivity)
            title(text = "Hello, Fellow Manager!")
            message(text = "Read more about this app from Medium @ifnyas")

            negativeButton(text = "Back") { dismiss() }
            positiveButton(text = "Read More") { readMore() }
        }
    }

    // Text Reader Analyzer
    private class TextReaderAnalyzer(
        private val textFoundListener: (Text) -> Unit
    ) : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            imageProxy.image?.let { process(it, imageProxy) }
        }

        private fun process(image: Image, imageProxy: ImageProxy) {
            try {
                readTextFromImage(
                    InputImage.fromMediaImage(image, 90),
                    imageProxy
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun readTextFromImage(image: InputImage, imageProxy: ImageProxy) {
            TextRecognition.getClient(DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener { v -> processTextFromImage(v) }
                .addOnFailureListener { e -> e.printStackTrace() }
                .addOnCompleteListener { imageProxy.close() }
        }

        private fun processTextFromImage(visionText: Text) = textFoundListener(visionText)
    }
}