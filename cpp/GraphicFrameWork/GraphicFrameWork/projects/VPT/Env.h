#pragma once
#include <jni.h>
#include <exception>
#include <string>

using namespace std;

class Env {
public:
	static JavaVM* jvm;
	static JavaVM* GetJVM() { return jvm; }
	static void SetJVM(JavaVM* newJVM) { jvm = newJVM; };
	static JNIEnv* GetJNIEnv(bool isDaemon);
	static JNIEnv* GetJNIEnv() { return GetJNIEnv(true); };
	static void DetachCurrentThread();
};

class JNIAttachException : public exception {
public:
	JNIAttachException(bool triedAttach, int resultStatus) : triedAttach(triedAttach), resultStatus(resultStatus) {}
	static string bool_to_string(bool b) { return b ? "true" : "false"; }
	virtual const char* what() const throw() {
		string out = string("Error Attaching Thread! Tried Attach: ") + bool_to_string(triedAttach) + string(" Result Code: ") + bool_to_string(resultStatus);
		return out.c_str();
	}
	const bool triedAttach;
	const int resultStatus;
};