#include <iostream>
#include "SDL.h"
#include "SDL_image.h"
#include "Instance.h"
#include <SDL_ttf.h>
#include <jni.h>
#include "projects/VPT/client.h"
#include "projects/VPT/editor.h"
#include <vector>
#include "projects/VPT/RelativePaths.h"
#ifndef USE_DEBUG_CLIENT
#include "projects/VPT/client_ClientJNI.h"
#else
#include "projects/VPT/VPT.h"
#endif
#include "projects/VPT/PacketId.h"
#include "projects/VPT/ResultId.h"
using namespace std;
vector <AppInstance*> instances;
#ifndef USE_DEBUG_CLIENT
JNIEXPORT void JNICALL Java_client_ClientJNI_cppMain(JNIEnv* env, jclass claz, jobjectArray ja);
JNIEXPORT void JNICALL Java_client_ClientJNI_recievePacket(JNIEnv* env, jclass claz, jobject packet);
JNIEXPORT void JNICALL Java_client_ClientJNI_socketClosed(JNIEnv* env, jclass claz);
#else
JNIEXPORT void JNICALL Java_VPT_cppMain(JNIEnv* env, jclass claz, jobjectArray ja);
JNIEXPORT void JNICALL Java_VPT_recievePacket(JNIEnv* env, jclass claz, jobject packet);
JNIEXPORT void JNICALL Java_VPT_socketClosed(JNIEnv* env, jclass claz);
#endif
int main(int argc, char* argv[])
{

    if (SDL_Init(SDL_INIT_EVERYTHING) == 0) {
        cout << "SDL init worked" << endl;
    }
    else
    {
        cout << "SDL has not init" << endl;
    }
    if (TTF_Init() < 0) {
        cout << "TTf has not init" << endl;
    }
    else {
        cout << "TTf init worked" << endl;
    }
    instances.push_back(new client::client());
    client::AppData appdata;
    instances[0]->Init(appdata.win_name, appdata.w, appdata.h);
    instances[0]->Loop();
    return 0;
}

void cppMain(JNIEnv* env, jclass claz, jobjectArray ja) {
    jmethodID mid = env->GetStaticMethodID(claz, "CallBack", "()V");
    env->CallStaticVoidMethod(claz, mid);
    client::client::SetJNIEnv(env);
    if (SDL_Init(SDL_INIT_EVERYTHING) == 0) {
        cout << "SDL init worked" << endl;
    }
    else
    {
        cout << "SDL has not init" << endl;
    }
    if (TTF_Init() < 0) {
        cout << "TTf has not init" << endl;
    }
    else {
        cout << "TTf init worked" << endl;
    }
    instances.push_back(new client::client());
    client::AppData appdata;
    instances[0]->Init(appdata.win_name, appdata.w, appdata.h);
    instances[0]->Loop();
    return;
}

void recievePacket(JNIEnv* env, jclass claz, jobject packetIn) {
    jobject packet = env->NewGlobalRef(packetIn);
    jclass packetClass = env->FindClass("common/networking/packet/Packet");
    if (!env->IsInstanceOf(packet, packetClass)) {
        //Not a packet
        return;
    }
    int packetId = env->GetIntField(packet, env->GetFieldID(packetClass, "id", "I"));
    cout << packetId << endl;
    if (packet == NULL || packetId == PacketId_NULL) {
        //null packet
        return;
    }
    if (packetId == PacketId_FORCE_LOGOUT) {
        client::client::QueuePacket(new Packet(packet, packetId, ResultId_NULL));
    }
    if (packetId == PacketId_RESULT) {
        //ResultPacket
        jclass resultPacketClass = env->FindClass("common/networking/packet/packets/result/ResultPacket");
        if (!env->IsInstanceOf(packet, resultPacketClass)) {
            //invalid result packet
            return;
        }
        int resultId = env->GetIntField(packet, env->GetFieldID(resultPacketClass, "resultType", "I"));
        if (resultId == ResultId_NULL) {
            //null result packet
            return;
        }
        client::client::QueuePacket(new Packet(packet, packetId, resultId));
    }
    //unsupported packet type
    return;
}

void socketClosed(JNIEnv* env, jclass claz) {
    return;
}

#ifndef USE_DEBUG_CLIENT
JNIEXPORT void JNICALL Java_client_ClientJNI_cppMain(JNIEnv* env, jclass claz, jobjectArray ja)
{
    cppMain(env, claz, ja);
}

JNIEXPORT void JNICALL Java_client_ClientJNI_recievePacket(JNIEnv* env, jclass claz, jobject packetIn)
{
    recievePacket(env, claz, packetIn);
}

JNIEXPORT void JNICALL Java_client_ClientJNI_socketClosed(JNIEnv* env, jclass claz)
{
    socketClosed(env, claz);
}
#else
JNIEXPORT void JNICALL Java_VPT_cppMain(JNIEnv* env, jclass claz, jobjectArray ja)
{
    cppMain(env, claz, ja);
}

JNIEXPORT void JNICALL Java_VPT_recievePacket(JNIEnv* env, jclass claz, jobject packetIn)
{
    recievePacket(env, claz, packetIn);
}

JNIEXPORT void JNICALL Java_VPT_socketClosed(JNIEnv* env, jclass claz)
{
    socketClosed(env, claz);
}
#endif