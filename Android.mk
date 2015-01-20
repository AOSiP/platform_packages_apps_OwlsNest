LOCAL_PATH:= $(call my-dir)
#include $(CLEAR_VARS)
#
#LOCAL_MODULE_TAGS := optional
#
#LOCAL_JAVA_LIBRARIES := bouncycastle telephony-common
#LOCAL_JAVA_LIBRARIES += org.cyanogenmod.hardware
#LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 android-support-v4
#
#LOCAL_SRC_FILES := $(call all-subdir-java-files)
#
#LOCAL_PACKAGE_NAME := IllusionBox
#LOCAL_CERTIFICATE := platform
#
#include $(BUILD_PACKAGE)
#
# Use the folloing include to make our test apk.
#include $(call all-makefiles-under,$(LOCAL_PATH))
