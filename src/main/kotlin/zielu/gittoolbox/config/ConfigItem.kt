package zielu.gittoolbox.config

internal interface ConfigItem<T : ConfigItem<T>> {
  fun copy(): T
}
