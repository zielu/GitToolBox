package zielu.gittoolbox.ui.util

import com.intellij.openapi.Disposable
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import org.apache.commons.lang.StringUtils
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.swing.event.DocumentEvent

internal class RegExpTextField : JBTextField(), Disposable {
  private val textListeners = arrayListOf<TextListener>()

  init {
    document.addDocumentListener(object : DocumentAdapter() {
      override fun textChanged(e: DocumentEvent) {
        val text = text
        val error: String? = validateTextOnChange(text)
        textListeners.forEach { it.invoke(text, error) }
      }
    })
  }

  fun addTextListener(listener: TextListener) {
    textListeners.add(listener)
  }

  private fun validateTextOnChange(text: String): String? {
    return if (StringUtils.isBlank(text)) {
      null
    } else try {
      Pattern.compile(text)
      null
    } catch (exp: PatternSyntaxException) {
      exp.message
    }
  }

  override fun dispose() {
    textListeners.clear()
  }
}

internal typealias TextListener = (text: String, error: String?) -> Unit
