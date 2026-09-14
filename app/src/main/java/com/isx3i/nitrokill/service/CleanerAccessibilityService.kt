package com.isx3i.nitrokill.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class CleanerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "NitroKillCleaner"

        private const val ACTION_DISMISS =
            AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS.id
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally passive.
        // The service only acts when dismissCurrentWindow() is called.
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    /**
     * Attempts to dismiss the currently active window.
     *
     * @return true if a dismiss action was successfully performed.
     */
    fun dismissCurrentWindow(): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false

            val result = dismissNode(rootNode)

            rootNode.recycle()

            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dismiss current window", e)
            false
        }
    }

    /**
     * Searches the accessibility node tree for the dismiss action.
     */
    private fun dismissNode(node: AccessibilityNodeInfo): Boolean {

        for (action in node.actionList) {
            if (action.id == ACTION_DISMISS) {
                return node.performAction(action.id)
            }
        }

        for (index in 0 until node.childCount) {

            val child = node.getChild(index) ?: continue

            try {
                if (dismissNode(child)) {
                    return true
                }
            } finally {
                child.recycle()
            }
        }

        return false
    }
}
