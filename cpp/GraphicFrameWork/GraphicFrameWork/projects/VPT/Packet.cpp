#include "Packet.h"

Packet::Packet(jobject packetObj, int packetId, int resultType) {
	packetObj_m = packetObj;
	packetId_m = packetId;
	resultType_m = resultType;
}