LOCAL_PATH:=$(call my-dir)

include $(CLEAR_VARS)

#include ../../OpenCV-2.4.7-android-sdk/sdk/native/jni/OpenCV.mk
#include /Users/eddyxd/Documents/workspace/OpenCV-2.4.7-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE	:= jnilibsvm
#LOCAL_CFLAGS	:= -Werror
LOCAL_CFLAGS    := -DDEV_NDK=1
#LOCAL_SRC_FILES := common.cpp acoustic_detection.cpp
LOCAL_SRC_FILES := \
	common.cpp jnilibsvm.cpp \
	libsvm/svm-scale.cpp \
	libsvm/linear-predict.cpp \
	libsvm/svm.cpp \
	libsvm/tron.cpp \
	libsvm/linear.cpp \
	libsvm/blas/daxpy.cpp \
	libsvm/blas/ddot.cpp \
	libsvm/blas/dnrm2.cpp \
	libsvm/blas/dscal.cpp \
	getAudioFeature.cpp \
	DspFilters/Butterworth.cpp \
	DspFilters/Biquad.cpp \
	DspFilters/PoleFilter.cpp \
	DspFilters/Cascade.cpp
	
	

LOCAL_LDLIBS	+= -llog -ldl

include $(BUILD_SHARED_LIBRARY)

