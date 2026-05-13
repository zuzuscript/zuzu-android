(function () {
	function safeStringify( value ) {
		try {
			if ( typeof value === 'string' ) {
				return value;
			}
			return JSON.stringify( value, null, 2 );
		} catch ( err ) {
			return String( value );
		}
	}

	function renderOutputLines( value ) {
		return String( value == null ? '' : value ).replace( /\r\n/g, '\n' ).replace( /\n$/g, '' );
	}

	function appendPart( parts, value ) {
		var text = renderOutputLines( value );
		if ( text !== '' ) {
			parts.push( text );
		}
	}

	function diagnostics() {
		var available = [
			'zuzu_run=' + ( typeof window.zuzu_run ),
			'zuzu_eval=' + ( typeof window.zuzu_eval ),
			'zuzu_runtime=' + ( typeof window.zuzu_runtime )
		].join( ', ' );
		var errors = window.__zuzuAndroidLoadErrors || [];
		return errors.length > 0
			? available + '\nload errors:\n' + errors.join( '\n' )
			: available;
	}

	window.zuzuAndroidRuntimeReady = function () {
		return typeof window.zuzu_run === 'function' || typeof window.zuzu_eval === 'function';
	};

	window.zuzuAndroidEvaluate = function ( code ) {
		try {
			if ( typeof window.zuzu_run === 'function' ) {
				var result = window.zuzu_run( code, { throwOnError: false } );
				if ( result && typeof result.then === 'function' ) {
					return '[error]\nAsynchronous browser runtime results are not supported by the Android bridge yet.';
				}

				var parts = [];
				appendPart( parts, result && result.stdout );
				appendPart( parts, result && result.stderr );
				if ( result && typeof result.result !== 'undefined' ) {
					appendPart( parts, '=> ' + safeStringify( result.result ) );
				}
				if ( parts.length === 0 ) {
					appendPart( parts, safeStringify( result ) );
				}
				return result && result.status !== 0
					? '[error]\n' + parts.join( '\n' )
					: parts.join( '\n' );
			}

			if ( typeof window.zuzu_eval !== 'function' ) {
				return '[error]\nZuzuScript runtime API is unavailable.\n' + diagnostics();
			}

			return safeStringify( window.zuzu_eval( code ) );
		} catch ( err ) {
			var message = err && err.stack ? err.stack : String( err );
			return '[error]\n' + message;
		}
	};
})();
