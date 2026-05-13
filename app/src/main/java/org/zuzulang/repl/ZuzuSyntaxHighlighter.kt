package org.zuzulang.repl

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat

class ZuzuSyntaxHighlighter(
	context: Context
) {

	private val keywordColour = context.colour( R.color.z_code_keyword )
	private val stringColour = context.colour( R.color.z_code_string )
	private val numberColour = context.colour( R.color.z_code_number )
	private val commentColour = context.colour( R.color.z_code_comment )
	private val operatorColour = context.colour( R.color.z_code_operator )
	private val punctuationColour = context.colour( R.color.z_code_punctuation )

	fun highlight( text: Editable ) {
		text.getSpans( 0, text.length, ZuzuColourSpan::class.java ).forEach( text::removeSpan )
		text.getSpans( 0, text.length, ZuzuStyleSpan::class.java ).forEach( text::removeSpan )

		for ( token in tokenize( text.toString() ) ) {
			val colour = when ( token.type ) {
				TokenType.Keyword -> keywordColour
				TokenType.String -> stringColour
				TokenType.Number -> numberColour
				TokenType.Comment -> commentColour
				TokenType.Operator -> operatorColour
				TokenType.Punctuation -> punctuationColour
			}

			text.setSpan(
				ZuzuColourSpan( colour ),
				token.start,
				token.end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
			)

			if ( token.type == TokenType.Keyword ) {
				text.setSpan(
					ZuzuStyleSpan( Typeface.BOLD ),
					token.start,
					token.end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			} else if ( token.type == TokenType.Comment ) {
				text.setSpan(
					ZuzuStyleSpan( Typeface.ITALIC ),
					token.start,
					token.end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
				)
			}
		}
	}

	private fun tokenize( source: String ): List<Token> {
		val tokens = mutableListOf<Token>()
		var index = 0

		while ( index < source.length ) {
			val char = source[index]
			val start = index

			when {
				char.isWhitespace() -> {
					index++
				}

				source.startsWith( "//", index ) -> {
					index = source.indexOf( '\n', index ).takeIf { it >= 0 } ?: source.length
					tokens += Token( start, index, TokenType.Comment )
				}

				source.startsWith( "/*", index ) -> {
					val end = source.indexOf( "*/", index + 2 )
					index = if ( end >= 0 ) end + 2 else source.length
					tokens += Token( start, index, TokenType.Comment )
				}

				source.startsWithAny( blockStringDelimiters, index ) != null -> {
					val delimiter = source.startsWithAny( blockStringDelimiters, index ) ?: ""
					index = scanBlockString( source, index, delimiter )
					tokens += Token( start, index, TokenType.String )
				}

				char == '"' || char == '\'' || char == '`' -> {
					index = scanQuotedString( source, index, char )
					tokens += Token( start, index, TokenType.String )
				}

				isRegexStart( source, index ) -> {
					index = scanRegex( source, index )
					tokens += Token( start, index, TokenType.String )
				}

				char.isDigit() -> {
					index = scanNumber( source, index )
					tokens += Token( start, index, TokenType.Number )
				}

				isIdentifierStart( char ) -> {
					index = scanIdentifier( source, index )
					val word = source.substring( start, index )
					if ( keywords.contains( word ) || builtinTypes.contains( word ) ) {
						tokens += Token( start, index, TokenType.Keyword )
					}
				}

				else -> {
					val operator = source.startsWithAny( operators, index )
					if ( operator != null ) {
						index += operator.length
						tokens += Token( start, index, TokenType.Operator )
					} else {
						index++
						if ( punctuation.contains( char ) ) {
							tokens += Token( start, index, TokenType.Punctuation )
						}
					}
				}
			}
		}

		return tokens
	}

	private fun scanBlockString( source: String, start: Int, delimiter: String ): Int {
		val end = source.indexOf( delimiter, start + delimiter.length )
		return if ( end >= 0 ) end + delimiter.length else source.length
	}

	private fun scanQuotedString( source: String, start: Int, quote: Char ): Int {
		var index = start + 1
		while ( index < source.length ) {
			if ( source[index] == '\\' ) {
				index += 2
			} else if ( source[index] == quote ) {
				return index + 1
			} else {
				index++
			}
		}
		return source.length
	}

	private fun scanRegex( source: String, start: Int ): Int {
		var index = start + 1
		while ( index < source.length ) {
			if ( source[index] == '\\' ) {
				index += 2
			} else if ( source[index] == '/' ) {
				index++
				if ( index < source.length && source[index] == 'i' ) {
					index++
				}
				return index
			} else if ( source[index] == '\n' ) {
				return index
			} else {
				index++
			}
		}
		return source.length
	}

	private fun scanNumber( source: String, start: Int ): Int {
		if (
			start + 1 < source.length
			&& source[start] == '0'
			&& ( source[start + 1] == 'x' || source[start + 1] == 'X' )
		) {
			var index = start + 2
			while ( index < source.length && source[index].isHexDigit() ) {
				index++
			}
			return index
		}

		if (
			start + 1 < source.length
			&& source[start] == '0'
			&& ( source[start + 1] == 'b' || source[start + 1] == 'B' )
		) {
			var index = start + 2
			while ( index < source.length && ( source[index] == '0' || source[index] == '1' ) ) {
				index++
			}
			return index
		}

		var index = start
		while ( index < source.length && source[index].isDigit() ) {
			index++
		}
		if ( index < source.length && source[index] == '.' ) {
			index++
			while ( index < source.length && source[index].isDigit() ) {
				index++
			}
		}
		if ( index < source.length && ( source[index] == 'e' || source[index] == 'E' ) ) {
			val exponentStart = index
			index++
			if ( index < source.length && ( source[index] == '+' || source[index] == '-' ) ) {
				index++
			}
			val digitStart = index
			while ( index < source.length && source[index].isDigit() ) {
				index++
			}
			if ( digitStart == index ) {
				return exponentStart
			}
		}
		return index
	}

	private fun scanIdentifier( source: String, start: Int ): Int {
		var index = start + 1
		while ( index < source.length && isIdentifierPart( source[index] ) ) {
			index++
		}
		return index
	}

	private fun isRegexStart( source: String, index: Int ): Boolean {
		if ( source[index] != '/' || index + 1 >= source.length ) {
			return false
		}
		val next = source[index + 1]
		return next != '/' && next != '*' && next != '=' && !next.isWhitespace()
	}

	private fun isIdentifierStart( char: Char ): Boolean =
		char == '_' || char.isLetter()

	private fun isIdentifierPart( char: Char ): Boolean =
		char == '_' || char.isLetterOrDigit()

	private fun Char.isHexDigit(): Boolean =
		isDigit() || this in 'a'..'f' || this in 'A'..'F'

	private fun Context.colour( resourceId: Int ): Int =
		ContextCompat.getColor( this, resourceId )

	private fun String.startsWithAny( values: List<String>, startIndex: Int ): String? =
		values.firstOrNull { startsWith( it, startIndex ) }

	private data class Token(
		val start: Int,
		val end: Int,
		val type: TokenType
	)

	private enum class TokenType {
		Keyword,
		String,
		Number,
		Comment,
		Operator,
		Punctuation
	}

	private class ZuzuColourSpan( colour: Int ) : ForegroundColorSpan( colour )

	private class ZuzuStyleSpan( style: Int ) : StyleSpan( style )

	private companion object {
		private val blockStringDelimiters = listOf( "\"\"\"", "'''", "```" )

		private val keywords = setOf(
			"abs", "and", "as", "assert", "async", "await", "but", "can", "case",
			"catch", "ceil", "class", "clear", "cmp", "cmpi", "const", "continue",
			"debug", "default", "die", "do", "does", "else", "eq", "eqi",
			"equivalentof", "extends", "false", "floor", "fn", "for", "from",
			"function", "ge", "gei", "get", "gt", "gti", "has", "if", "import",
			"in", "instanceof", "int", "intersection", "last", "lc", "le", "lei",
			"length", "let", "lt", "lti", "method", "mod", "nand", "ne", "nei",
			"new", "next", "not", "null", "or", "print", "return", "round", "say",
			"self", "set", "spawn", "sqrt", "static", "subsetof", "supersetof",
			"super", "switch", "throw", "trait", "true", "try", "typeof", "uc",
			"union", "unless", "warn", "weak", "while", "with", "xor"
		)

		private val builtinTypes = setOf(
			"Array", "Bag", "Boolean", "Class", "Collection", "Dict", "Function",
			"Number", "Object", "Pair", "PairList", "Set", "String", "Trait"
		)

		private val operators = listOf(
			"...", "<<<", ">>>", "**=", "?:=", "<=>", "?:", "=>", "->", "@?",
			"@@", "~=", ":=", ".(", "{{", "}}", "<<", ">>", "..", "==", "!=",
			"<=", ">=", "+=", "-=", "*=", "/=", "%=", "_=", "++", "--", "**",
			"+", "-", "*", "/", "%", "<", ">", "=", "!", "?", ":", "|", "&",
			".", "^", "~", "\\", "@"
		)

		private val punctuation = setOf( '{', '}', '(', ')', '[', ']', ',', ';' )
	}
}
