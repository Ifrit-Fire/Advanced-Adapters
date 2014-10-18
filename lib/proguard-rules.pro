# The following is not actually used by lib. Only here for official reference

# Advanced-Adapters
-keepclassmembers class com.sawyer.advadapters.widget.JSONAdapter {
	boolean isFilteredOut(...);
}
-keepclassmembers class * extends com.sawyer.advadapters.widget.JSONAdapter {
	boolean isFilteredOut(...);
}