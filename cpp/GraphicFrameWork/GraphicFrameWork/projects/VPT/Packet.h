#pragma once
#include <jni.h>

class Packet {
public:
	Packet(jobject packetObj, int packetId, int resultType);
	jobject packetObj_m;
	int packetId_m;
	int resultType_m;
};