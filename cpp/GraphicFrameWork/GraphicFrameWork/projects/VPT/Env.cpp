#include "Env.h"

JavaVM* Env::jvm = nullptr;

jint TryAttachCurrentThreadAsNormalOrDaemon(bool isDaemon, JavaVM* jvm, void** penv, void* args) {
	if (isDaemon) {
		return jvm->AttachCurrentThreadAsDaemon(penv, args);
	}
	else {
		return jvm->AttachCurrentThread(penv, args);
	}
}

JNIEnv* Env::GetJNIEnv(bool isDaemon) {
	JNIEnv* env = nullptr;
	JNIEnv** envPtr = &env;
	jint result = jvm->GetEnv((void**)envPtr, JNI_VERSION_10);
	if (result == JNI_EDETACHED) {
		envPtr = &env;
		result = TryAttachCurrentThreadAsNormalOrDaemon(isDaemon, jvm, (void**)envPtr, NULL);
		if (result == JNI_OK) {
			return env;
		}
		else {
			throw JNIAttachException(true, result);
		}
	}
	else if (result == JNI_OK) {
		return env;
	}
	else {
		throw JNIAttachException(false, result);
	}
}

void Env::DetachCurrentThread() {
	jvm->DetachCurrentThread();
}