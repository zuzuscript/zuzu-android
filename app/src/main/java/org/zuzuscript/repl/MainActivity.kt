package org.zuzuscript.repl

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

	private lateinit var input: EditText
	private lateinit var output: TextView
	private lateinit var runButton: Button
	private lateinit var clearButton: Button
	private lateinit var webView: WebView
	private lateinit var bridge: ZuzuBridge

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate( savedInstanceState: Bundle? ) {
		super.onCreate( savedInstanceState )
		setContentView( R.layout.activity_main )

		input = findViewById( R.id.input_code )
		output = findViewById( R.id.output_text )
		runButton = findViewById( R.id.run_button )
		clearButton = findViewById( R.id.clear_button )
		webView = findViewById( R.id.runtime_webview )

		webView.settings.javaScriptEnabled = true
		webView.settings.allowFileAccess = false
		webView.settings.allowContentAccess = false
		webView.loadUrl( "file:///android_asset/index.html" )

		bridge = ZuzuBridge( webView )

		runButton.setOnClickListener {
			runCode()
		}

		clearButton.setOnClickListener {
			output.text = ""
		}
	}

	private fun runCode() {
		val code = input.text.toString()
		if ( code.isBlank() ) {
			return
		}

		bridge.evaluate( code ) { result ->
			runOnUiThread {
				val previous = output.text.toString()
				val next = if ( previous.isBlank() ) {
					result
				} else {
					"$previous\n\n$result"
				}
				output.text = next
			}
		}
	}
}
