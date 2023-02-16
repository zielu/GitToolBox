package zielu.intellij.util

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator

internal object ZDummyProgressIndicator : ProgressIndicator {
  override fun start() {
  }

  override fun stop() {
  }

  override fun isRunning(): Boolean {
    return true
  }

  override fun cancel() {
  }

  override fun isCanceled(): Boolean {
    return false
  }

  override fun setText(text: String?) {
  }

  override fun getText(): String {
    return ""
  }

  override fun setText2(text: String?) {
  }

  override fun getText2(): String {
    return ""
  }

  override fun getFraction(): Double {
    return 0.0
  }

  override fun setFraction(fraction: Double) {
  }

  override fun pushState() {
  }

  override fun popState() {
  }

  override fun isModal(): Boolean {
    return false
  }

  override fun getModalityState(): ModalityState {
    return ModalityState.any()
  }

  override fun setModalityProgress(modalityProgress: ProgressIndicator?) {
  }

  override fun isIndeterminate(): Boolean {
    return true
  }

  override fun setIndeterminate(indeterminate: Boolean) {
  }

  override fun checkCanceled() {
  }

  override fun isPopupWasShown(): Boolean {
    return false
  }

  override fun isShowing(): Boolean {
    return false
  }
}
