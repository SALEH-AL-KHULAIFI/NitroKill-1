# NitroKill — proguard/R8 rules
# Keep the accessibility service class name (referenced by name in the manifest/XML config)
-keep class com.isx3i.nitrokill.service.CleanerAccessibilityService { *; }
-keep class com.isx3i.nitrokill.service.SpeedMonitorService { *; }

# Compose compiler already ships consumer rules; nothing else app-specific is needed.
