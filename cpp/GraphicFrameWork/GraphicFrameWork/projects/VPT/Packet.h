#pragma once
#include <jni.h>
#include "Env.h"

class Packet {
public:
	Packet(jobject packetObj, int packetId, int resultType);
	~Packet() { Env::GetJNIEnv()->DeleteGlobalRef(packetObj_m); };
	jobject packetObj_m;
	int packetId_m;
	int resultType_m;
};