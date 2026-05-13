package org.zuzulang.repl

import android.webkit.WebView
import org.json.JSONObject

class ZuzuBridge(
	private val webView: WebView
) {

	fun checkReady( callback: ( Boolean ) -> Unit ) {
		webView.evaluateJavascript(
			"typeof window.zuzuAndroidRuntimeReady === 'function' && window.zuzuAndroidRuntimeReady();"
		) { raw ->
			callback( raw == "true" )
		}
	}

	fun evaluate( code: String, callback: ( String ) -> Unit ) {
		val jsArg = JSONObject.quote( code )
		val script = "window.zuzuAndroidEvaluate($jsArg);"

		webView.evaluateJavascript( script ) { raw ->
			val rendered = raw
				?.trim()
				?.removePrefix( "\"" )
				?.removeSuffix( "\"" )
				?.replace( "\\n", "\n" )
				?.replace( "\\\"", "\"" )
				?: "(no result)"
			callback( rendered )
		}
	}
}
