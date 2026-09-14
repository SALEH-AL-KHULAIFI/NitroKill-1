package com.isx3i.nitrokill.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.isx3i.nitrokill.R
import java.lang.ref.WeakReference

/**
 * KNOWN LIMITATION (documented up front, see README):
 * Since Android 5.0 no ordinary app can force-stop other apps — that API
 * simply doesn't exist for non-system apps any more, which is exactly why
 * the previous build's "close apps" button showed a success message but did
 * nothing. The only thing that reliably removes apps from memory on a
 * non-rooted phone is what "task cleaner" apps actually do under the hood:
 * open the system Recents screen and tap its own "Clear all" button (or, if
 * that button doesn't exist on a given OEM skin, dismiss each card).
 * This is inherently launcher-dependent — Pixel/stock, Samsung One UI, MIUI
 * and others each label and lay out Recents slightly differently, so this
 * is a best-effort implementation, not a guarantee, and needs testing on
 * whichever phones NitroKill is expected to run on.
 */
class CleanerAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: WeakReference<CleanerAccessibilityService>? = null
        private val CLEAR_ALL_LABELS = listOf(
            "clear all", "close all", "clear",       // stock / Pixel / OnePlus
            "مسح الكل", "إغلاق الكل", "مسح الكل",     // Arabic UIs
            "clear all cards", "close all"
        )

        fun isEnabled(): Boolean = instance?.get() != null

        /** Returns true if the request was dispatched (service is enabled), false if not. */
        fun requestClose(): Boolean {
            val service = instance?.get() ?: return false
            service.performCleanup()
            return true
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Deliberately empty — cleanup is driven by performCleanup()'s own
        // polling loop rather than reacting to every window-state event,
        // which keeps this service idle (and battery-cheap) the rest of the time.
    }

    override fun onInterrupt() {}

    private fun performCleanup() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
        // Recents needs a beat to render before its nodes exist.
        var attemptsLeft = 6
        lateinit var attempt: Runnable
        attempt = Runnable {
            val root = rootInActiveWindow
            val clicked = root?.let { findAndClickClearAll(it) } ?: false
            if (clicked) {
                toast(getString(R.string.cleaner_toast_done))
            } else if (--attemptsLeft > 0) {
                handler.postDelayed(attempt, 250L)
            } else if (root != null) {
                // No "Clear all" button on this launcher — dismiss cards individually.
                dismissEachCard(root)
                toast(getString(R.string.cleaner_toast_done))
            }
        }
        handler.postDelayed(attempt, 300L)
    }

    private fun findAndClickClearAll(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").lowercase()
        if (text.isNotEmpty() && CLEAR_ALL_LABELS.any { text.contains(it) }) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            var parent = node.parent
            var hops = 0
            while (parent != null && hops < 4) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                parent = parent.parent
                hops++
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickClearAll(child)) return true
        }
        return false
    }

    /** Fallback for launchers with no single "clear all" affordance: dismiss each recent-task node. */
    private fun dismissEachCard(node: AccessibilityNodeInfo) {
        if (node.actionList.any { it.id == AccessibilityNodeInfo.ACTION_DISMISS.id }) {
            node.performAction(AccessibilityNodeInfo.ACTION_DISMISS)
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { dismissEachCard(it) }
        }
    }

    private fun toast(message: String) {
        handler.post { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }
}
