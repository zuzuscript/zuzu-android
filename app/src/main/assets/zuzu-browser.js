(function () {
	if ( typeof window.zuzu_eval === 'function' ) {
		return;
	}
	window.zuzu_eval = function ( code ) {
		return {
			note: 'Stub runtime. Run scripts/sync-zuzu-browser-bundle.sh',
			input: code
		};
	};
})();
