package org.zuzuscript.repl

import android.webkit.WebView
import org.json.JSONObject

class ZuzuBridge(
	private val webView: WebView
) {

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
