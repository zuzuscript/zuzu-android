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

	window.zuzuAndroidEvaluate = function ( code ) {
		try {
			if ( typeof window.zuzu_eval !== 'function' ) {
				return '[error] window.zuzu_eval is unavailable';
			}
			var result = window.zuzu_eval( code );
			return '[ok]\n' + safeStringify( result );
		} catch ( err ) {
			var message = err && err.stack ? err.stack : String( err );
			return '[error]\n' + message;
		}
	};
})();
