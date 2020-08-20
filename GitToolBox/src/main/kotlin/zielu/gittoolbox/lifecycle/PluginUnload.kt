package zielu.gittoolbox.lifecycle

import zielu.gittoolbox.util.AppUtil
import java.util.concurrent.atomic.AtomicBoolean

internal class PluginUnload {
    private val unloading = AtomicBoolean()

    fun markUnloading() {
        unloading.compareAndSet(false, true)
    }

    companion object {
        @JvmStatic
        fun isUnloading(): Boolean {
            return AppUtil.getExistingServiceInstance(PluginUnload::class.java)
              .map { it.unloading.get() }
              .orElse(false)
        }

        fun isInactive(): Boolean {
            return !AppUtil.getExistingServiceInstance(PluginUnload::class.java).isPresent
        }

        fun getInstance(): PluginUnload {
            return AppUtil.getServiceInstance(PluginUnload::class.java)
        }
    }
}
