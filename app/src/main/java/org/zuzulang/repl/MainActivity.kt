package org.zuzulang.repl

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

	private lateinit var input: EditText
	private lateinit var output: TextView
	private lateinit var outputScroll: ScrollView
	private lateinit var runButton: Button
	private lateinit var webView: WebView
	private lateinit var bridge: ZuzuBridge
	private lateinit var highlighter: ZuzuSyntaxHighlighter
	private var runtimeReady = false

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate( savedInstanceState: Bundle? ) {
		super.onCreate( savedInstanceState )
		setContentView( R.layout.activity_main )

		input = findViewById( R.id.input_code )
		output = findViewById( R.id.output_text )
		outputScroll = findViewById( R.id.output_scroll )
		runButton = findViewById( R.id.run_button )
		webView = findViewById( R.id.runtime_webview )
		highlighter = ZuzuSyntaxHighlighter( this )

		input.inputType = InputType.TYPE_CLASS_TEXT or
			InputType.TYPE_TEXT_FLAG_MULTI_LINE or
			InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
			InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
		input.setHorizontallyScrolling( false )
		val codeTypeface = Typeface.create( "monospace", Typeface.NORMAL )
		input.typeface = codeTypeface
		output.typeface = codeTypeface

		webView.settings.javaScriptEnabled = true
		webView.settings.allowFileAccess = true
		webView.settings.allowFileAccessFromFileURLs = true
		webView.settings.allowContentAccess = false
		webView.webViewClient = object : WebViewClient() {
			override fun onPageFinished( view: WebView?, url: String? ) {
				checkRuntimeReady()
			}
		}
		webView.loadUrl( "file:///android_asset/index.html" )

		bridge = ZuzuBridge( webView )

		input.setText( DEFAULT_SCRIPT )
		highlighter.highlight( input.text )
		input.addTextChangedListener( CodeTextWatcher() )

		runButton.isEnabled = false
		runButton.text = "Loading Runtime"
		runButton.setOnClickListener {
			runCode()
		}
	}

	private fun checkRuntimeReady() {
		bridge.checkReady { ready ->
			runOnUiThread {
				runtimeReady = ready
				runButton.isEnabled = true
				runButton.text = if ( ready ) "Run Script" else "Runtime Diagnostics"
			}
		}
	}

	private fun runCode() {
		val code = input.text.toString()
		if ( code.isBlank() ) {
			return
		}

		output.text = ""
		runButton.isEnabled = false
		bridge.evaluate( code ) { result ->
			runOnUiThread {
				output.text = result
				outputScroll.post {
					outputScroll.fullScroll( ScrollView.FOCUS_DOWN )
				}
				runButton.isEnabled = true
			}
		}
	}

	private inner class CodeTextWatcher : TextWatcher {
		private var isApplyingEdit = false
		private var changedStart = 0
		private var insertedText = ""

		override fun beforeTextChanged( s: CharSequence?, start: Int, count: Int, after: Int ) {
		}

		override fun onTextChanged( s: CharSequence?, start: Int, before: Int, count: Int ) {
			if ( isApplyingEdit || s == null ) {
				return
			}

			changedStart = start
			insertedText = s.subSequence( start, start + count ).toString()
		}

		override fun afterTextChanged( s: Editable ) {
			if ( isApplyingEdit ) {
				return
			}

			isApplyingEdit = true
			try {
				when ( insertedText ) {
					"\n" -> indentAfterNewline( s, changedStart + 1 )
					"}" -> outdentClosingBrace( s, changedStart )
				}
				highlighter.highlight( s )
			} finally {
				isApplyingEdit = false
			}
		}
	}

	private fun indentAfterNewline( text: Editable, position: Int ) {
		if ( position > text.length ) {
			return
		}

		val source = text.toString()
		val previousLineEnd = position - 1
		val previousLineStart = if ( previousLineEnd <= 0 ) {
			0
		} else {
			source.lastIndexOf( '\n', previousLineEnd - 1 ) + 1
		}
		val previousLine = source.substring( previousLineStart, previousLineEnd )
		val indent = previousLine.takeWhile { it == ' ' || it == '\t' } +
			if ( previousLine.trimEnd().endsWith( "{" ) ) INDENT else ""

		if ( indent.isNotEmpty() ) {
			text.insert( position, indent )
			input.setSelection( position + indent.length )
		}
	}

	private fun outdentClosingBrace( text: Editable, bracePosition: Int ) {
		val source = text.toString()
		val lineStart = if ( bracePosition <= 0 ) {
			0
		} else {
			source.lastIndexOf( '\n', bracePosition - 1 ) + 1
		}
		val prefix = source.substring( lineStart, bracePosition )

		if ( prefix.isEmpty() || prefix.any { it != ' ' && it != '\t' } ) {
			return
		}

		val removeCount = when {
			prefix.endsWith( INDENT ) -> INDENT.length
			prefix.endsWith( "\t" ) -> 1
			else -> prefix.length.coerceAtMost( INDENT.length )
		}

		text.delete( bracePosition - removeCount, bracePosition )
		input.setSelection( bracePosition - removeCount + 1 )
	}

	private companion object {
		private const val INDENT = "    "
		private const val DEFAULT_SCRIPT = "say \"Hello from ZuzuScript\";\n" +
			"let nums := [ 1 ... 5 ];\n" +
			"say nums;"
	}
}
