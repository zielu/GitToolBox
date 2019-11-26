package zielu.gittoolbox.config

import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.ResBundle

internal enum class DateType(private val labelKey: String) {
  AUTO("date.type.auto"),
  RELATIVE("date.type.relative"),
  ABSOLUTE("date.type.absolute"),
  HIDDEN("date.type.hidden")
  ;

  @Transient
  fun getDisplayLabel() = ResBundle.message(labelKey)
}
