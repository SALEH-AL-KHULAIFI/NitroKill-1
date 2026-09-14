package com.isx3i.nitrokill.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class CleanerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "NitroKillCleaner"

        private var instance: CleanerAccessibilityService? = null

        /**
         * Returns true when the AccessibilityService
         * is currently connected and available.
         */
        fun isEnabled(): Boolean {
            return instance != null
        }

        /**
         * Requests the currently active AccessibilityService
         * to dismiss the current window.
         */
        fun requestClose(): Boolean {
            return instance?.dismissCurrentWindow() == true
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        instance = this

        Log.d(
            TAG,
            "Accessibility service connected"
        )
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        // The service remains passive.
        // Actions are performed only when explicitly requested.
    }

    override fun onInterrupt() {
        Log.d(
            TAG,
            "Accessibility service interrupted"
        )
    }

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }

        Log.d(
            TAG,
            "Accessibility service destroyed"
        )

        super.onDestroy()
    }

    /**
     * Attempts to dismiss the currently active window.
     */
    fun dismissCurrentWindow(): Boolean {
        return try {
            val rootNode = rootInActiveWindow
                ?: return false

            try {
                dismissNode(rootNode)
            } finally {
                rootNode.recycle()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to dismiss current window",
                e
            )

            false
        }
    }

    /**
     * Searches the AccessibilityNodeInfo tree
     * for the standard dismiss action.
     */
    private fun dismissNode(
        node: AccessibilityNodeInfo
    ): Boolean {

        for (action in node.actionList) {

            if (
                action.id ==
                AccessibilityNodeInfo
                    .AccessibilityAction
                    .ACTION_DISMISS
                    .id
            ) {
                return node.performAction(action.id)
            }
        }

        for (index in 0 until node.childCount) {

            val child = node.getChild(index)
                ?: continue

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
