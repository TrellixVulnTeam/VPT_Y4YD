#include <iostream>
#include "SDL.h"
#include "SDL_image.h"
#include "Instance.h"
#include <SDL_ttf.h>
#include <jni.h>
#include "projects/VPT/client.h"
#include "projects/VPT/VPT.h"
#include <vector>
using namespace std;
vector <AppInstance*> instances;
JNIEXPORT void JNICALL Java_VPT_cppMain(JNIEnv *env, jclass claz, jobjectArray ja);
JNIEXPORT void JNICALL Java_VPT_forceLogout(JNIEnv* env, jclass claz);
JNIEXPORT void JNICALL Java_VPT_socketClosed(JNIEnv* env, jclass claz);
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

JNIEXPORT void JNICALL Java_VPT_cppMain(JNIEnv* env, jclass claz, jobjectArray ja)
{
    jmethodID mid = env->GetStaticMethodID(claz, "CallBack", "()V");
    env->CallStaticVoidMethod(claz, mid);
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

JNIEXPORT void JNICALL Java_VPT_forceLogout(JNIEnv* env, jclass claz)
{
    return;
}

JNIEXPORT void JNICALL Java_VPT_socketClosed(JNIEnv* env, jclass claz)
{
    return;
}
