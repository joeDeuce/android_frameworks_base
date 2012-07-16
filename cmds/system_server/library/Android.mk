LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	system_init.cpp

base = $(LOCAL_PATH)/../../..
native = $(LOCAL_PATH)/../../../../native

LOCAL_C_INCLUDES := \
	$(base)/services/sensorservice \
	$(native)/services/surfaceflinger \
	$(JNI_H_INCLUDE)

ifeq ($(BOARD_USES_QCOM_HARDWARE),true)
LOCAL_C_INCLUDES +=  hardware/qcom/display/libqcomui
LOCAL_CFLAGS += -DQCOM_HARDWARE
endif

LOCAL_SHARED_LIBRARIES := \
	libandroid_runtime \
	libsensorservice \
	libsurfaceflinger \
    libinput \
	libutils \
	libbinder \
	libcutils

LOCAL_MODULE:= libsystem_server

include $(BUILD_SHARED_LIBRARY)
